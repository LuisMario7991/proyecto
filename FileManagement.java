import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.PublicKey;

public class FileManagement {

    private static Commands command = new Commands();

    protected static void subirArchivo() {
        System.out.println("Subiendo archivo...");

        recibirArchivo("uploadFile");

        File selectedFile = new File("uploadFile");

        BlobUploader blobUploader = new BlobUploader();
        // blobUploader.getBlob();
        // System.out.println("Conexión comprobada...");
        blobUploader.uploadEncryptedBlob(selectedFile);
        System.out.println("Subiendo archivo...");
    }

    protected static void compartirArchivo() {
        try {

            BlobUploader blobUploader = new BlobUploader();

            // Lógica para compartir archivo
            System.out.println("Compartiendo archivo...");

            String blobName = Servidor.dataInputStream.readUTF();

            byte[] azureFile = blobUploader.getBlob(blobName);

            System.out.println("Enviando archivo: " + blobName + "de tamaño: " + azureFile.length + " bytes");

            // Enviar el nombre del archivo y su tamaño
            Servidor.dataOutputStream.writeUTF(blobName);
            Servidor.dataOutputStream.writeLong(azureFile.length);
            Servidor.dataOutputStream.flush();
            
            Servidor.dataOutputStream.write(azureFile);
            Servidor.dataOutputStream.flush();
            
            System.out.println("Archivo enviado al servidor");
        } catch (Exception e) {
            System.err.println("No se pudo enviar el archivo al cliente: " + e.getMessage());
        }
    }

    protected static boolean validarArchivo() {
        boolean isMatch = false;
        try {
            System.out.println("Validando archivo");

            // Aplicar hash SHA-256 al archivo 'm.txt'
            byte[] fileHash;
            fileHash = Utilidades.hashFile("recibido_m.txt");

            PublicKey publicKey = Utilidades.getPublicKeyFromFile("recibido_PublicKey.pem");

            byte[] decryptedHash = Utilidades.decryptWithPublicKey("recibido_encrypted_hash.bin",
                    publicKey);

            // Comparar los hashes
            isMatch = MessageDigest.isEqual(fileHash, decryptedHash);
            System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT MATCH"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMatch;
    }

    public static void enviarArchivo(String filePath) throws IOException {
        Servidor.dataOutputStream.writeUTF(command.getUploadFile());
        Servidor.dataOutputStream.flush();

        File file = new File(filePath);
        long fileSize = file.length();
        String fileName = file.getName();

        System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

        // Enviar el nombre del archivo y su tamaño
        Servidor.dataOutputStream.writeUTF(fileName);
        Servidor.dataOutputStream.flush();
        Servidor.dataOutputStream.writeLong(fileSize);
        Servidor.dataOutputStream.flush();

        FileInputStream fileInputStream = new FileInputStream(filePath);

        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;

        while (totalBytesRead < fileSize && (bytesRead = fileInputStream.read(buffer)) != -1) {
            try {
                Servidor.dataOutputStream.write(buffer, 0, bytesRead);
                Servidor.dataOutputStream.flush();
                totalBytesRead += bytesRead;

                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fileInputStream.close();

        System.out.println("Archivo enviado al servidor");
    }

    protected static void recibirArchivo() {
        try {
            String fileName = Servidor.dataInputStream.readUTF();
            long fileSize = Servidor.dataInputStream.readLong();
            String saveFilePath = "recibido_" + fileName;

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = Servidor.dataInputStream.read(buffer)) != -1) {
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

    protected static void recibirArchivo(String saveFilePath) {
        try {
            String fileName = Servidor.dataInputStream.readUTF();
            long fileSize = Servidor.dataInputStream.readLong();

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = Servidor.dataInputStream.read(buffer)) != -1) {
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
