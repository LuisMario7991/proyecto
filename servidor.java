import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    protected static ServerSocket serverSocket;
    protected static Socket clientSocket;
    protected static DataInputStream dataInputStream;
    protected static DataOutputStream dataOutputStream;
    protected static ObjectInputStream objectInputStream;
    protected static ObjectOutputStream objectOutputStream;
    
    protected static Socket getClientSocket() {
        return Servidor.clientSocket;
    }

    protected static void startServer() {
        final int PORT = 12345;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor listo\nBob está escuchando en el puerto " + PORT);
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    protected static void waitForClient() throws InterruptedException {
        System.out.println("Esperando nuevo cliente");
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

                // Crear un nuevo hilo para manejar la conexión con el cliente
                new Thread(new ClientHandler()).start();
                break;
            } catch (IOException e) {
                Thread.sleep(1000);
            }
        }
    }

    protected static void cerrarConexion() {
        try {
            if (dataInputStream != null)
                dataInputStream.close();

            if (dataOutputStream != null)
                dataOutputStream.close();

            if (objectInputStream != null)
                objectInputStream.close();

            if (objectOutputStream != null)
                objectOutputStream.close();

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();

            System.out.println("Conexión cerrada correctamente.");

            waitForClient();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("No se pudo cerrar correctamente la conexión: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("No se pudo esperar nuevo cliente: " + e.getMessage());
        }
    }
}
