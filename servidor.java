import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

        subirButton.setOnAction(e -> System.out.println("Subiendo archivo..."));
        compartirButton.setOnAction(e -> System.out.println("Compartiendo archivo..."));
        validarButton.setOnAction(e -> System.out.println("Validando archivo..."));
        agregarButton.setOnAction(e -> System.out.println("Agregar usuario"));
        eliminarButton.setOnAction(e -> System.out.println("Eliminar usuario"));

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton);
        Scene scene = new Scene(vbox, 300, 200);
        stage.setScene(scene);
        stage.show();
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
