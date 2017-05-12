package ch.ethz.globis.mtfobu.odb_project;

import java.io.IOException;
import java.net.URL;
import java.security.Permissions;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import ch.ethz.globis.mtfobu.odb_project.zoodb.Conference;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) throws IOException {

		DatabaseBaseX db = DatabaseBaseX.getDatabase();
		db.closeDB();
//		launch(args);
		System.out.println("PROGRAMM TERMINATED");

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		FXMLLoader loader = new FXMLLoader();
		URL url = getClass().getResource("/first-prototype.fxml");
		loader.setLocation(url);
		Parent root = loader.load();

		Scene scene = new Scene(root, 1024, 786);

		primaryStage.setTitle("FXML based prototype");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

}
