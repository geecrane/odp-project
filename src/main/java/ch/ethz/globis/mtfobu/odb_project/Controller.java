package ch.ethz.globis.mtfobu.odb_project;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
 
public class Controller {
	@FXML
	public Button btnImport;
	
	@FXML
	public Label lblStatus;

	
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
		
    }
	
	public void loadPersonMainTable () {
		ObservableList columns = personMainTable.getColumns();
		columns.get(0);
	}
	
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