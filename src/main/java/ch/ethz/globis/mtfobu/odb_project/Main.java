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

import benchmark.Benchmark;
import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseBaseX;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseMongoDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) throws IOException {
		
		Benchmark bch = new Benchmark(DatabaseMongoDB.getDatabase());
		try {
			//bch.benchmark();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		launch(args);
		
		//for testing purposes
//		Database db = new DatabaseMongoDB();
//		
//		Proceedings proc = new Proceedings();
//		InProceedings inProc = new InProceedings("/conf/some/id");
//		inProc.setProceedings(new Proceedings("conf/hmi/1987"));
//		inProc.setNote("Published");
//		inProc.setPages("45-67");
//		proc.setTitle("some title");
//		
//		List<String> cvs = db.validateInProceedings(inProc);
//		for( String cv: cvs){
//			System.out.println(cv);
//		}
		
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
