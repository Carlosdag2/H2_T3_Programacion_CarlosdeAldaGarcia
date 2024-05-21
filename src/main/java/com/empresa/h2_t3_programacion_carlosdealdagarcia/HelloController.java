package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.ConnectionString;

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

        // Cargar datos iniciales
        cargarDatos();
    }

    private void cargarDatos() {
        tablaDatos.getItems().clear();
        for (Document doc : coleccion.find()) {
            Persona persona = new Persona(doc.getObjectId("_id").toString(), doc.getString("nombre"), doc.getString("correo"), doc.getString("contrasena"));
            tablaDatos.getItems().add(persona);
        }
    }

    @FXML
    private void manejarAgregar() {
        String nombre = campoNombre.getText();
        String correo = campoCorreo.getText();
        String contrasena = campoContrasena.getText();
        Document doc = new Document("nombre", nombre).append("correo", correo).append("contrasena", contrasena);
        coleccion.insertOne(doc);
        cargarDatos();
    }

    @FXML
    private void manejarActualizar() {
        Persona personaSeleccionada = tablaDatos.getSelectionModel().getSelectedItem();
        if (personaSeleccionada != null) {
            Document query = new Document("_id", new ObjectId(personaSeleccionada.getId()));
            Document update = new Document("$set", new Document("nombre", campoNombre.getText()).append("correo", campoCorreo.getText()).append("contrasena", campoContrasena.getText()));
            coleccion.updateOne(query, update);
            cargarDatos();
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
}
