public class Main {
    public static void main(String[] args) {
        Servidor.startServer();
        Servidor.waitForClient();
        LoginScreen.initialize(args);
    }
}
