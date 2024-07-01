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
            System.out.println("Parámetros Diffie-Hellman generados");
        }

        public byte[] exchangeKeys() throws Exception {
            System.out.println("Compartiendo parámetros Diffie-Hellman");

            // Enviar parámetros Diffie-Hellman a Alice
            Servidor.objectOutputStream.writeObject(this.dhSpec.getP());
            Servidor.objectOutputStream.flush();
            Servidor.objectOutputStream.writeObject(this.dhSpec.getG());
            Servidor.objectOutputStream.flush();
            Servidor.objectOutputStream.writeInt(this.dhSpec.getL());
            Servidor.objectOutputStream.flush();
            System.out.println("Parámetros Diffie-Hellman enviados a Alice.");

            // Enviar clave pública a Alice
            Servidor.objectOutputStream.writeObject(this.publicKey);
            Servidor.objectOutputStream.flush();
            System.out.println("Clave pública de Bob enviada a Alice.");

            // Recibir clave pública de Alice
            PublicKey alicePublicKey = (PublicKey) Servidor.objectInputStream.readObject();
            if (!Utilidades.validatePublicKey(alicePublicKey)) {
                throw new IllegalArgumentException("Clave pública recibida es inválida");
            }
            System.out.println("Clave pública de Alice recibida y validada.");

            // Generar la clave compartida
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(this.privateKey);
            keyAgree.doPhase(alicePublicKey, true);
            byte[] sharedSecret = keyAgree.generateSecret();
            
            // Calcular hash SHA-256 de la clave compartida
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            byte[] first16Bytes = Arrays.copyOf(sharedSecretHash, 16);
            System.out.println("Clave compartida hash (Bob): " + Utilidades.bytesToHex(sharedSecretHash));

            // Guarda el hash en un archivo TXT
            String fileName = "DHAESKEY.bin";
            Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);
            // Files.writeString(Paths.get(fileName), bytesToHex(first16Bytes), StandardOpenOption.CREATE);

            return first16Bytes;
        }
    }
}
