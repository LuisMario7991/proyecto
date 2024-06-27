import java.io.IOException;
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

    protected static void agregarUsuario() {
        try {
            System.out.println("Agregando usuario...");

            // Lee los datos del nuevo usuario enviados por el cliente
            String email = Servidor.dataInputStream.readUTF();
            String password = Servidor.dataInputStream.readUTF();
            String userType = Servidor.dataInputStream.readUTF();

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

    protected static void eliminarUsuario() {
        try {
            System.out.println("Eliminando usuario...");

            String email = Servidor.dataInputStream.readUTF();

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

    protected static void authenticateUser() {
        while (true) {
            try {
                String userEmail = Servidor.dataInputStream.readUTF();
                String userPassword = Servidor.dataInputStream.readUTF();

                try (Connection connection = connect();
                        PreparedStatement stmt = connection
                                .prepareStatement(
                                        "SELECT Contrasena, TipoUsuario FROM Usuarios WHERE Correo = ?")) {
                    stmt.setString(1, userEmail);
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        Servidor.dataOutputStream.writeUTF("NOT_FOUND"); // Enviar mensaje si el usuario no se encuentra
                        Servidor.dataOutputStream.flush();
                        continue;
                    }

                    String storedPassword = rs.getString("Contrasena");
                    String userType = rs.getString("TipoUsuario");

                    if (!BCrypt.checkpw(userPassword, storedPassword)) {
                        Servidor.dataOutputStream.writeUTF("INVALID"); // Enviar mensaje de error si la contraseña no
                        // coincide
                        Servidor.dataOutputStream.flush();
                        continue;
                    }

                    Servidor.dataOutputStream.writeUTF(userType); // Enviar tipo de usuario al cliente si la
                    // autenticación es exitosa
                    Servidor.dataOutputStream.flush();
                    connection.close();
                    break;
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
