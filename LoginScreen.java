import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        Label userLabel = new Label("Usuario:");
        Label passwordLabel = new Label("Contraseña:");
        TextField userField = new TextField();
        TextField passwordField = new TextField();
        Button loginButton = new Button("Iniciar sesión");

        loginButton.setOnAction(e -> {
            // Aquí podrías validar las credenciales
            String usuario = userField.getText();
            String contraseña = passwordField.getText();
            if (validarUsuario(usuario, contraseña)) {
                servidor.showMainInterface(primaryStage); // Llama a la interfaz principal después de la autenticación
            }
        });

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private boolean validarUsuario(String usuario, String contraseña) {
        // Implementa tu lógica de validación aquí
        return true; // Solo como ejemplo
    }
}
