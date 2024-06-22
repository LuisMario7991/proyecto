import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.*;
import java.security.*;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;

public class DHKeyExchange {

    public static class ServerDH {
        private final DHParameterSpec dhSpec;
        private final KeyPair keyPair;
        private final PublicKey publicKey;
        private final PrivateKey privateKey;

        public ServerDH() throws Exception {
            // Generar parámetros Diffie-Hellman
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(2048);
            AlgorithmParameters params = paramGen.generateParameters();
            this.dhSpec = params.getParameterSpec(DHParameterSpec.class);

            // Generar par de claves Diffie-Hellman
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
            keyPairGen.initialize(dhSpec);
            this.keyPair = keyPairGen.generateKeyPair();

            // Obtener clave pública y privada
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        }

        public void exchangeKeys(Socket socket) throws Exception {
            // Enviar parámetros Diffie-Hellman a Alice
            OutputStream out = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(dhSpec.getP());
            oos.writeObject(dhSpec.getG());
            oos.writeInt(dhSpec.getL());
            oos.flush();
            System.out.println("Parámetros Diffie-Hellman enviados a Alice.");

            // Enviar clave pública a Alice
            oos.writeObject(publicKey);
            oos.flush();
            System.out.println("Clave pública de Bob enviada a Alice.");

            // Recibir clave pública de Alice
            InputStream in = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            PublicKey alicePublicKey = (PublicKey) ois.readObject();
            System.out.println("Clave pública de Alice recibida.");

            // Generar la clave compartida
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(privateKey);
            keyAgree.doPhase(alicePublicKey, true);
            byte[] sharedSecret = keyAgree.generateSecret();

            // Calcular hash SHA-256 de la clave compartida
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            byte[] first16Bytes = Arrays.copyOf(sharedSecretHash, 16);
            System.out.println("Clave compartida hash (Bob): " + bytesToHex(sharedSecretHash));

            

            // Guarda el hash en un archivo TXT
            String fileName = "hasht.txt";
            Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);

            //ois.close();
            //oos.close();
        }

        private static String bytesToHex(byte[] bytes) {
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }
}
