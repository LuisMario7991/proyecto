import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CommandProcessor {

    public static void processCommand(Socket clientSocket, String command) throws IOException {
        switch (command) {
            case "subirArchivo":
                FileManagement.subirArchivo(clientSocket);
                break;
            case "compartirArchivo":
                FileManagement.compartirArchivo(clientSocket);
                break;
            case "validarArchivo":
                FileManagement.validarArchivo(clientSocket);
                break;
            case "agregaUsuario":
                UserManagement.agregarUsuario(clientSocket);
                break;
            case "eliminaUsuario":
                UserManagement.eliminarUsuario(clientSocket);
                break;
            case "recibeArchivo":
                FileManagement.recibirArchivo(clientSocket);
                break;
            case "terminaConexion":
                Servidor.cerrarConexion(clientSocket);
                break;
            default:
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeUTF("Comando desconocido");
                break;
        }
    }
}
