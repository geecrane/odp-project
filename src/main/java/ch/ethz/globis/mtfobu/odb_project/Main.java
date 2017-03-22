package ch.ethz.globis.mtfobu.odb_project;

import java.net.URL;
import java.util.Collection;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

public class Main extends Application{

	public static void main(String[] args) {
        
        launch(args);
        
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		URL url = getClass().getResource("/first-prototype.fxml");
		loader.setLocation(url);
		Parent root = loader.load(); 
	    
		
        Scene scene = new Scene (root, 1024, 786);
        
        
        primaryStage.setTitle("FXML based prototype");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        //George: only for testing purposes
        //of course will have to run on a separate thread!
        Database db = new Database(Config.DATABASE_NAME);
        db.create();
        XmlImport xim = new XmlImport(db);
        xim.ImportFromXML("src/main/resources/dblp_filtered.xml");
		
		
        
        
        /*
         
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
                String dbName = "ExampleDB.zdb";
                createDB(dbName);
                populateDB(dbName);
                readDB(dbName);
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
        */
        
		
	}
	

}
