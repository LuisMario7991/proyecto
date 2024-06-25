import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFileChooser;

public class servidor {

    public static void main(String[] args) {
        LoginScreen.initialize(args); // Inicia con la pantalla de login
    }

    public static void showMainInterface(Stage primaryStage) {
        setupUI(primaryStage); // Configura y muestra la UI principal
        startServer(); // Inicia el servidor
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
    }

    private static void eliminaUsuario() {
        System.out.println("Eliminando usuario...");
    }

    private static void startServer() {
        final int PORT = 12345;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor listo\nBob está escuchando en el puerto " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    static class ClientHandler extends Thread {
        private Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Ejemplo de manejo de cliente
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                // Asumiendo algún tipo de protocolo de comunicación aquí
                input.close();
                output.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error al manejar el cliente: " + e.getMessage());
            }
        }
    }
}
