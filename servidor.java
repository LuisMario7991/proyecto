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

import javax.crypto.Cipher;
import javax.swing.JFileChooser;

public class servidor {

    static ServerSocket socket;

    private static Connection connect() throws SQLException {
        String url = "jdbc:mysql://chef-server.mysql.database.azure.com:3306/chef?useSSL=true";
        return DriverManager.getConnection(url, "chefadmin", "ch3f4dm1n!");
    }

    public static void main(String[] args) {
        try {
            startServer();
            waitForClient(socket);

            LoginScreen.initialize(args); // Inicia con la pantalla de login
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void showMainInterface(Stage primaryStage) {
        setupUI(primaryStage);
    }

    private static void setupUI(Stage stage) {
        stage.setTitle("File Sharing App");
        Button subirButton = new Button("Subir");
        Button compartirButton = new Button("Compartir");
        Button validarButton = new Button("Validar");
        Button agregarButton = new Button("Agregar usuario");
        Button eliminarButton = new Button("Eliminar usuario");

        subirButton.setOnAction(e -> subirArchivo());
        compartirButton.setOnAction(e -> compartirArchivo());
        validarButton.setOnAction(e -> validarArchivo());
        agregarButton.setOnAction(e -> agregaUsuario());
        eliminarButton.setOnAction(e -> eliminaUsuario());

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton);
        Scene scene = new Scene(vbox, 300, 200);
        stage.setScene(scene);
        stage.show();
    }

    private static void subirArchivo() {
        System.out.println("Subiendo archivo...");

        String connectStr = "DefaultEndpointsProtocol=https;AccountName=criptografia;AccountKey=ZqM6hU8KHAPBja0nWQ5YaiAC12vJqGv44R4HEytD5UwoLBpupGwQT2SAGVetp9kqPz04F+6M0WGW+ASt499lYQ==;EndpointSuffix=core.windows.net";
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
            socket = new ServerSocket(PORT);
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

                // Crear un nuevo hilo para manejar la conexión con el cliente
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();

                DHKeyExchange.ServerDH dhKeyExchange;
                dhKeyExchange = new DHKeyExchange.ServerDH();
                dhKeyExchange.exchangeKeys(socket);

                break;
            } catch (Exception e) {
                Thread.sleep(1000);
                System.err.println("Error en el intercambio de llaves: " + e.getMessage());
            }
        }
    }

    public static void recibirArchivo(Socket socket) {
        try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
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

                bufferedOutputStream.flush(); // Asegurar que todos los datos han sido escritos

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

        }
    }
}
