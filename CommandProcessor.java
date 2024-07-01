import java.io.IOException;

public class CommandProcessor {

    public static void processCommand(String command) throws IOException {
        switch (command) {
            case "recibeArchivo":
                System.out.println("Recibiendo archivo del colaborador...");
                FileManagement.recibirArchivo();
            case "enviaArchivo":
                FileManagement.recibirArchivo();
                break;
            case "terminaConexion":
                Servidor.cerrarConexion();
                break;
            default:
                Servidor.dataOutputStream.writeUTF("Comando desconocido");
                break;
        }
    }
}
