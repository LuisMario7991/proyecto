import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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

    protected static void agregarUsuario(Socket clientSocket) {
        try {
            System.out.println("Agregando usuario...");
            
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            // Lee los datos del nuevo usuario enviados por el cliente
            String email = dataInputStream.readUTF();
            String password = dataInputStream.readUTF();
            String userType = dataInputStream.readUTF();

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
                dataOutputStream.writeUTF("Usuario agregado exitosamente");

                connection.close();
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    protected static void eliminarUsuario(Socket clientSocket) {
        try {
            System.out.println("Eliminando usuario...");

            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            String email = dataInputStream.readUTF();

            try (Connection connection = connect();
                    PreparedStatement stmt = connection.prepareStatement("DELETE FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, email);
                stmt.executeUpdate();

                System.out.println("Usuario eliminado exitosamente.");
                dataOutputStream.writeUTF("Usuario eliminado exitosamente");

                connection.close();
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    protected static void authenticateUser(Socket clientSocket) {
        while (true) {
            try {
                
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                
                String userEmail = dataInputStream.readUTF();
                String userPassword = dataInputStream.readUTF();
                
                System.out.println("Autenticando usuario");
                try (Connection connection = connect();
                        PreparedStatement stmt = connection
                                .prepareStatement(
                                        "SELECT Contrasena, TipoUsuario FROM Usuarios WHERE Correo = ?")) {
                    stmt.setString(1, userEmail);
                    ResultSet rs = stmt.executeQuery();

                    if (!rs.next()) {
                        dataOutputStream.writeUTF("NOT_FOUND"); // Enviar mensaje si el usuario no se encuentra
                        dataOutputStream.flush();
                        continue;
                    }

                    String storedPassword = rs.getString("Contrasena");
                    String userType = rs.getString("TipoUsuario");

                    if (!BCrypt.checkpw(userPassword, storedPassword)) {
                        dataOutputStream.writeUTF("INVALID"); // Enviar mensaje de error si la contraseña no
                        // coincide
                        dataOutputStream.flush();
                        continue;
                    }

                    dataOutputStream.writeUTF(userType); // Enviar tipo de usuario al cliente si la
                    // autenticación es exitosa
                    dataOutputStream.flush();
                    connection.close();
                    break;
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
