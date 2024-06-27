import java.io.IOException;

public class CommandProcessor {

    protected static void processCommand(String command) throws IOException {
        switch (command) {
            case "subirArchivo":
                FileManagement.subirArchivo();
                break;
            case "compartirArchivo":
                FileManagement.compartirArchivo();
                break;
            case "validarArchivo":
                FileManagement.validarArchivo();
                break;
            case "agregaUsuario":
                UserManagement.agregarUsuario();
                break;
            case "eliminaUsuario":
                UserManagement.eliminarUsuario();
                break;
            case "recibeArchivo":
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
