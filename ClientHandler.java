import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler implements Runnable {
    Socket clientSocket;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(this.clientSocket.getInputStream());

            DHKeyExchange.ServerDH dhKeyExchange;
            dhKeyExchange = new DHKeyExchange.ServerDH();
            dhKeyExchange.exchangeKeys(this.clientSocket);

            UserManagement.authenticateUser(this.clientSocket);

            // El servidor ahora espera por comandos
            while (!this.clientSocket.isClosed()) {
                try {
                    System.out.println("Esperando intrucción del cliente");
                    String command = dataInputStream.readUTF();
                    CommandProcessor.processCommand(this.clientSocket, command);
                } catch (IOException e) {
                    System.out.println("Error al leer el comando: " + e.getMessage());
                    Servidor.cerrarConexion(this.clientSocket);
                }
            }
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error en el exchangeKeys(): " + e.getMessage());
        }
    }
}