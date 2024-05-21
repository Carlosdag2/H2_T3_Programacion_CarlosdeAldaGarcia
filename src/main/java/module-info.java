module com.empresa.h2_t3_programacion_carlosdealdagarcia {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.empresa.h2_t3_programacion_carlosdealdagarcia to javafx.fxml;
    exports com.empresa.h2_t3_programacion_carlosdealdagarcia;
}