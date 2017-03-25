package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
 
public class Controller {
	@FXML
	public Button btnImport;
	
	@FXML
	public Label lblStatus;

	
	public PersistenceManager pm;
	
    public void initialize() {
    	
		btnImport.setOnAction((event) -> {
			//George: only for testing purposes
	        //of course will have to run on a separate thread!
 		    
			
			final Controller parameter = this; 
			Thread t = new Thread(new Runnable() {
				 Controller c = parameter;
		         public void run()
		         {
		        	c.btnImport.setDisable(true);
		        	
		        	Database db = new Database(Config.DATABASE_NAME);
		 	        db.create();
		 	        XmlImport xim = new XmlImport(db, c);
		 	        xim.ImportFromXML("src/main/resources/dblp_filtered.xml");
		 	        c.btnImport.setDisable(false);
		         }
			});
			
			t.start();
	        
		});
		
		tabPeople.setOnSelectionChanged(new EventHandler<Event>() {
	        @Override
	        public void handle(Event t) {
	            if (tabPeople.isSelected()) {
	            	System.out.println("tabPeople was selected");
	            	loadPersonMainTable();
	            }
	        }
	    });
		
    }
    
    
	
	public void loadPersonMainTable () {
		pm = ZooJdoHelper.openDB(Config.DATABASE_NAME);
		pm.currentTransaction().begin();
        
        System.out.println("Queries: ");
        Query query = pm.newQuery(Person.class);
        Collection<Person> people = (Collection<Person>) query.execute();
        for (Person p: people) {
        	System.out.println("Person found: " + p.getName());
        }
        query.closeAll();
        
        pm.currentTransaction().commit();
        
        ObservableList<Person> observableList = FXCollections.observableArrayList();
        observableList.addAll(people);
		
		ObservableList<TableColumn> columns = personMainTable.getColumns();
		TableColumn<Person,String> nameCol = (TableColumn<Person,String>) columns.get(0);
		TableColumn<Person,String> authEditCol = (TableColumn<Person,String>) columns.get(1);
		
		nameCol.setCellValueFactory(new Callback<CellDataFeatures<Person, String>, ObservableValue<String>>() {
		     public ObservableValue<String> call(CellDataFeatures<Person, String> p) {
		         return new ReadOnlyObjectWrapper<String>(p.getValue().getName());
		     }
		  });
		
		personMainTable.setItems(observableList);
		
	}
	
	@FXML TabPane tabPane;
	
	@FXML Tab tabPeople;
	@FXML Tab tabProceedings; 
	@FXML Tab tabIProceedings;
	@FXML Tab tabPublications;
	@FXML Tab tabPublishers;
	@FXML Tab tabConferences;
	@FXML Tab tabConferenceEditions;
	@FXML Tab tabSeries;
	
	@FXML TableView personMainTable;
	@FXML TableView proceedingMainTable; 
	@FXML TableView inProceedingMainTable;
	@FXML TableView publicationMainTable;
	@FXML TableView publisherMainTable;
	@FXML TableView conferenceMainTable;
	@FXML TableView conferenceEditionMainTable;
	@FXML TableView serieMainTable;
	
    @FXML protected void handleDeletePersonAction(ActionEvent event) {
    	System.out.println("Hello World!");
        String dbName = "ExampleDB.zdb";
        DBExample.createDB(dbName);
        DBExample.populateDB(dbName);
        DBExample.readDB(dbName);
    }
    
    protected void onImport(ActionEvent event){
    	System.out.printf("Test");
    }
    

}