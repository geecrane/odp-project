package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.jdo.PersistenceManager;

import org.zoodb.jdo.ZooJdoHelper;

import ch.ethz.globis.mtfobu.odb_project.Conference;
import ch.ethz.globis.mtfobu.odb_project.ConferenceEdition;
import ch.ethz.globis.mtfobu.odb_project.Config;
import ch.ethz.globis.mtfobu.odb_project.Database;
import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Person;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.Publisher;
import ch.ethz.globis.mtfobu.odb_project.Series;
import ch.ethz.globis.mtfobu.odb_project.XmlImport;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

 
public class Controller {
	public final int PAGE_SIZE = 20;
	public PersistenceManager pm;
    public Database db;
	
    public void initialize() {
    	db = Database.getDatabase();
    	List<InProceedings> inProcs = db.getInProceedings();
    	
    	ObservableList<InProceedings> masterData = FXCollections.observableArrayList();
    	masterData.addAll(inProcs);
    	
    	// Initialize Columns
    	inProceedingMainTableTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
    	inProceedingMainTableYearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
    	inProceedingMainTableProceedingsColumn.setCellValueFactory(cellData -> {
    		Proceedings proc = cellData.getValue().getProceedings();
    		String procTitle = "";
    		if(proc != null) {procTitle = cellData.getValue().getProceedings().getTitle();}
    		return new SimpleStringProperty(procTitle);
    				});
    	
    	// Wrap the ObservableList in a FilteredList. Display all data (unfiltered)
    	FilteredList<InProceedings> filteredData = new FilteredList<>(masterData, inProc -> true);
    	
    	// Set the filter Predicate whenever the filter changes.
    	inProceedingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(inProc -> {
                // If filter text is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (inProc.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches title.
                } else if (String.valueOf(inProc.getYear()).contains(lowerCaseFilter)) {
                    return true; // Filter matches year.
                } else if(inProc.getProceedings() != null && inProc.getProceedings().getTitle().toLowerCase().contains(lowerCaseFilter)){
                	return true; // Filter matches Proceedings
                }
                return false; // Does not match.
            });
        });
    	
    	// Wrap the FilteredList in a SortedList. 
    	SortedList<InProceedings> sortedData = new SortedList<>(filteredData);
    	
    	//Bind the SortedList comparator to the TableView comparator
    	sortedData.comparatorProperty().bind(inProceedingMainTable.comparatorProperty());
    	// Add sorted (and filtered) data to the table.
    	inProceedingMainTable.setItems(sortedData);
    }
 
    // START section for person tab
    
	// END section for person tab
	
	
	// START section for proceeding tab
	
	// END section for proceeding tab
	
	
	// START section for inproceeding tab

	// END section for inproceeding tab
	
	
	// START section for publication tab

	// End section for publication tab
	
	
	// START section for publisher tab

	// End section for publisher tab
	
	
	// START section for conferences tab

	// END section for conferences tab
	
	
	// START section for conference editions tab	

	// END section for conference editions tab
	
	
	// START section for series tab

	// END section for series tab
    
    
    
    
    // START section for table entry data types

    // END section for table entry data types
    
    
    
    
    
    // START section for fields that reference FXML
    @FXML TabPane tabPane;

    //Start Inproceedings
    @FXML   Tab inProceedingTab;
    @FXML   private TableView<InProceedings> inProceedingMainTable;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableTitleColumn;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableYearColumn;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableProceedingsColumn;
    
    private TableColumn<Person, String> firstNameColumn;
    @FXML    private TextField inProceedingSearchField;
    @FXML    private Button inProceedingSearchButton;
    @FXML    private Button inProceedingDeleteButton;
    @FXML    private Button inProceedingNextPageButton;
    @FXML    private Button inProceedingPreviousPageButton;
    @FXML    private TextField inProceedingCurrentPageField;
    @FXML    private Button inProceedingCreateButton;
    @FXML    private TextField inProceedingPagesField;
    @FXML    private Button inProceedingChangePagesButton;
    @FXML    private TextField inProceedingProceedingFilterField;
    @FXML    private ChoiceBox<?> inProceedingProceedingDropdown;
    @FXML    private Button inProceedingChangeProceedingButton;
    @FXML    private ChoiceBox<?> inProceedingAuthorDropdown;
    @FXML    private Button inProceedingAddAuthorButton;
    @FXML    private TextField inProceedingAuthorFilterField;
    //@FXML    private TableView<SecondaryPersonTableEntry> inProceedingAuthorTable;
    @FXML    private Button inProceedingRemoveAuthorButton;
    @FXML    private TextField inProceedingTitleField;
    @FXML    private Button inProceedingChangeTitleButton;
    @FXML    private TextField inProceedingYearField;
    @FXML    private Button inProceedingChangeYearButton;
    
    
    // END section for fields that reference FXML
}