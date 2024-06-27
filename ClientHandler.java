import java.io.IOException;

class ClientHandler implements Runnable {

    @Override
    public void run() {
        try {
            DHKeyExchange.ServerDH dhKeyExchange;
            dhKeyExchange = new DHKeyExchange.ServerDH();
            dhKeyExchange.exchangeKeys();

            UserManagement.authenticateUser();

            // El servidor ahora espera por comandos
            while (!Servidor.clientSocket.isClosed()) {
                try {
                    System.out.println("Esperando intrucción del cliente");
                    String command = Servidor.dataInputStream.readUTF();
                    CommandProcessor.processCommand(command);
                } catch (IOException e) {
                    System.err.println("Error al leer el comando: " + e.getMessage());
                    Servidor.cerrarConexion();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}