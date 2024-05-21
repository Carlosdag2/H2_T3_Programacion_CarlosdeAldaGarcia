package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;

public class HelloController {

    @FXML
    private TableView<Persona> tablaDatos;
    @FXML
    private TableColumn<Persona, String> columnaId;
    @FXML
    private TableColumn<Persona, String> columnaNombre;
    @FXML
    private TextField campoNombre;
    @FXML
    private TableColumn<Persona, String> columnaCorreo;
    @FXML
    private TableColumn<Persona, String> columnaContrasena;
    @FXML
    private TextField campoCorreo;
    @FXML
    private TextField campoContrasena;
    @FXML
    private TextField campoBusqueda;
    private ObservableList<Persona> masterData = FXCollections.observableArrayList();

    private MongoClient mongoClient;
    private MongoCollection<Document> coleccion;

    public void initialize() {
        // Configurar columnas de la tabla
        columnaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        columnaContrasena.setCellValueFactory(new PropertyValueFactory<>("contrasena"));

        // Establecer conexión a MongoDB
        mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.gomt1im.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoDatabase database = mongoClient.getDatabase("hito2_mongo");
        coleccion = database.getCollection("usuarios");

        coleccion.createIndex(Indexes.ascending("correo"), new IndexOptions().unique(true));

        // Cargar datos iniciales
        cargarDatos();

        // Agregar un listener al campo de búsqueda
        campoBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPersonas(newValue);
        });
    }

    private void cargarDatos() {
        masterData.clear();
        for (Document doc : coleccion.find()) {
            String contrasenaDescifrada = Cipher.decrypt(doc.getString("contrasena"));
            Persona persona = new Persona(doc.getObjectId("_id").toString(),
                    doc.getString("nombre"),
                    doc.getString("correo"),
                    contrasenaDescifrada);
            masterData.add(persona);
        }
        tablaDatos.setItems(masterData);
    }

    @FXML
    private void manejarAgregar() {
        String nombre = campoNombre.getText();
        String correo = campoCorreo.getText();
        String contrasena = campoContrasena.getText();

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, rellena todos los campos.");
            alert.showAndWait();
        } else {
            String contrasenaCifrada = Cipher.encrypt(contrasena);
            Document doc = new Document("nombre", nombre).append("correo", correo).append("contrasena", contrasenaCifrada);
            try {
                coleccion.insertOne(doc);
                cargarDatos();
                campoNombre.clear();
                campoCorreo.clear();
                campoContrasena.clear();
            } catch (MongoWriteException e) {
                if (e.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error: El correo ya existe en la base de datos.");
                    alert.showAndWait();
                } else {
                    throw e;
                }
            }
        }
    }

    @FXML
    private void manejarActualizar() {
        Persona personaSeleccionada = tablaDatos.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona un usuario para actualizar.");
            alert.showAndWait();
        } else {
            try {
                // Cargar la vista de actualización
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actualizar-view.fxml"));
                Parent root = fxmlLoader.load();

                // Obtener el controlador y pasarle la persona seleccionada
                ActualizarController actualizarController = fxmlLoader.getController();
                actualizarController.setPersona(personaSeleccionada);

                // Crear una nueva ventana y mostrarla
                Stage stage = new Stage();
                stage.setTitle("Actualizar Usuario");
                stage.setScene(new Scene(root, 400, 300));
                stage.show();
                stage.setOnHidden(e -> cargarDatos()); // Recargar los datos cuando la ventana se cierre
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void manejarEliminar() {
        Persona personaSeleccionada = tablaDatos.getSelectionModel().getSelectedItem();
        if (personaSeleccionada != null) {
            Document query = new Document("_id", new ObjectId(personaSeleccionada.getId()));
            coleccion.deleteOne(query);
            cargarDatos();
        }
    }

    private void filtrarPersonas(String term) {
        FilteredList<Persona> filteredData = new FilteredList<>(masterData, p -> true);

        if (term == null || term.isEmpty()) {
            filteredData.setPredicate(persona -> true);
        } else {
            String lowerCaseFilter = term.toLowerCase();
            filteredData.setPredicate(persona -> persona.getCorreo().toLowerCase().contains(lowerCaseFilter));
        }

        SortedList<Persona> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tablaDatos.comparatorProperty());
        tablaDatos.setItems(sortedData);
    }
}