package ch.ethz.globis.mtfobu.odb_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
 
public class Controller {
    
    @FXML protected void handleDeletePersonAction(ActionEvent event) {
    	System.out.println("Hello World!");
        String dbName = "ExampleDB.zdb";
        DBExample.createDB(dbName);
        DBExample.populateDB(dbName);
        DBExample.readDB(dbName);
    }

}