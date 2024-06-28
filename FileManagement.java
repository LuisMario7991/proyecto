import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PublicKey;

public class FileManagement {

    protected static void subirArchivo(Socket clientSocket) {
        System.out.println("Subiendo archivo...");

        String connectStr = "https://criptografia.blob.core.windows.net/recetas?sp=racwdli&st=2024-06-26T05:33:45Z&se=2024-06-28T13:33:45Z&sv=2022-11-02&sr=c&sig=uPPamXwmkNlP69aTGs0bP8BFrCo39o5X3Smed7gazVE%3D";
        String containerName = "recetas";

        recibirArchivo(clientSocket, "uploadFile");

        File selectedFile = new File("uploadFile");

        BlobUploader blobUploader = new BlobUploader(connectStr, containerName);
        blobUploader.uploadEncryptedBlob(selectedFile);
        System.out.println("Subiendo archivo...");
    }

    protected static void compartirArchivo(Socket clientSocket) {
        // Lógica para compartir archivo
        System.out.println("Compartiendo archivo...");
        // Aquí se debería implementar la lógica para enviar el archivo al cliente
        // mediante sockets
        // Ejemplo básico:
        // enviarArchivoAlCliente();
    }

    protected static void validarArchivo(Socket clientSocket) {
        try {
            System.out.println("Validando archivo");
            
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            // Aplicar hash SHA-256 al archivo 'm.txt'
            byte[] fileHash;
            fileHash = Utilidades.hashFile("recibido_m.txt");
            
            PublicKey publicKey = Utilidades.getPublicKeyFromFile("recibido_PublicKey.pem");

            byte[] decryptedHash = Utilidades.decryptWithPublicKey("recibido_encrypted_hash.bin",
                    publicKey);

            // Comparar los hashes
            boolean isMatch = MessageDigest.isEqual(fileHash, decryptedHash);
            System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT MATCH"));

            dataOutputStream.writeBoolean(isMatch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void recibirArchivo(Socket clientSocket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();
            String saveFilePath = "recibido_" + fileName;

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                bufferedOutputStream.flush(); // Asegurar que todataOutputStream los datos han sido escritos

                if (totalBytesRead == fileSize) {
                    System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);

                } else {
                    System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead
                            + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                }

                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void recibirArchivo(Socket clientSocket, String saveFilePath) {
        try {
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    bufferedOutputStream.flush();
                    totalBytesRead += bytesRead;
                }
                fileOutputStream.close();

                if (totalBytesRead == fileSize) {
                    System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);
                } else {
                    System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead
                            + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
