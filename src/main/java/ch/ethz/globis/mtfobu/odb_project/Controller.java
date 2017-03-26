package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
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

	
	public PersistenceManager pm;
	
    public void initialize() {
    	pm = ZooJdoHelper.openDB(Config.DATABASE_NAME);
    	
    	
    	importButton.setOnAction((event) -> {
			//George: only for testing purposes
	        //of course will have to run on a separate thread!
 		    
			
			final Controller parameter = this; 
			Thread t = new Thread(new Runnable() {
				 Controller c = parameter;
		         public void run()
		         {
		        	c.importButton.setDisable(true);
		        	
		        	Database db = new Database(Config.DATABASE_NAME);
		 	        db.create();
		 	        XmlImport xim = new XmlImport(db, c);
		 	        xim.ImportFromXML("src/main/resources/dblp_filtered.xml");
		 	        c.importButton.setDisable(false);
		         }
			});
			
			t.start();
	        
		});
		
    	tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
	        @Override
	        public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
	        	
	            if (newTab == personTab) {
	            	loadMainTab();
	            }
	            if (newTab == proceedingTab) {
	            	
	            }
	            if (newTab == inProceedingTab) {
	            	
	            }
	            if (newTab == publicationTab) {
	            	
	            }
	            if (newTab == publisherTab) {
	            	
	            }
	            if (newTab == conferenceTab) {
	            	
	            }
	            if (newTab == conferenceEditionTab) {
	            	
	            }
	            if (newTab == seriesTab) {
	            	
	            }
	            if (newTab == importTab) {
	            	
	            }
	        }
	    });
		
    	
		loadMainTab ();
    }
    
    
	
	public void loadMainTab () {
		
		pm.currentTransaction().begin();
        
        System.out.println("Querying for people: ");
        Query query = pm.newQuery(Person.class);
        Collection<Person> people = (Collection<Person>) query.execute();
        
        ObservableList<PersonTableEntry> observableList = FXCollections.observableArrayList();
        
        
        
        for (Person p: people) {
        	StringBuilder builder = new StringBuilder();
        	
        	for (Publication pub : p.getAuthoredPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	for (Publication pub : p.getEditedPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	String auth = builder.substring(0, builder.length() - 2);
        	
        	observableList.add(new PersonTableEntry(p.getName(), auth));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
		
		ObservableList<TableColumn<PersonTableEntry, ?>> columns = personMainTable.getColumns();
		TableColumn<PersonTableEntry,String> nameCol = (TableColumn<PersonTableEntry,String>) columns.get(0);
		TableColumn<PersonTableEntry,String> authEditCol = (TableColumn<PersonTableEntry,String>) columns.get(1);
		
		nameCol.setCellValueFactory(new Callback<CellDataFeatures<PersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().name);
			}
		});
		
		authEditCol.setCellValueFactory(new Callback<CellDataFeatures<PersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().authoredEdited);
			}
		});
		
		personMainTable.setItems(observableList);
	}
	
	
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
    
    
    protected void setImportStatus(String text){
    	importStatusLabel.setText(text);
    }
    
    
    class PersonTableEntry {
    	public String name;
    	public String authoredEdited;
    	public PersonTableEntry(String n, String a) {
    		name = n;
    		authoredEdited = a;
    	}
    }
    

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab personTab;

    @FXML
    private TableView<PersonTableEntry> personMainTable;

    @FXML
    private Button personDeleteButton;

    @FXML
    private TextField personSearchField;

    @FXML
    private Button personSearchButton;

    @FXML
    private Button personNextPageButton;

    @FXML
    private Button personPreviousPageButton;

    @FXML
    private Label personCurrentPageLabel;

    @FXML
    private Button personNewButton;

    @FXML
    private TextField personNameField;

    @FXML
    private Button personChangeNameButton;

    @FXML
    private ChoiceBox<?> personProceedingDropdown;

    @FXML
    private Button personAddEditorButton;

    @FXML
    private TextField personProceedingFilterField;

    @FXML
    private TableView<?> personProceedingTable;

    @FXML
    private Button personRemoveProceedingButton;

    @FXML
    private Button personAddAuthorButton;

    @FXML
    private TextField personInProceedingFilterField;

    @FXML
    private TableView<?> personInProceedingTable;

    @FXML
    private Button personRemoveInProceedingButton;

    @FXML
    private Tab proceedingTab;

    @FXML
    private TableView<?> proceedingMainTable;

    @FXML
    private TextField proceedingSearchField;

    @FXML
    private Button proceedingSearchButton;

    @FXML
    private Button proceedingDeleteButton;

    @FXML
    private Button proceedingNewButton;

    @FXML
    private Button proceedingNextPageButton;

    @FXML
    private Button proceedingPreviousPageButton;

    @FXML
    private Label proceedingCurrentPageLabel;

    @FXML
    private TextField proceedingTitleField;

    @FXML
    private Button proceedingChangeTitleButton;

    @FXML
    private ChoiceBox<?> proceedingPublisherDropdown;

    @FXML
    private Button proceedingChangePublisherButton;

    @FXML
    private TextField proceedingPublisherFilterField;

    @FXML
    private ChoiceBox<?> proceedingSeriesDropdown;

    @FXML
    private Button proceedingChangeSeriesButton;

    @FXML
    private TextField proceedingSeriesFilterField;

    @FXML
    private TextField proceedingIsbnField;

    @FXML
    private Button proceedingChangeIsbnButton;

    @FXML
    private ChoiceBox<?> proceedingConferenceDropdown;

    @FXML
    private Button proceedingChangeConferenceButton;

    @FXML
    private TextField proceedingConferenceFilterField;

    @FXML
    private ChoiceBox<?> proceedingEditionDropdown;

    @FXML
    private Button proceedingChangeEditionButton;

    @FXML
    private TextField proceedingEditorFilterField;

    @FXML
    private TableView<?> proceedingEditorTable;

    @FXML
    private Button proceedingRemoveEditorButton;

    @FXML
    private ChoiceBox<?> proceedingEditorDropdown;

    @FXML
    private Button proceedingAddEditorButton;

    @FXML
    private Tab inProceedingTab;

    @FXML
    private TableView<?> inProceedingMainTable;

    @FXML
    private TextField inProceedingFilterField;

    @FXML
    private Button inProceedingSearchButton;

    @FXML
    private Button inProceedingDeleteButton;

    @FXML
    private Button inProceedingNextPageButton;

    @FXML
    private Button inProceedingPreviousPageButton;

    @FXML
    private Label inProceedingCurrentPageLabel;

    @FXML
    private Button inProceedingCreateButton;

    @FXML
    private TextField inProceedingPagesField;

    @FXML
    private Button inProceedingChangePagesButton;

    @FXML
    private ChoiceBox<?> inProceedingFilterDropdown;

    @FXML
    private Button inProceedingChangeProceedingButton;

    @FXML
    private ChoiceBox<?> inProceedingAuthorDropdown;

    @FXML
    private Button inProceedingAddAuthorButton;

    @FXML
    private TextField inProceedingAuthorFilterField;

    @FXML
    private TableView<?> inProceedingAuthorTable;

    @FXML
    private Button inProceedingRemoveAuthorButton;

    @FXML
    private TextField inProceedingTitleField;

    @FXML
    private Button inProceedingChangeTitleButton;

    @FXML
    private TextField inProceedingYearField;

    @FXML
    private Button inProceedingChangeYearButton;

    @FXML
    private Tab publicationTab;

    @FXML
    private TableView<?> publicationMainTable;

    @FXML
    private TextField publicationSearchField;

    @FXML
    private Button publicationSearchButton;

    @FXML
    private Button publicationDeleteButton;

    @FXML
    private Button publicationNextPageButton;

    @FXML
    private Button publicationPreviousPageButton;

    @FXML
    private Label publicationCurrentPageLabel;

    @FXML
    private Tab publisherTab;

    @FXML
    private TableView<?> publisherMainTable;

    @FXML
    private Button publisherDeleteButton;

    @FXML
    private TextField publisherSearchField;

    @FXML
    private Button publisherSearchButton;

    @FXML
    private Button publisherCreateButton;

    @FXML
    private Button publisherNextPageButton;

    @FXML
    private Button publisherPreviousPageButton;

    @FXML
    private Label publisherCurrentPageLabel;

    @FXML
    private TextField publisherNameField;

    @FXML
    private Button publisherChangeNameButton;

    @FXML
    private ChoiceBox<?> publisherProceedingDropdown;

    @FXML
    private Button publisherAddProceedingButton;

    @FXML
    private TextField publisherProceedingFilterField;

    @FXML
    private ChoiceBox<?> publisherInProceedingDropdown;

    @FXML
    private Button publisherAddInProceedingButton;

    @FXML
    private TextField publisherInProceedingFilterField;

    @FXML
    private TableView<?> publisherProdeedingTable;

    @FXML
    private Button publisherRemoveProceedingButton;

    @FXML
    private TableView<?> publisherInProceedingTable;

    @FXML
    private Button publisherRemoveInProceedingButton;

    @FXML
    private Tab conferenceTab;

    @FXML
    private TableView<?> conferenceMainTable;

    @FXML
    private Button conferenceDeleteButton;

    @FXML
    private TextField conferenceSearchField;

    @FXML
    private Button conferenceSearchButton;

    @FXML
    private Button conferenceCreateButton;

    @FXML
    private Button conferenceNextPageButton;

    @FXML
    private Button conferencePreviousPageButton;

    @FXML
    private Label conferenceCurrentPageLabel;

    @FXML
    private TextField conferenceNameField;

    @FXML
    private Button conferenceChangeNameButton;

    @FXML
    private ChoiceBox<?> conferenceEditionDropdown;

    @FXML
    private Button conferenceAddEditionButton;

    @FXML
    private TextField conferenceEditionFilterField;

    @FXML
    private TableView<?> conferenceEditionTable;

    @FXML
    private Button conferenceRemoveEditionButton;

    @FXML
    private Tab conferenceEditionTab;

    @FXML
    private TableView<?> conferenceEditionMainTable;

    @FXML
    private Button conferenceEditionDeleteButton;

    @FXML
    private TextField conferenceEditionSearchField;

    @FXML
    private Button conferenceEditionSearchButton;

    @FXML
    private Button conferenceEditionCreateButton;

    @FXML
    private Button conferenceEditionNextPageButton;

    @FXML
    private Button conferenceEditionPreviousPageButton;

    @FXML
    private Label conferenceEditionCurrentPageLabel;

    @FXML
    private TextField conferenceEditionNameField;

    @FXML
    private Button conferenceEditionChangeNameButton;

    @FXML
    private TextField conferenceEditionEditionField;

    @FXML
    private Button conferenceEditionChangeEditionButton;

    @FXML
    private TableView<?> conferenceEditionProceedingTable;

    @FXML
    private Button conferenceEditionRemoveProceedingButton;

    @FXML
    private Tab seriesTab;

    @FXML
    private TableView<?> serieMainTable;

    @FXML
    private Button seriesDeleteButton;

    @FXML
    private TextField seriesSearchField;

    @FXML
    private Button seriesSearchButton;

    @FXML
    private Button seriesCreateButton;

    @FXML
    private Button seriesNextPageButton;

    @FXML
    private Button seriesPreviousPageButton;

    @FXML
    private Label seriesCurrentPageLabel;

    @FXML
    private TextField seriesNameField;

    @FXML
    private Button seriesChangeNameButton;

    @FXML
    private TextField seriesProceedingFilterField;

    @FXML
    private Button seriesAddProceedingButton;

    @FXML
    private ChoiceBox<?> seriesProceedingDropdown;

    @FXML
    private TableView<?> seriesProceedingTable;

    @FXML
    private Button seriesRemoveProceedingButton;

    @FXML
    private Tab importTab;

    @FXML
    private Button importButton;

    @FXML
    private Label importStatusLabel;
}