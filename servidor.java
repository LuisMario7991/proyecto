import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

public class servidor {

    static ServerSocket serverSocket;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

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

    private static void subirArchivo() {
        System.out.println("Subiendo archivo...");

        String connectStr = "https://criptografia.blob.core.windows.net/recetas?sp=racwdli&st=2024-06-26T05:33:45Z&se=2024-06-28T13:33:45Z&sv=2022-11-02&sr=c&sig=uPPamXwmkNlP69aTGs0bP8BFrCo39o5X3Smed7gazVE%3D";
        String containerName = "recetas";

        // Crear un selector de archivos usando JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Subir el archivo cifrado al contenedor en Azure Blob Storage

            BlobUploader blobUploader = new BlobUploader(connectStr, containerName);
            blobUploader.uploadEncryptedBlob(selectedFile);
            System.out.println("Subiendo archivo...");
        }
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
        System.out.println("Agregando usuario...");

        // Crear una ventana para ingresar datos del usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("administrador", "colaborador");
        Button addButton = new Button("Agregar");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Tipo de Usuario:"), 0, 2);
        grid.add(typeComboBox, 1, 2);
        grid.add(addButton, 1, 3);

        addButton.setOnAction(e -> {
            String hashedPassword = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
            try (Connection conn = connect();
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO Usuarios (Correo, Contrasena, TipoUsuario) VALUES (?, ?, ?)")) {
                stmt.setString(1, emailField.getText());
                stmt.setString(2, hashedPassword); // Guardar la contraseña hasheada
                stmt.setString(3, typeComboBox.getValue());
                stmt.executeUpdate();
                System.out.println("Usuario agregado exitosamente.");
                stage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private static void eliminaUsuario() {
        System.out.println("Eliminando usuario...");

        // Crear una ventana para eliminar un usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        Button deleteButton = new Button("Eliminar");

        grid.add(new Label("Email del usuario a eliminar:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(deleteButton, 1, 1);

        deleteButton.setOnAction(e -> {
            try (Connection conn = connect();
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, emailField.getText());
                stmt.executeUpdate();
                stage.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
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
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // Crear un nuevo hilo para manejar la conexión con el cliente
                new Thread(new ClientHandler(socket)).start();
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
                System.err.println("Error en el intercambio de llaves: " + e.getMessage());
            }
        }
    }

    public static void recibirArchivo(Socket socket) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] hashBytes(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    private static byte[] hashFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return digest.digest(fileBytes);
    }

    private static PublicKey getPublicKeyFromFile(String filePath) throws Exception {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static byte[] decryptWithPublicKey(String filePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        return cipher.doFinal(encryptedData);
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
                            dataOutputStream.writeUTF("INVALID"); // Enviar mensaje de error si la contraseña no coincide
                            dataOutputStream.flush();
                            continue;
                        }

                        dataOutputStream.writeUTF(userType); // Enviar tipo de usuario al cliente si la autenticación es exitosa
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
                    subirArchivo();
                    break;
                case "compartirArchivo":
                    compartirArchivo();
                    break;
                case "validarArchivo":
                    validarArchivo();
                    break;
                // Agrega más comandos según necesidad
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
