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
import ch.ethz.globis.mtfobu.odb_project.DomainObject;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

 
public class Controller {
	public final int PAGE_SIZE = 20;
	public PersistenceManager pm;
    public Database db;
    
    public void initialize() {
    	db = Database.getDatabase();
    	
    	//initializeInproceedingsMainColumns();	
    	//loadInProceedings();
    	
    	initializePeopleMainColumns();
//    	loadPeople();
    	
    	
    }

	
	
	//START people tab
	private void loadPeople() {
		//init table data
		List<Person> inProcs = db.getPeople();
    	ObservableList<Person> masterData = FXCollections.observableArrayList();
    	masterData.addAll(inProcs);
    	FilteredList<Person> filteredData = setTable(masterData,personMainTable);
    	
    	//search field
    	searchSetupPeople(filteredData);  
	}
	private void initializePeopleMainColumns() {
		// Initialize InProceedings Columns
		personMainTableNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    	
	}
	private void searchSetupPeople(FilteredList<Person> filteredData) {
		
		// Set the filter Predicate whenever the filter changes.
		personSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(p -> {
                // If filter text is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if (p.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches title.
                }
                return false; // Does not match.
            });
        });
	}
	//END people tab
	
	
	//START InProceedings tab
	private void loadInProceedings() {
		//init table data
		List<InProceedings> inProcs = db.getInProceedings();
    	ObservableList<InProceedings> masterData = FXCollections.observableArrayList();
    	masterData.addAll(inProcs);
    	FilteredList<InProceedings> filteredData = setTable(masterData,inProceedingMainTable);
    	
    	//search field
    	searchSetupInproceedings(filteredData);  
	}
	private void searchSetupInproceedings(FilteredList<InProceedings> filteredData) {
		
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
	}
	private void initializeInproceedingsMainColumns() {
		// Initialize InProceedings Columns
		inProceedingMainTableTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
    	inProceedingMainTableYearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
    	inProceedingMainTableProceedingsColumn.setCellValueFactory(cellData -> {
    		Proceedings proc = cellData.getValue().getProceedings();
    		String procTitle = "";
    		if(proc != null) {procTitle = cellData.getValue().getProceedings().getTitle();}
    		return new SimpleStringProperty(procTitle);
    				});
    	
    	//initialize authors column
    	inProceedingAuthorTableNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
    	
	}
	@FXML
	public void inProceedingsMainClickItem(MouseEvent event)
	{
	    if (event.getClickCount() == 2) //Checking double click
	    {
	    	
	    	//initialize data
	    	InProceedings selected = inProceedingMainTable.getSelectionModel().getSelectedItem();
	    	List<Person> authors = selected.getAuthors();
	    	ObservableList<Person> masterData = FXCollections.observableArrayList();
	    	masterData.addAll(authors);
	    	
	    	//load table
	    	FilteredList<Person> filteredData = setTable(masterData,inProceedingAuthorTable);
	    	
	    	//editable fields
	    	inProceedingTitleField.setText(selected.getTitle());
	    	inProceedingPagesField.setText(selected.getPages());
	    	inProceedingYearField.setText(String.valueOf(selected.getYear()));
	    	
	    }
	}
	//END InProceedings tab
	
	private <P extends DomainObject> FilteredList<P> setTable(ObservableList<P> masterData, TableView<P> tableView) {
    	// Wrap the ObservableList in a FilteredList. Display all data (unfiltered)
    	FilteredList<P> filteredData = new FilteredList<>(masterData, p -> true);

    	// Wrap the FilteredList in a SortedList. 
    	SortedList<P> sortedData = new SortedList<>(filteredData);
    	
    	//Bind the SortedList comparator to the TableView comparator
    	sortedData.comparatorProperty().bind(tableView.comparatorProperty());
    	
    	// Add sorted (and filtered) data to the table.
    	tableView.setItems(sortedData);
		return filteredData;
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
    
    //Start People
    @FXML   Tab personTab;
    @FXML   private TableView<Person> personMainTable;
    @FXML	private TableColumn<Person, String> personMainTableNameColumn;
    @FXML   private TextField personSearchField;
    
    //End People

    //Start Inproceedings
    @FXML   Tab inProceedingTab;
    @FXML   private TableView<InProceedings> inProceedingMainTable;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableTitleColumn;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableYearColumn;
    @FXML	private TableColumn<InProceedings, String> inProceedingMainTableProceedingsColumn;
    
    @FXML	private TableView<Person> inProceedingAuthorTable;
    @FXML	private TableColumn<Person, String> inProceedingAuthorTableNameColumn;
    
    @FXML    private TextField inProceedingSearchField;
    @FXML    private Button inProceedingSearchButton;//TODO remove this
    @FXML    private Button inProceedingDeleteButton;
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