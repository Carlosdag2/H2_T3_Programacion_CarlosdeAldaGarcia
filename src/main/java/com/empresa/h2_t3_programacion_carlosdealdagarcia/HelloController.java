package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoClientException;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.regex.Pattern;

public class HelloController {

    @FXML
    private TableView<Persona> tablaDatos;
    @FXML
    private TableColumn<Persona, String> columnaId;
    @FXML
    private TableColumn<Persona, String> columnaNombre;
    @FXML
    private TableColumn<Persona, String> columnaCorreo;
    @FXML
    private TableColumn<Persona, String> columnaContrasena;
    @FXML
    private TextField campoNombre;
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

        // Configurar la política de redimensionamiento de las columnas
        tablaDatos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Ajustar cada columna para que ocupe una proporción del ancho disponible
        columnaId.setMaxWidth(1f * Integer.MAX_VALUE * 10);        // 10% del ancho disponible
        columnaNombre.setMaxWidth(1f * Integer.MAX_VALUE * 30);    // 30% del ancho disponible
        columnaCorreo.setMaxWidth(1f * Integer.MAX_VALUE * 30);    // 30% del ancho disponible
        columnaContrasena.setMaxWidth(1f * Integer.MAX_VALUE * 30); // 30% del ancho disponible

        // Establecer conexión a MongoDB
        try {
            mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.gomt1im.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
            MongoDatabase database = mongoClient.getDatabase("hito2_mongo");
            coleccion = database.getCollection("usuarios");

            coleccion.createIndex(Indexes.ascending("correo"), new IndexOptions().unique(true));
        } catch (MongoClientException e) {
            showErrorAlert("Error de Conexión", "No se pudo conectar a la base de datos.");
            return;
        }

        // Cargar datos iniciales
        cargarDatos();

        // Agregar un listener al campo de búsqueda
        campoBusqueda.textProperty().addListener((observable, oldValue, newValue) -> filtrarPersonas(newValue));
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
            showWarningAlert("Advertencia", "Por favor, rellena todos los campos.");
        } else if (!isEmailValid(correo)) {
            showWarningAlert("Advertencia", "Por favor, introduce un correo electrónico válido. Debe incluir un punto (.) después del nombre de dominio y el símbolo @.");
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
                    showErrorAlert("Error", "El correo ya existe en la base de datos.");
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
            showWarningAlert("Advertencia", "Por favor, selecciona un usuario para actualizar.");
        } else {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actualizar-view.fxml"));
                Parent root = fxmlLoader.load();

                ActualizarController actualizarController = fxmlLoader.getController();
                actualizarController.setPersona(personaSeleccionada);

                Stage stage = new Stage();
                stage.setTitle("Actualizar Usuario");
                stage.setScene(new Scene(root, 400, 300));
                stage.show();
                stage.setOnHidden(e -> cargarDatos());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void manejarEliminar() {
        Persona personaSeleccionada = tablaDatos.getSelectionModel().getSelectedItem();
        if (personaSeleccionada == null) {
            showWarningAlert("Advertencia", "Por favor, selecciona un usuario para eliminar.");
        } else {
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

    private boolean isEmailValid(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}