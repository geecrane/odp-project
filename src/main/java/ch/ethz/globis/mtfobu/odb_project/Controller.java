package ch.ethz.globis.mtfobu.odb_project;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
 
public class Controller {
    
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

}