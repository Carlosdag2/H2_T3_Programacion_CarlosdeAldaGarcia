package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ActualizarController {

    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoCorreo;
    @FXML
    private TextField campoContrasena;

    private Persona persona;
    private MongoCollection<Document> coleccion;

    public void initialize() {
        // Establecer conexión a MongoDB
        var mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.gomt1im.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        MongoDatabase database = mongoClient.getDatabase("hito2_mongo");
        coleccion = database.getCollection("usuarios");
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
        campoNombre.setText(persona.getNombre());
        campoCorreo.setText(persona.getCorreo());
        campoContrasena.setText(persona.getContrasena());
    }

    @FXML
    private void manejarActualizar() {
        if (persona != null) {
            Document query = new Document("_id", new ObjectId(persona.getId()));
            Document update = new Document("$set", new Document("nombre", campoNombre.getText()).append("correo", campoCorreo.getText()).append("contrasena", campoContrasena.getText()));
            try {
                coleccion.updateOne(query, update);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Información");
                alert.setHeaderText(null);
                alert.setContentText("Usuario actualizado con éxito.");
                alert.showAndWait();
                // Cerrar la ventana
                ((Stage) campoNombre.getScene().getWindow()).close();
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
}