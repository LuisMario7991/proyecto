import java.sql.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Cipher;
import javax.swing.JFileChooser;

public class Servidor {

    static ServerSocket serverSocket;
    protected static DataInputStream dataInputStream;
    protected static DataOutputStream dataOutputStream;
    protected static ObjectInputStream objectInputStream;
    protected static ObjectOutputStream objectOutputStream;

    private static Connection connect() throws SQLException {
        // final Logger logger = LoggerFactory.getLogger(servidor.class);
        String url = "jdbc:mysql://chef-server.mysql.database.azure.com:3306/chef?useSSL=true";
        return DriverManager.getConnection(url, "chefadmin", "ch3f4dm1n!");
    }

    public static void main(String[] args) {
        try {
            startServer();
            waitForClient(serverSocket);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void subirArchivo(Socket clientSocket) {
        System.out.println("Subiendo archivo...");

        String connectStr = "https://criptografia.blob.core.windows.net/recetas?sp=racwdli&st=2024-06-26T05:33:45Z&se=2024-06-28T13:33:45Z&sv=2022-11-02&sr=c&sig=uPPamXwmkNlP69aTGs0bP8BFrCo39o5X3Smed7gazVE%3D";
        String containerName = "recetas";

        recibeArchivo(clientSocket, "uploadFile");

        File selectedFile = new File("uploadFile");

        BlobUploader blobUploader = new BlobUploader(connectStr, containerName);
        blobUploader.uploadEncryptedBlob(selectedFile);
        System.out.println("Subiendo archivo...");
    }

    private static void compartirArchivo() {
        // Lógica para compartir archivo
        System.out.println("Compartiendo archivo...");
        // Aquí se debería implementar la lógica para enviar el archivo al cliente
        // mediante sockets
        // Ejemplo básico:
        // enviarArchivoAlCliente();
    }

    private static void validarArchivo() {
        System.out.println("Validando archivo...");
        // Aplicar hash SHA-256 al archivo 'm.txt'
        // byte[] fileHash = hashFile("received_m.txt");
        // PublicKey publicKey = getPublicKeyFromFile("receivedPublicKey.pem");

        // byte[] decryptedHash = decryptWithPublicKey("received_encrypted_hash.bin",
        // publicKey);

        // Aplicar hash SHA-256 al resultado del descifrado
        // byte[] decryptedHashFileHash = hashBytes(decryptedHash);

        // Comparar los hashes
        // boolean isMatch = MessageDigest.isEqual(fileHash, decryptedHashFileHash);
        // System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT
        // MATCH"));;
    }

    private static void agregaUsuario() {
        try {
            System.out.println("Agregando usuario...");

            // Lee los datos del nuevo usuario enviados por el cliente
            String email = dataInputStream.readUTF();
            String password = dataInputStream.readUTF();
            String userType = dataInputStream.readUTF();

            // Hashea la contraseña
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            try (Connection conn = connect();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO Usuarios (Correo, Contrasena, TipoUsuario) VALUES (?, ?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, userType);
                stmt.executeUpdate();

                System.out.println("Usuario agregado exitosamente.");
                dataOutputStream.writeUTF("Usuario agregado exitosamente");
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    private static void eliminaUsuario() {
        try {
            System.out.println("Eliminando usuario...");

            String email = dataInputStream.readUTF();

            try (Connection conn = connect();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, email);
                stmt.executeUpdate();

                System.out.println("Usuario eliminado exitosamente.");
                dataOutputStream.writeUTF("Usuario eliminado exitosamente");
            } catch (SQLException ex) {
                System.err.println("Error con la conexión de la base de datos: " + ex.getMessage());
                dataOutputStream.writeUTF("Error con la conexión de la base de datos: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error con la recepción de parámetros del usuario: " + e.getMessage());
        }
    }

    private static void startServer() {
        final int PORT = 12345;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor listo\nBob está escuchando en el puerto " + PORT);
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    private static void waitForClient(ServerSocket server) throws InterruptedException {
        System.out.println("Esperando nuevo cliente");
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                // Crear un nuevo hilo para manejar la conexión con el cliente
                new Thread(new ClientHandler(socket)).start();
                break;
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
    }

    public static void recibeArchivo() {
        try {
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();
            String saveFilePath = "recibido_" + fileName;

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                bufferedOutputStream.flush(); // Asegurar que todataOutputStream los datos han sido escritos

                if (totalBytesRead == fileSize) {
                    System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);

                } else {
                    System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead
                            + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                }

                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recibeArchivo(Socket socket, String saveFilePath) {
        try {
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    bufferedOutputStream.flush();
                    totalBytesRead += bytesRead;
                }
                fileOutputStream.close();

                if (totalBytesRead == fileSize) {
                    System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);
                } else {
                    System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead
                    + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cerrarConexion(Socket socketClient) {
        try {
            if (dataInputStream != null)
                dataInputStream.close();

            if (dataOutputStream != null)
                dataOutputStream.close();

            if (socketClient != null && !socketClient.isClosed())
                socketClient.close();

            System.out.println("Conexión cerrada correctamente.");

            waitForClient(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cerrar correctamente la conexión: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("No se pudo esperar nuevo cliente: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                DHKeyExchange.ServerDH dhKeyExchange;
                dhKeyExchange = new DHKeyExchange.ServerDH();
                dhKeyExchange.exchangeKeys(this.clientSocket);

                authenticateUser(this.clientSocket);

                // El servidor ahora espera por comandos
                while (!clientSocket.isClosed()) {
                    try {
                        System.out.println("Esperando intrucción del cliente");
                        String command = dataInputStream.readUTF();
                        processCommand(command);
                    } catch (IOException e) {
                        System.err.println("Error al leer el comando: " + e.getMessage());
                        closeConnection();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void authenticateUser(Socket clientSocket) {
            while (true) {
                try {

                    String userEmail = dataInputStream.readUTF();
                    String userPassword = dataInputStream.readUTF();

                    try (Connection conn = connect();
                            PreparedStatement stmt = conn
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

                        dataOutputStream.writeUTF(userType); // Enviar tipo de usuario al cliente si la autenticación es
                                                             // exitosa
                        dataOutputStream.flush();
                        break;
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processCommand(String command) throws IOException {
            switch (command) {
                case "subirArchivo":
                    subirArchivo(this.clientSocket);
                    break;
                case "compartirArchivo":
                    compartirArchivo();
                    break;
                case "validarArchivo":
                    validarArchivo();
                    break;
                case "agregaUsuario":
                    agregaUsuario();
                    break;
                case "eliminaUsuario":
                    eliminaUsuario();
                    break;
                case "recibeArchivo":
                    recibeArchivo();
                    break;
                case "terminaConexion":
                    cerrarConexion(this.clientSocket);
                    break;
                default:
                    dataOutputStream.writeUTF("Comando desconocido");
                    break;
            }
        }

        private void closeConnection() {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
