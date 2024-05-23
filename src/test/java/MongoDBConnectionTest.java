import com.empresa.h2_t3_programacion_carlosdealdagarcia.MongoDBConnection;
import com.empresa.h2_t3_programacion_carlosdealdagarcia.Persona;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoDBConnectionTest {
    private static MongoDBConnection mongoDBConnection;
    private static final String TEST_COLLECTION = "usuarios";

    @BeforeClass
    public static void setUp() {
        // Conectar a una base de datos de prueba
        mongoDBConnection = new MongoDBConnection();
        mongoDBConnection.getDatabase().getCollection(TEST_COLLECTION).drop(); // Elimina la colección de prueba si existe
    }

    @AfterClass
    public static void tearDown() {
        // Cerrar la conexión y eliminar la colección de prueba
        mongoDBConnection.getDatabase().getCollection(TEST_COLLECTION).drop();
        mongoDBConnection = null;
    }

    @Test
    public void testRegisterUser() {
        // Crear un usuario de prueba
        Persona user = new Persona("TestName", "test@test.com", "testpassword");

        // Registrar el usuario
        boolean result = mongoDBConnection.registerUser(user);

        // Verificar que el usuario se haya registrado correctamente
        assertTrue(result);

        // Intentar registrar al mismo usuario nuevamente
        boolean resultDuplicate = mongoDBConnection.registerUser(user);

        // Verificar que no se pueda registrar al mismo usuario dos veces
        assertFalse(resultDuplicate);
    }

    @Test
    public void testAuthenticateUser() {
        // Crear un usuario de prueba
        Persona user = new Persona("TestName", "test@test.com", "testpassword");

        // Registrar al usuario
        mongoDBConnection.registerUser(user);

        // Autenticar al usuario con credenciales correctas
        boolean authResult = mongoDBConnection.authenticateUser("test@test.com", "testpassword");

        // Verificar que la autenticación sea exitosa
        assertTrue(authResult);

        // Autenticar al usuario con credenciales incorrectas
        boolean authResultWrong = mongoDBConnection.authenticateUser("test@test.com", "wrongpassword");

        // Verificar que la autenticación falle con credenciales incorrectas
        assertFalse(authResultWrong);

        // Autenticar con un correo que no está registrado
        boolean authResultNonExisting = mongoDBConnection.authenticateUser("nonexistent@test.com", "anypassword");

        // Verificar que la autenticación falle con un correo no registrado
        assertFalse(authResultNonExisting);
    }
}
