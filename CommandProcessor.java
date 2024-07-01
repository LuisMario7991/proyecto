import java.io.IOException;

public class CommandProcessor {

    public static void processCommand(String command) throws IOException {
        switch (command) {
            case "recibeArchivo":
                FileManagement.compartirArchivo();
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
