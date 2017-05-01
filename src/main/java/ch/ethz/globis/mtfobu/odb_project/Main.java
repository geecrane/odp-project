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

		Database db = Database.getDatabase();
		// comment out the line below, if you don't want to import the xml every
		// time.
		 //tempXMLImport(db);
//		InProceedings p = db.getInProceedingsById("conf/kivs/Wilhelm83");
//		System.out.println(p.getTitle());
//		
//		List<Publication> pubs = db.getPublicationsByFilter("Process", 0, 5);
//		for (Publication pub:pubs){
//			System.out.println(pub.getTitle());
//		}
//		System.out.println("test");
//		Publisher pub = db.getPublisherByName("Springer");
//		System.out.println(pub.getName());
//		List<Person> people = db.getPeople();
//		for(Person per: people){
//			System.out.println(per.getName());
//		}
		// task 13
		List<InProceedings> inProcs = db.getPublicationsWhereAuthorIsLast("Antónia Lopes");
		for(InProceedings inProc: inProcs){
			System.out.println(inProc.getTitle());
		}
		// task 12
		List<Person> pers = db.getPeopleThatAreAuthorsAndEditors();
		for(Person per: pers){
			System.out.println(per.getName());
		}
//		Set<Publication> pubs2 = db.getAuthoredPublications("Sanjay Sharma", true);
//		for(Publication pub:pubs2){
//			if(((InProceedings)pub).getProceedings() != null) System.out.println(((InProceedings)pub).getProceedings().getTitle());
//		}
//		// task 11
//		List<Publication> pubs = db.getAllPublicationsOfConferenceByName("BMVC");
//		for( Publication pub: pubs){
//			System.out.println(pub.getTitle());
//		}
//		// task 10
//		List<Person> pers = db.getAllAuthorsOfConferenceByName("BMVC");
//		for(Person per: pers){
//			System.out.println(per.getName());
//		}
//	
//		// task 9
//		System.out.println(db.countEditorsAndAuthorsOfConferenceByName("BMVC"));
//		//Test task 7
//		List<String> res = db.getNumberPublicationsPerYearInterval(1900, 1990);
//		for(String str: res){
//			System.out.println(str);
//		}
//		
//		InProceedings inproc = db.getInProceedingsById("conf/ifip5-7/Jagdev88a");
//		System.out.println(inproc.getProceedings().getTitle());
//		List<ch.ethz.globis.mtfobu.odb_project.Conference> confs = db.getConferences();
//		for(ch.ethz.globis.mtfobu.odb_project.Conference per: confs){
//			System.out.println(per.getName());
//		}
//		List<Publisher> confs2 = db.getPublishers();
//		for(Publisher per: confs2){
//			System.out.println(per.getName());
//		}
//		List<Person> coAuthors = db.getCoAuthors("Peter Buneman");
//		for (Person coAuthor: coAuthors){
//			System.out.println(coAuthor.getName());
//		}

//		// print all inproceedings where author appears last, given author id
//		List<InProceedings> inProceedingsList = db.getInproceedingsAuthorLast("1785178126");
//		for (InProceedings inProceedings : inProceedingsList) {
//			System.out.println(inProceedings.getTitle());
//		}
//		// print all inproceedings where author appears last, given conference
//		// number
//		List<InProceedings> inProceedingsList2 = db.allTasksFromPublication("GI Jahrestagung (1)");
//		for (InProceedings inProceedings : inProceedingsList2) {
//			System.out.println(inProceedings.getTitle());
//		}
//		List<Person> people2 = db.allAuthorsOfConference("GI Jahrestagung (1)");
//		for (Person inProceedings : people2) {
//			System.out.println(inProceedings.getName());
//		}
//		//
//		System.out.println(db.countEditors("GI Jahrestagung (1)"));
//		// db.noPublicationsPerYear(0, 2013);
//
//		System.out.println(db.globalAvgAuthors());
//
//		List<Person> people = db.getCoAuthores("1785178126");
//		for (Person inProceedings : people) {
//			System.out.println(inProceedings.getName());
//		}
//		// please don't try to enter a wrong ID. Otherwise the program will run
//		// for quite some time
//
//		System.out.println(db.authorDistance("1785178126", "1107451538"));
//		// uncomment line below to enable the GUI
//
// launch(args);
//		System.out.println("PROGRAMM TERMINATED");

	}

