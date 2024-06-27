public class Main {
    public static void main(String[] args) {
        try {
            Servidor.startServer();
            Servidor.waitForClient();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
