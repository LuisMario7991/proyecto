import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;

public class AESGCMEncryptor {

    public static void encryptFile(File inputFile, File keyFile, File outputFile) throws Exception {
        // Leer la clave desde el archivo
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Configurar el cifrador AES GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // IV debe tener exactamente 12 bytes para AES GCM
        new SecureRandom().nextBytes(iv); // Generar IV aleatorio para cada cifrado
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        // Leer el archivo a cifrar y cifrarlo
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
            cos.flush();
        }

        // Es importante almacenar el IV junto con el archivo cifrado, pues será necesario para descifrar
        try (FileOutputStream ivOut = new FileOutputStream(outputFile, true)) {
            ivOut.write(iv);
        }
    }
}
