package ch.ethz.globis.mtfobu.odb_project;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {

		Database db = new Database(Config.DATABASE_NAME);
		// comment out the line below, if you don't want to import the xml every
		// time.
		// tempXMLImport(db);
		Proceedings p = db.getProceedingsById("conf/bcshci/1988");
		System.out.println(p.getTitle());

		// print all inproceedings where author appears last, given author id
		List<InProceedings> inProceedingsList = db.getInproceedingsAuthorLast("1785178126");
		for (InProceedings inProceedings : inProceedingsList) {
			System.out.println(inProceedings.getTitle());
		}
		// print all inproceedings where author appears last, given conference
		// number
		List<InProceedings> inProceedingsList2 = db.allTasksFromPublication("GI Jahrestagung (1)");
		for (InProceedings inProceedings : inProceedingsList2) {
			System.out.println(inProceedings.getTitle());
		}
		List<Person> people2 = db.allAuthorsOfConference("GI Jahrestagung (1)");
		for (Person inProceedings : people2) {
			System.out.println(inProceedings.getName());
		}
		//
		System.out.println(db.countEditors("GI Jahrestagung (1)"));
		// db.noPublicationsPerYear(0, 2013);

		System.out.println(db.globalAvgAuthors());

		List<Person> people = db.getCoAuthores("1785178126");
		for (Person inProceedings : people) {
			System.out.println(inProceedings.getName());
		}
		// please don't try to enter a wrong ID. Otherwise the program will run
		// for quite some time

		System.out.println(db.authorDistance("1785178126", "1107451538"));
		// uncomment line below to enable the GUI

		// launch(args);
		System.out.println("PROGRAMM TERMINATED");

	}

	public void provisionallyFunction(String Query) {
		// format: taskN;param;param;param...
		
		Database db = new Database(Config.DATABASE_NAME);
		String[] params = Query.split(";");
		if (params.length > 0) {
			String task = params[0];
			switch (task) {
			case "task1":
				if (params.length<2) System.out.println("Task1: Query does not have the right format");
				Publication pub;
				pub = db.getPublicationById(params[1]);
				break;
			case "task2":
				QueryParameters qp = new QueryParameters();
				qp.searchTerm = params[1];
				qp.isRanged = true;
			//unsuable	//qp.rangeStart = new OptionalLong(Long.parseLong(params[2]));
				break;
			case "task3":
				QueryParameters qp = new QueryParameters();
				qp.searchTerm = params[1];
				qp.isRanged = true;
				OptionalLong ol;
				qp.rangeStart = ol.of(Long.parseLong(params[2]));
				break;
				

			default:
				break;
			}
		}

	}

	// temporary function. just for testing until the gui works
	public static void tempXMLImport(Database db) {

		// delete db if exists already
		db.create();

		XmlImport importer = new XmlImport(db, null);
		importer.ImportFromXML("src/main/resources/dblp_filtered.xml");
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
