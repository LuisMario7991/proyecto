import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static ServerSocket serverSocket;

    // DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
    // DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
    // ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    // ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

    protected static void startServer() {
        final int PORT = 12345;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor listo\nBob está escuchando en el puerto " + PORT);
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    protected static void waitForClient() {
        System.out.println("Esperando nuevo cliente");
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Crear un nuevo hilo para manejar la conexión con el cliente
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                System.err.println("Error en la espera de cliente nuevo: " + e.getMessage());
            }
        }
    }

    public static void cerrarConexion(Socket clientSocket) {
        try {
            if (!clientSocket.isClosed() || clientSocket != null) {
                clientSocket.close();
            }

            System.out.println("Conexión cerrada correctamente.");

            waitForClient();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
