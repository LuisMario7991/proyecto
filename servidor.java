import javax.swing.JFileChooser;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.*;
import javax.crypto.spec.*;


public class servidor{
    public static void show(Stage secondStage) {
        secondStage.setTitle("File Sharing App");
        // Botones
        Button subirButton = new Button("Subir");
        Button compartirButton = new Button("Compartir");
        Button validarButton = new Button("Validar");
        Button agregarButton = new Button("Agregar usuario");
        Button eliminarButton = new Button("Eliminar usuario");
        


        // Acciones de los botones
        subirButton.setOnAction(e -> {
             String connectStr = "DefaultEndpointsProtocol=https;AccountName=criptografia;AccountKey=ZqM6hU8KHAPBja0nWQ5YaiAC12vJqGv44R4HEytD5UwoLBpupGwQT2SAGVetp9kqPz04F+6M0WGW+ASt499lYQ==;EndpointSuffix=core.windows.net";
        String containerName = "recetas";

        // Crear un selector de archivos usando JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Subir el archivo cifrado al contenedor en Azure Blob Storage
          
            BlobUploader blobUploader = new BlobUploader(connectStr, containerName);
            blobUploader.uploadEncryptedBlob(selectedFile);
            System.out.println("Subiendo archivo...");
        }
         });

        compartirButton.setOnAction(e -> {
            // Lógica para compartir archivo
            System.out.println("Compartiendo archivo...");
            // Aquí se debería implementar la lógica para enviar el archivo al cliente mediante sockets
            // Ejemplo básico:
            // enviarArchivoAlCliente();
        });

        validarButton.setOnAction(e -> {
              //Aplicar hash SHA-256 al archivo 'm.txt'
                    //byte[] fileHash = hashFile("received_m.txt");
                    //PublicKey publicKey = getPublicKeyFromFile("receivedPublicKey.pem");

                    //byte[] decryptedHash = decryptWithPublicKey("received_encrypted_hash.bin", publicKey);

                    // Aplicar hash SHA-256 al resultado del descifrado
                    //byte[] decryptedHashFileHash = hashBytes(decryptedHash);

                    // Comparar los hashes
                    //boolean isMatch = MessageDigest.isEqual(fileHash, decryptedHashFileHash);
                    //System.out.println("Hash comparison result: " + (isMatch ? "MATCH" : "DO NOT MATCH"));;
            System.out.println("Validando archivo...");
        });

        agregarButton.setOnAction(e -> {
           
            System.out.println("agregar usuario");
            
            // agregar el usuario en la base de datos
        });
        eliminarButton.setOnAction(e -> {
           
            System.out.println("Compartiendo archivo...");
           // eliminar el usuario en la base de datos
        });

         

        // Layout
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(subirButton, compartirButton, validarButton);

        Scene scene = new Scene(vbox, 300, 200);
        secondStage.setScene(scene);
        secondStage.show();
    }

    public static void main(String[] args) throws Exception {
        
       
        final int PORT = 12345;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            System.out.println("Bob está esperando conexiones en el puerto " + PORT);
          
            // Aceptar conexión del cliente (Alice)
            
            while (true) {
                // Aceptar conexión del cliente (Alice)
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress());
               
                // Crear un nuevo hilo para manejar la conexión con el cliente
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
                DHKeyExchange.ServerDH dhKeyExchange = new DHKeyExchange.ServerDH();
                dhKeyExchange.exchangeKeys(socket);
              
                // Leer datos del cliente y escribir a un archivo
                recibirArchivo(socket);
                        System.out.println("Archivo recibido y guardado.");
                
            }    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recibirArchivo(Socket socket) {
            try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
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
    
                    bufferedOutputStream.flush(); // Asegurar que todos los datos han sido escritos
    
                    if (totalBytesRead == fileSize) {
                        System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);
                       
                    } else {
                        System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    

   

    private static byte[] hashBytes(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    private static byte[] hashFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return digest.digest(fileBytes);
    }

     private static PublicKey getPublicKeyFromFile(String filePath) throws Exception {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static byte[] decryptWithPublicKey(String filePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        return cipher.doFinal(encryptedData);
    }
    
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }


        @Override
        public void run() {
       
        }
    }
}
  