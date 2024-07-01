import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.stage.Stage;

public class Servidor {

    private static ServerSocket serverSocket;
    protected static Socket clientSocket;

    protected static DataInputStream dataInputStream;
    protected static DataOutputStream dataOutputStream;
    protected static ObjectInputStream objectInputStream;
    protected static ObjectOutputStream objectOutputStream;

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
        try {
            DHKeyExchange.ServerDH dhKeyExchange;
            dhKeyExchange = new DHKeyExchange.ServerDH();

            clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            
            dhKeyExchange.exchangeKeys();
            new Thread(new ConnectionHandler()).start();
        } catch (IOException e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                System.err.println("Error en la espera de cliente nuevo: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error en el intercambio de llaves: " + e.getMessage());
        }
    }

    protected static String login(String email, String password) {
        try {
            System.out.println("Validando credenciales");
            
            String userType = UserManagement.authenticateUser(email, password);

            if (userType.equals("INVALID") || userType.equals("NOT_FOUND")) {
                System.out.println("Error de autenticación: " + userType);
            } else {
                System.out.println("Bienvenido/a");
            }

            return userType;
        } catch (Exception e) {
            System.err.println("Hubo un problema con la lectura de la respuesta: " + e);
            return null;
        }
    }

    protected static void displayUserInterface(String userType, Stage primaryStage) {
        if (userType.equals("administrador")) {
            AdminInterface.showAdminInterface(primaryStage);
        }
    }

    public static void cerrarConexion() {
        try {
            if (objectInputStream != null)
                objectInputStream.close();

            if (objectOutputStream != null)
                objectOutputStream.close();

            if (dataInputStream != null)
                dataInputStream.close();

            if (dataOutputStream != null)
                dataOutputStream.close();

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();

            System.out.println("Conexión cerrada correctamente.");
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
