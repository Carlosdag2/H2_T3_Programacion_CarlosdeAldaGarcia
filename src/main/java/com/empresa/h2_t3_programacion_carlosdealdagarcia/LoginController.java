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

public class LoginController {
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
    private void handleLogin() {
        String correo = campoCorreo.getText();
        String contrasena = campoContrasena.getText();
        String captcha = campoCaptcha.getText();
        if (!captcha.equals(correctCaptcha)) {
            showErrorAlert("Inicio de sesión fallido", "CAPTCHA incorrecto");
            return;
        }
        if (dbConnection.authenticateUser(correo, contrasena)) {
            showInformationAlert("Inicio de sesión correcto", "Bienvenido " + correo);
            // Aquí redirijo a la vista principal
            goToMainView();
        } else {
            showErrorAlert("Inicio de sesión fallido", "Correo o contraseña incorrectos");
        }
    }

    @FXML
    private void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));
            Stage stage = (Stage) campoCorreo.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 650, 450));
        } catch (IOException e) {
            showErrorAlert("Error", "Error al cargar la vista de registro");
        }
    }

    private void goToMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Stage stage = (Stage) campoCorreo.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            showErrorAlert("Error", "Error al cargar la vista principal");
        }
    }

    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
