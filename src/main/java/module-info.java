module com.empresa.h2_t3_programacion_carlosdealdagarcia {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.bson;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;


    opens com.empresa.h2_t3_programacion_carlosdealdagarcia to javafx.fxml;
    exports com.empresa.h2_t3_programacion_carlosdealdagarcia;
}