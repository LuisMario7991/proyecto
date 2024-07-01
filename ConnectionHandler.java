import java.io.IOException;

class ConnectionHandler implements Runnable {
    byte[] key;

    @Override
    public void run() {
        try {
            // El servidor ahora espera por comandos
            while (!Servidor.clientSocket.isClosed()) {
                try {
                    System.out.println("Esperando intrucción del cliente");
                    String command = Servidor.dataInputStream.readUTF();
                    CommandProcessor.processCommand(command);
                } catch (IOException e) {
                    System.out.println("Error al leer el comando: " + e.getMessage());
                    Servidor.cerrarConexion();
                }
            }
            Thread.sleep(10000);
        } catch (Exception e) {
            System.out.println("Error en el exchangeKeys(): " + e.getMessage());
        }
    }
}