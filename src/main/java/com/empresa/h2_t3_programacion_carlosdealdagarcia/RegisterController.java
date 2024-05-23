package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoCorreo;
    @FXML
    private PasswordField campoContrasena;
    @FXML
    private TextField campoCaptcha;
    @FXML
    private VBox captchaBox;

    private MongoDBConnection dbConnection = new MongoDBConnection();
    private String correctCaptcha;

    @FXML
    private void initialize() {
        generarCaptcha();
    }

    private void generarCaptcha() {
        correctCaptcha = generateRandomString(6); // Genera una cadena aleatoria de longitud 6
        Text captchaText = new Text(correctCaptcha);
        captchaText.setFont(Font.font("Arial", 20));
        captchaText.setFill(Color.BLACK);

        captchaBox.getChildren().addAll(captchaText);
    }

    private String generateRandomString(int length) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    @FXML
    private void handleRegister() {
        String nombre = campoNombre.getText();
        String correo = campoCorreo.getText();
        String contrasena = campoContrasena.getText();
        String captcha = campoCaptcha.getText();
        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || captcha.isEmpty()) {
            showWarningAlert("Advertencia", "Por favor, rellena todos los campos.");
        } else if (!isEmailValid(correo)) {
            showWarningAlert("Advertencia", "Por favor, introduce un correo electrónico válido.");
        } else if (!captcha.equals(correctCaptcha)) {
            showWarningAlert("Advertencia", "El CAPTCHA ingresado es incorrecto.");
        } else {
            if (dbConnection.registerUser(new Persona(nombre, correo, contrasena))) {
                showInformationAlert("Registro Exitoso", "Te has registrado exitosamente. Ahora puedes iniciar sesión.");
                goToLogin();
            } else {
                showErrorAlert("Registro Fallido", "El correo ya está registrado.");
            }
        }
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Stage stage = (Stage) campoCorreo.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 650, 450));
        } catch (IOException e) {
            showErrorAlert("Error", "Error al cargar la vista de inicio de sesión");
        }
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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