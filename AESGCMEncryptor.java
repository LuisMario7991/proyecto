import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;

public class AESGCMEncryptor {

    public static void encryptFile(File inputFile, File keyFile, File outputFile) throws Exception {
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        try (FileOutputStream fos = new FileOutputStream(outputFile);
                CipherOutputStream cos = new CipherOutputStream(fos, cipher);
                FileInputStream fis = new FileInputStream(inputFile)) {

            fos.write(iv);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
            cos.flush();
        }
    }
}
