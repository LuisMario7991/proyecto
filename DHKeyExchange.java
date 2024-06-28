import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
            System.out.println("Generando parámetros Diffie-Hellman");
            
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

        public void exchangeKeys(Socket clientSocket) throws Exception {
            System.out.println("Compartiendo parámetros Diffie-Hellman");

            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            // Enviar parámetros Diffie-Hellman a Alice
            objectOutputStream.writeObject(dhSpec.getP());
            objectOutputStream.flush();
            objectOutputStream.writeObject(dhSpec.getG());
            objectOutputStream.flush();
            objectOutputStream.writeInt(dhSpec.getL());
            objectOutputStream.flush();
            System.out.println("Parámetros Diffie-Hellman enviados a Alice.");

            // Enviar clave pública a Alice
            objectOutputStream.writeObject(publicKey);
            objectOutputStream.flush();
            System.out.println("Clave pública de Bob enviada a Alice.");

            // Recibir clave pública de Alice
            PublicKey alicePublicKey = (PublicKey) objectInputStream.readObject();
            if (!Utilidades.validatePublicKey(alicePublicKey)) {
                throw new IllegalArgumentException("Clave pública recibida es inválida");
            }
            System.out.println("Clave pública de Alice recibida y validada.");

            // Generar la clave compartida
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(privateKey);
            keyAgree.doPhase(alicePublicKey, true);
            byte[] sharedSecret = keyAgree.generateSecret();
            
            // Calcular hash SHA-256 de la clave compartida
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            byte[] first16Bytes = Arrays.copyOf(sharedSecretHash, 16);
            System.out.println("Clave compartida hash (Bob): " + Utilidades.bytesToHex(sharedSecretHash));

            // Guarda el hash en un archivo TXT
            String fileName = "hasht.txt";
            Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);
            // Files.writeString(Paths.get(fileName), bytesToHex(first16Bytes), StandardOpenOption.CREATE);
        }
    }
}
