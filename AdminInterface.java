import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminInterface {

    private static Commands command = new Commands();

    public static void showAdminInterface(Stage primaryStage) {
        primaryStage.setTitle("Administrador");
        Button subirButton = new Button("Subir");
        Button compartirButton = new Button("Compartir");
        Button validarButton = new Button("Validar");
        Button agregarButton = new Button("Agregar usuario");
        Button eliminarButton = new Button("Eliminar usuario");
        Button salirButton = new Button("Salir");

        subirButton.setOnAction(e -> subirArchivo());
        compartirButton.setOnAction(e -> compartirArchivo());
        validarButton.setOnAction(e -> validarArchivo());
        agregarButton.setOnAction(e -> agregaUsuario());
        eliminarButton.setOnAction(e -> eliminaUsuario());
        salirButton.setOnAction(e -> salir(primaryStage));

        VBox vbox = new VBox(10, subirButton, compartirButton, validarButton, agregarButton, eliminarButton,
                salirButton);
        Scene scene = new Scene(vbox, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static void subirArchivo() {
        // Crear un selector de archivos usando JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {

                Servidor.dataOutputStream.writeUTF(command.getUploadAzure());
                File selectedFile = fileChooser.getSelectedFile();

                FileInputStream fileInputStream = new FileInputStream(selectedFile);

                long fileSize = selectedFile.length();
                String fileName = selectedFile.getName();

                // Servidor.dataOutputStream.writeUTF(command.getUploadFile());
                // Servidor.dataOutputStream.flush();

                // Enviar el nombre del archivo y su tamaño
                Servidor.dataOutputStream.writeUTF(fileName);
                Servidor.dataOutputStream.writeLong(fileSize);
                Servidor.dataOutputStream.flush();

                System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    Servidor.dataOutputStream.write(buffer, 0, bytesRead);
                    Servidor.dataOutputStream.flush();
                }

                fileInputStream.close();

                System.out.println("Archivo " + fileName + " enviado al servidor.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void compartirArchivo() {
        // Lógica para compartir archivo
        try {
            System.out.println("Compartiendo archivo...");

            Servidor.dataOutputStream.writeUTF(command.getShareFile());

            // Crear una ventana para ingresar datos del usuario
            Stage stage = new Stage();
            GridPane grid = new GridPane();
            TextField fileField = new TextField();
            Button sendButton = new Button("Agregar");

            grid.add(new Label("Nombre del archivo:"), 0, 0);
            grid.add(fileField, 1, 0);
            grid.add(sendButton, 1, 1);

            sendButton.setOnAction(e -> {
                try {
                    Servidor.dataOutputStream.writeUTF(fileField.getText());

                    // Enviar el nombre del archivo y su tamaño
                    String fileName = Servidor.dataInputStream.readUTF();
                    long fileSize = Servidor.dataInputStream.readLong();

                    byte[] fileData = new byte[(int) fileSize];
                    Servidor.dataInputStream.readFully(fileData);

                    // Guardar los bytes en un archivo local
                    try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                        fileOutputStream.write(fileData);
                        System.out.println("Archivo guardado: " + fileName + " (" + fileSize + " bytes)");
                    }

                    stage.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

            Scene scene = new Scene(grid);
            stage.setScene(scene);
            stage.setTitle("Validación de Acuerdo");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validarArchivo() {
        try {

            System.out.println("Validando archivo en el serivdor");

            // Comparar los hashes
            boolean isMatch = FileManagement.validarArchivo();
            String validationText = (isMatch) ? "Validación exitosa:" : "Validación fallida:";
            String messageText = (isMatch) ? "El acuerdo fue verificado y cumple con los requisitos."
                    : "El acuerdo no coincide. Pida que firmen el acuerdo nuevamente.";

            // Crear una ventana para ingresar datos del usuario
            Stage stage = new Stage();
            GridPane grid = new GridPane();
            Button acceptButton = new Button("Aceptar");

            grid.add(new Label("               "), 1, 0);
            grid.add(new Label("        "), 0, 1);
            grid.add(new Label(validationText), 1, 1);
            grid.add(new Label("        "), 2, 1);
            grid.add(new Label("        "), 0, 2);
            grid.add(new Label(messageText), 1, 2);
            grid.add(new Label("        "), 2, 2);
            grid.add(new Label("               "), 1, 3);
            grid.add(acceptButton, 1, 4);
            grid.add(new Label("               "), 1, 5);

            acceptButton.setOnAction(e -> stage.close());

            Scene scene = new Scene(grid);
            stage.setScene(scene);
            stage.setTitle("Validación de Acuerdo");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void agregaUsuario() {
        // Crear una ventana para ingresar datos del usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("administrador", "colaborador");
        Button addButton = new Button("Agregar");

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Tipo de Usuario:"), 0, 2);
        grid.add(typeComboBox, 1, 2);
        grid.add(addButton, 1, 3);

        addButton.setOnAction(e -> {
            UserManagement.agregarUsuario(emailField.getText(), passwordField.getText(), typeComboBox.getValue());
            stage.close(); // Cierra la ventana tras la respuesta
        });

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private static void eliminaUsuario() {
        // Crear una ventana para eliminar un usuario
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        TextField emailField = new TextField();
        Button deleteButton = new Button("Eliminar");

        grid.add(new Label("Email del usuario a eliminar:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(deleteButton, 1, 1);

        deleteButton.setOnAction(e -> {
            UserManagement.eliminarUsuario(emailField.getText());
            stage.close();
        }); // Cierra la ventana tras la respuesta

        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    private static void salir(Stage stage) {
        Servidor.cerrarConexion();
        stage.close();
    }
}
