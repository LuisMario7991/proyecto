import com.azure.storage.blob.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.nio.file.Path;

public class BlobUploader {
    private String connectStr;
    private String containerName;

    public BlobUploader() {
    }

    public byte[] getBlob(String resource) {
        try {
            String sasBlobUrl = "https://recetaschef.blob.core.windows.net/recetas-chef/" + resource + "?sp=racwd&st=2024-07-01T01:20:59Z&se=2024-07-03T09:20:59Z&sv=2022-11-02&sr=b&sig=ZcAzk5xyMIoPbNZhtcYprOoQXcIeWwVfjqDmaKrDzBw%3D";
            URI uri = new URI(sasBlobUrl);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Error al obtener el blob. Código de respuesta: " + responseCode);
                return null;
            }

            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] blobBytes = outputStream.toByteArray();
            System.out.println("Longitud del blob: " + blobBytes.length + " bytes");

            outputStream.close();
            inputStream.close();

            connection.disconnect();

            return blobBytes;
        } catch (Exception e) {
            System.err.println("Error al obtener el blob: " + e.getMessage());
            return null;
        }
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
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr)
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
