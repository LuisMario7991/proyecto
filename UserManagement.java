import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import org.mindrot.jbcrypt.BCrypt;

public class UserManagement {

    private static Connection connect() throws SQLException {
        // final Logger logger = LoggerFactory.getLogger(servidor.class);
        String url = "jdbc:mysql://chef-server.mysql.database.azure.com:3306/chef?useSSL=true";
        return DriverManager.getConnection(url, "chefadmin", "ch3f4dm1n!");
    }

    protected static void agregarUsuario(String email, String password, String userType) {
        try {
            System.out.println("Agregando usuario...");

            // Hashea la contraseña
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            try (Connection connection = connect();
                    PreparedStatement stmt = connection.prepareStatement(
                            "INSERT INTO Usuarios (Correo, Contrasena, TipoUsuario) VALUES (?, ?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, userType);
                stmt.executeUpdate();

                System.out.println("Usuario agregado exitosamente.");
                Servidor.dataOutputStream.writeUTF("Usuario agregado exitosamente");

                connection.close();
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                Servidor.dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    protected static void eliminarUsuario(String email) {
        try {
            System.out.println("Eliminando usuario...");

            try (Connection connection = connect();
                    PreparedStatement stmt = connection.prepareStatement("DELETE FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, email);
                stmt.executeUpdate();

                System.out.println("Usuario eliminado exitosamente.");
                Servidor.dataOutputStream.writeUTF("Usuario eliminado exitosamente");

                connection.close();
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                Servidor.dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    protected static String authenticateUser(String userEmail, String userPassword) {
        try {
            System.out.println("Autenticando usuario");
            try (Connection connection = connect();
                    PreparedStatement stmt = connection
                            .prepareStatement(
                                    "SELECT Contrasena, TipoUsuario FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, userEmail);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return "NOT_FOUND";
                }

                String storedPassword = rs.getString("Contrasena");
                String userType = rs.getString("TipoUsuario");

                if (!BCrypt.checkpw(userPassword, storedPassword)) {
                    return "INVALID";
                }

                connection.close();

                return userType;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL_ERROR";
        }
    }
}
