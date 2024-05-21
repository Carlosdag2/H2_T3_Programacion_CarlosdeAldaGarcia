package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoClientException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.regex.Pattern;

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
        try {
            var mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.gomt1im.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
            MongoDatabase database = mongoClient.getDatabase("hito2_mongo");
            coleccion = database.getCollection("usuarios");
        } catch (MongoClientException e) {
            showErrorAlert("Error de Conexión", "No se pudo conectar a la base de datos.");
        }
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
            String nombre = campoNombre.getText();
            String correo = campoCorreo.getText();
            String contrasena = campoContrasena.getText();

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
                showWarningAlert("Advertencia", "Por favor, rellena todos los campos.");
                return;
            }

            if (!isEmailValid(correo)) {
                showWarningAlert("Advertencia", "Por favor, introduce un correo electrónico válido. Debe incluir un punto (.) después del nombre de dominio y el símbolo @.");
                return;
            }

            String contrasenaCifrada = Cipher.encrypt(contrasena);
            Document query = new Document("_id", new ObjectId(persona.getId()));
            Document update = new Document("$set", new Document("nombre", nombre).append("correo", correo).append("contrasena", contrasenaCifrada));

            try {
                coleccion.updateOne(query, update);
                showInformationAlert("Información", "Usuario actualizado con éxito.");
                // Cerrar la ventana
                Stage stage = (Stage) campoNombre.getScene().getWindow();
                stage.close();
            } catch (MongoWriteException e) {
                if (e.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY)) {
                    showErrorAlert("Error", "El correo ya existe en la base de datos.");
                } else {
                    throw e;
                }
            }
        }
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

    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}