//	public void provisionallyFunction(String Query) throws IOException {
//		// format: taskN;param;param;param...
//		
//		Database db = new Database(Config.DATABASE_NAME);
//		String[] params = Query.split(";");
//		if (params.length > 0) {
//			String task = params[0];
//			switch (task) {
//			case "task1":
//				if (params.length<2) System.out.println("Task1: Query does not have the right format");
//				Publication pub;
//				pub = db.getPublicationById(params[1]);
//				break;
//			case "task2":
//				if (params.length<4) System.out.println("Task2: Query does not have the right format");
//				Collection<Publication> pubs;
//				QueryParameters qp1 = new QueryParameters();
//				qp1.searchTerm = params[1];
//				qp1.isRanged = true;
//				qp1.rangeStart = OptionalLong.of(Long.parseLong(params[2]));
//				qp1.rangeEnd = OptionalLong.of(Long.parseLong(params[3]));
//				pubs = db.queryForPublications(qp1);
//				break;
//			case "task3":
//				if (params.length<4) System.out.println("Task3: Query does not have the right format");
//				Collection<Publication> pubs2;
//				QueryParameters qp2 = new QueryParameters();
//				qp2.searchTerm = params[1];
//				qp2.isRanged = true;
//				qp2.rangeStart = OptionalLong.of(Long.parseLong(params[2]));
//				qp2.rangeEnd = OptionalLong.of(Long.parseLong(params[3]));
//				pubs2 = db.queryForPublications(qp2);
//				break;
//			case "task4":
//				if (params.length<2) System.out.println("Task4: Query does not have the right format");
//				List<Person> coAuthors;
//				coAuthors = db.getCoAuthores(db.getPersonIdFromName(params[1]));
//				break;
//			case "task5":
//				if (params.length<3) System.out.println("Task5: Query does not have the right format");
//				int distance;
//				distance = db.authorDistance(db.getPersonIdFromName(params[1]), db.getPersonIdFromName(params[2]));
//				break;
//			case "task6":
//				double avg;
//				avg = db.globalAvgAuthors();
//				break;
//			case "task7":
//				if (params.length<3) System.out.println("Task7: Query does not have the right format");
//				HashMap<Integer,Integer> result;
//				result = db.noPublicationsPerYear(Integer.parseInt(params[1]), Integer.parseInt(params[2]));
//				break;
//			case "task8":
//				HashMap<String,Integer> result1;
//				result1 = db.noPublicationsPerConference();
//				break;
//			case "task9":
//				if (params.length<2) System.out.println("Task9: Query does not have the right format");
//				int numberEditors;
//				numberEditors = db.countEditors(db.getConferenceIdFromName(params[1]));
//				break;
//			case "task10":
//				if (params.length<2) System.out.println("Task9: Query does not have the right format");
//				List<Person> allAuthors;
//				allAuthors = db.allAuthorsOfConference(db.getConferenceIdFromName(params[1]));
//				break;
//			case "task11":
//				if (params.length<2) System.out.println("Task9: Query does not have the right format");
//				List<InProceedings> allTasks;
//				allTasks = db.allTasksFromPublication(db.getConferenceIdFromName(params[1]));
//				break;
//			case "task13":
//				if (params.length<2) System.out.println("Task9: Query does not have the right format");
//				List<InProceedings> inProcs;
//				inProcs = db.getInproceedingsAuthorLast(db.getPersonIdFromName(params[1]));
//				break;
//
//			default:
//				System.out.println("This task is not known");
//				break;
//			}
//		}

//	}

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
