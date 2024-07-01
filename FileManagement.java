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

        recibirArchivo(clientSocket, "uploadFile");

        File selectedFile = new File("uploadFile");

        BlobUploader blobUploader = new BlobUploader();
        // blobUploader.getBlob();
        // System.out.println("Conexión comprobada...");
        blobUploader.uploadEncryptedBlob(selectedFile);
        System.out.println("Subiendo archivo...");
    }

    protected static void compartirArchivo(Socket clientSocket) {
        try {

            BlobUploader blobUploader = new BlobUploader();

            // Lógica para compartir archivo
            System.out.println("Compartiendo archivo...");

            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            String blobName = dataInputStream.readUTF();

            byte[] azureFile = blobUploader.getBlob(blobName);

            System.out.println("Enviando archivo: " + blobName + "de tamaño: " + azureFile.length + " bytes");

            // Enviar el nombre del archivo y su tamaño
            dataOutputStream.writeUTF(blobName);
            dataOutputStream.writeLong(azureFile.length);
            dataOutputStream.flush();
            
            dataOutputStream.write(azureFile);
            dataOutputStream.flush();
            
            System.out.println("Archivo enviado al servidor");
        } catch (Exception e) {
            System.err.println("No se pudo enviar el archivo al cliente: " + e.getMessage());
        }
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
