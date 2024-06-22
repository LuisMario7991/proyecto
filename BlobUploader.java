import com.azure.storage.blob.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.nio.file.Path;

public class BlobUploader {
    private String connectStr;
    private String containerName;
   

    public BlobUploader(String connectStr, String containerName) {
        this.connectStr = connectStr;
        this.containerName = containerName;
    }

    public void uploadEncryptedBlob(File file) {
        try {
            // Generar una clave y un IV para AES-GCM
           
            SecretKey secretKey = readAESKeyFromFile("hasht.txt");
            byte[] iv = generateIV();
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            // Inicializar el cifrador AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            // Leer el contenido del archivo a cifrar
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // Cifrar el archivo
            byte[] encryptedBytes = cipher.doFinal(fileBytes);
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectStr)
            .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(file.getName() + ".encrypted");
            blobClient.upload(new ByteArrayInputStream(combined), combined.length, true);


            

            System.out.println("Se han subido de forma correcta los blobs!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SecretKey readAESKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Path.of(fileName));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[12]; // Tamaño fijo del IV para GCM
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

}
