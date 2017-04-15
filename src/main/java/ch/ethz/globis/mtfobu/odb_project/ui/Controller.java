package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

 
public class Controller {
	public final int PAGE_SIZE = 20;
	public PersistenceManager pm;
    public Database db;
	
    public void initialize() {
    	db = new Database(Config.DATABASE_NAME);
    	
    	importButton.setOnAction((event) -> {
			//George: only for testing purposes
	        //of course will have to run on a separate thread!
 		    
			
			final Controller parameter = this; 
			Thread t = new Thread(new Runnable() {
				 Controller c = parameter;
		         public void run()
		         {
		        	c.importButton.setDisable(true);
		        	
		        	db.create();
		    		XmlImport importer = new XmlImport(db, null);
		    		importer.ImportFromXML("src/main/resources/dblp_filtered.xml");
		    		
		 	        c.importButton.setDisable(false);
		         }
			});
			
			t.start();
	        
		});
		
    	tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
	        @Override
	        public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
	        	
	            if (newTab == personTab) {
	            	personTabController.reload();
	            } else if (newTab == proceedingTab) {
	            	proceedingTabController.reload();
	            } else if (newTab == inProceedingTab) {
	            	inProceedingTabController.reload();
	            } else if (newTab == publicationTab) {
	            	publicationTabController.reload();
	            } else if (newTab == publisherTab) {
	            	publisherTabController.reload();
	            } else if (newTab == conferenceTab) {
	            	conferenceTabController.reload();
	            } else if (newTab == conferenceEditionTab) {
	            	conferenceEditionTabController.reload();
	            } else if (newTab == seriesTab) {
	            	seriesTabController.reload();
	            } else if (newTab == importTab) {
	            	
	            }
	        }
	    });
		
    	// Need to do this first because the functions they need from one another are only available after
    	instantiateControllers();
    	
    	setUpPersonTab();
    	setUpProceedingTab();
    	setUpInProceedingTab();
    	setUpPublicationTab();
    	setUpPublisherTab();
    	setUpConferenceTab();
    	setUpConferenceEditionTab();
    	setUpSeriesTab();
    	
    	// Load person tab because that's the one the application starts with
    	//personTabController.reload(); //TODO: I comment that out. It solved the startup issue, but I don't know what side effects it had
    }
    
    
    
    protected PersonTabController personTabController;
    protected ProceedingTabController proceedingTabController;
    protected InProceedingTabController inProceedingTabController;
    protected PublicationTabController publicationTabController;
    protected PublisherTabController publisherTabController;
    protected ConferenceTabController conferenceTabController;
    protected ConferenceEditionTabController conferenceEditionTabController;
    protected SeriesTabController seriesTabController;
    
    protected Collection<TabController> mainDataTabs;
    
    private void instantiateControllers() {
    	personTabController = new PersonTabController(this, personMainTable,
    			personSearchField, personSearchButton,
    			personNextPageButton, personPreviousPageButton, personCurrentPageField,
    			personNewButton, personDeleteButton, 
    			personProceedingTable, personRemoveProceedingButton,
    			personInProceedingTable, personRemoveInProceedingButton);
    	
    	proceedingTabController = new ProceedingTabController(this, proceedingMainTable,
				proceedingSearchField, proceedingSearchButton,
				proceedingNextPageButton, proceedingPreviousPageButton, proceedingCurrentPageField,
				proceedingNewButton, proceedingDeleteButton,
				proceedingEditorTable, proceedingRemoveEditorButton,
				null, null);
    	
    	inProceedingTabController = new InProceedingTabController(this, inProceedingMainTable,
    			inProceedingSearchField, inProceedingSearchButton,
    			inProceedingNextPageButton, inProceedingPreviousPageButton, inProceedingCurrentPageField,
    			inProceedingCreateButton, inProceedingDeleteButton,
    			inProceedingAuthorTable, inProceedingRemoveAuthorButton,
				null, null);
    	
    	publicationTabController = new PublicationTabController(this, publicationMainTable,
    			publicationSearchField, publicationSearchButton,
    			publicationNextPageButton, publicationPreviousPageButton, publicationCurrentPageField,
    			null, publicationDeleteButton,
    			null, null,
				null, null);
    	
    	publisherTabController = new PublisherTabController(this, publisherMainTable,
    			publisherSearchField, publisherSearchButton,
    			publisherNextPageButton, publisherPreviousPageButton, publisherCurrentPageField,
    			publisherCreateButton, publisherDeleteButton,
    			publisherProceedingTable, publisherRemoveProceedingButton,
				null, null);
    	
    	conferenceTabController = new ConferenceTabController(this, conferenceMainTable,
    			conferenceSearchField, conferenceSearchButton,
    			conferenceNextPageButton, conferencePreviousPageButton, conferenceCurrentPageField,
    			conferenceCreateButton, conferenceDeleteButton,
    			conferenceEditionTable, conferenceRemoveEditionButton,
				null, null);
    	
    	conferenceEditionTabController = new ConferenceEditionTabController(this, conferenceEditionMainTable,
    			conferenceEditionSearchField, conferenceEditionSearchButton,
    			conferenceEditionNextPageButton, conferenceEditionPreviousPageButton, conferenceEditionCurrentPageField,
    			conferenceEditionCreateButton, conferenceEditionDeleteButton,
    			null, null,
				null, null);
    	
    	seriesTabController = new SeriesTabController(this, seriesMainTable,
    			seriesSearchField, seriesSearchButton,
    			seriesNextPageButton, seriesPreviousPageButton, seriesCurrentPageField,
    			seriesCreateButton, seriesDeleteButton,
    			seriesProceedingTable, seriesRemoveProceedingButton,
				null, null);
    	
    	mainDataTabs = new ArrayList<TabController>(10);
    	mainDataTabs.add(personTabController);
    	mainDataTabs.add(proceedingTabController);
    	mainDataTabs.add(inProceedingTabController);
    	mainDataTabs.add(publicationTabController);
    	mainDataTabs.add(publisherTabController);
    	mainDataTabs.add(conferenceTabController);
    	mainDataTabs.add(conferenceEditionTabController);
    	mainDataTabs.add(seriesTabController);
    	
    }
    
    // START section for person tab
    private void setUpPersonTab() {
    	personTabController.initializeTabSpecificItems(
    			personNameField,
    			personChangeNameButton,
    			
    			personProceedingFilterField,
    			personProceedingDropdown,
    			personAddEditorButton,
    			
    			personInProceedingFilterField,
    			personInProceedingDropdown,
    			personAddAuthorButton);
    	
    	personTabController.initializeFunctions(proceedingTabController.mainShowFunction, inProceedingTabController.mainShowFunction);
    	
    	personTabController.setUpTables();
    }
	// END section for person tab
	
	
	// START section for proceeding tab
	private void setUpProceedingTab() {
		proceedingTabController.initializeTabSpecificItems(
				proceedingTitleField,
				proceedingChangeTitleButton,

				proceedingPublisherDropdown,
				proceedingChangePublisherButton,
				proceedingPublisherFilterField,

				proceedingSeriesDropdown,
				proceedingChangeSeriesButton,
				proceedingSeriesFilterField,

				proceedingIsbnField,
				proceedingChangeIsbnButton,

				proceedingConferenceDropdown,
				proceedingChangeConferenceButton,
				proceedingConferenceFilterField,

				proceedingEditionFilterField,
				proceedingEditionDropdown,
				proceedingChangeEditionButton,

				proceedingEditorFilterField,
				proceedingEditorDropdown,
				proceedingAddEditorButton);
		
		proceedingTabController.initializeFunctions(personTabController.mainShowFunction);
		
		proceedingTabController.setUpTables();
	}
	// END section for proceeding tab
	
	
	// START section for inproceeding tab
	private void setUpInProceedingTab() {
		
		inProceedingTabController.initializeTabSpecificItems(
				  inProceedingPagesField,
				  inProceedingChangePagesButton,
				 
				  inProceedingProceedingFilterField,
				  inProceedingProceedingDropdown,
				  inProceedingChangeProceedingButton,
				 
				  inProceedingAuthorDropdown,
				  inProceedingAddAuthorButton,
				  inProceedingAuthorFilterField,
				 
				  inProceedingTitleField,
				  inProceedingChangeTitleButton,
				 
				  inProceedingYearField,
				  inProceedingChangeYearButton);
		
		inProceedingTabController.initializeFunctions(personTabController.mainShowFunction);
		
		inProceedingTabController.setUpTables();

	}
	// END section for inproceeding tab
	
	
	// START section for publication tab
	private void setUpPublicationTab() {

		publicationTabController.initializeTabSpecificItems();
		
		publicationTabController.initializeFunctions();

	
		publicationTabController.setUpTables();
	}
	// End section for publication tab
	
	
	// START section for publisher tab
	private void setUpPublisherTab() {
		publisherTabController.initializeTabSpecificItems(
				publisherNameField, 
				publisherChangeNameButton,
				
				publisherProceedingDropdown,
				publisherAddProceedingButton,
				publisherProceedingFilterField);
		
		publisherTabController.initializeFunctions(proceedingTabController.mainShowFunction);
		
		publisherTabController.setUpTables();
	}
	// End section for publisher tab
	
	
	// START section for conferences tab
	private void setUpConferenceTab() {
		conferenceTabController.initializeTabSpecificItems(
				conferenceNameField,
				conferenceChangeNameButton,
				
				conferenceEditionDropdown,
				conferenceAddEditionButton,
				conferenceEditionFilterField);
		
		conferenceTabController.initializeFunctions(conferenceEditionTabController.mainShowFunction);
		
		conferenceTabController.setUpTables();
	}
	// END section for conferences tab
	
	
	// START section for conference editions tab	
	private void setUpConferenceEditionTab() {
		conferenceEditionTabController.initializeTabSpecificItems(
				conferenceEditionNameField,
				conferenceEditionChangeNameButton,
				
				conferenceEditionEditionField,
				conferenceEditionChangeEditionButton);
		
		conferenceEditionTabController.initializeFunctions();
		
		conferenceEditionTabController.setUpTables();
	}
	// END section for conference editions tab
	
	
	// START section for series tab
	private void setUpSeriesTab() {
		
		seriesTabController.initializeTabSpecificItems(
				seriesNameField, 
				seriesChangeNameButton, 
				
				seriesProceedingFilterField,
				seriesAddProceedingButton, 
				seriesProceedingDropdown);
		
		seriesTabController.initializeFunctions(proceedingTabController.mainShowFunction);
		
		seriesTabController.setUpTables();
	}
	// END section for series tab
    
    
    public void setImportStatus(String text){
    	importStatusLabel.setText(text);
    }
    
    
    public void setAllDataTabsDirty() {
    	
        for (TabController tab: mainDataTabs) {
        	tab.setDirty();
        }	
        
    }
    
    
    // START section for table entry data types
    public abstract class TableEntry {
//    	public long objectId;
    	public String id;
    	public String getColumnContent(int i) {
    		return "";
    	};
    }
    
    public class PersonTableEntry extends TableEntry{
    	public String name;
    	public String authoredEdited;
    	public PersonTableEntry(Person person) {
        	StringBuilder builder = new StringBuilder();
        	
        	for (Publication pub : person.getAuthoredPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	for (Publication pub : person.getEditedPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	id = person.getId();
//        	objectId = person.jdoZooGetOid();
        	name = person.getName();
        	
        	int end = builder.length() - 2;
        	if (0 < end) {
        		authoredEdited = builder.substring(0, end);
        	} else {
        		authoredEdited = "";
        	}
        	
    	}
		@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				case 1: return authoredEdited;
				default: return "";
			}
		}
    }
       
    public class PublicationTableEntry extends TableEntry{
    	public String title;
    	public String authorsEditors;
    	public PublicationTableEntry(Publication pub) {
    		title = pub.getTitle();
    		StringBuilder builder = new StringBuilder();
        	
        	for (Person pers : pub.getAuthors()) {
        		builder.append(pers.getName());
        		builder.append(", ");
        	}
        	
        	int end = builder.length() - 2;
        	if (0 < end) {
        		authorsEditors = builder.substring(0, end);
        	} else {
        		authorsEditors = "";
        	}
        	
        	id = pub.getId();
        	
//        	if (pub instanceof Proceedings) {
//        		objectId = ((Proceedings)pub).jdoZooGetOid();
//        	} else {
//        		objectId = ((InProceedings)pub).jdoZooGetOid();
//        	}
        	
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return title;
				case 1: return authorsEditors;
				default: return "";
			}
		}
    }
    
    public class ProceedingTableEntry extends TableEntry{
    	public String title;
    	public String publisher;
    	public String conference;
    	public ProceedingTableEntry(Proceedings proc) {
    		title = proc.getTitle();
    		Publisher pub = proc.getPublisher();
    		if (null != pub) {
    			publisher = pub.getName();
    		} else {
    			publisher = "";
    		}
    		ConferenceEdition confEd = proc.getConferenceEdition();
    		if (null != confEd) {
    			Conference conf = confEd.getConference();
    			if (null != conf) {
    				conference = conf.getName();
    			} else {
    				conference = "";
    			}
    		} else {
    			conference = "";
    		}
    		
        	id = proc.getId();
//        	objectId = proc.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return title;
				case 1: return publisher;
				case 2: return conference;
				default: return "";
			}
		}
    }
    
    public class InProceedingTableEntry extends TableEntry{
    	public String title;
    	public String year;
    	public String proceeding;
    	public InProceedingTableEntry(InProceedings inProc) {
    		title = inProc.getTitle();
    		int y = inProc.getYear();
    		if (0 != y) {
    			year = Integer.toString(inProc.getYear());
    		} else {
    			year = "";
    		}
    		Proceedings proc = inProc.getProceedings();
    		if (null != proc) {
    			proceeding = proc.getTitle();
    		} else {
    			proceeding = "";
    		}
    		id = inProc.getId();
//        	objectId = inProc.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return title;
				case 1: return year;
				case 2: return proceeding;
				default: return "";
			}
		}
    }
    
    public class PublisherTableEntry extends TableEntry{
    	public String name;
    	public String publications;
    	public PublisherTableEntry(Publisher puber) {
    		name = puber.getName();
    		
    		StringBuilder builder = new StringBuilder();
        	
        	for (Publication pub : puber.getPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	int end = builder.length() - 2;
        	if (0 < end) {
        		publications = builder.substring(0, end);
        	} else {
        		publications = "";
        	}
        	id = puber.getId();
//    		objectId = puber.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				case 1: return publications;
				default: return "";
			}
		}
    }
    
    public class SecondaryPersonTableEntry extends TableEntry {
    	public String name;
    	public SecondaryPersonTableEntry(Person person) {
    		id = person.getId();
//    		objectId = person.jdoZooGetOid();
        	name = person.getName();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				default: return "";
			}
		}
    }
    
    public class ConferenceTableEntry extends TableEntry{
    	public String name;
    	public String editions;
    	public ConferenceTableEntry(Conference conf) {
    		name = conf.getName();
    		
    		StringBuilder builder = new StringBuilder();
        	
        	for (ConferenceEdition confEdit : conf.getEditions()) {
        		int year = confEdit.getYear();
        		if (0 != year) {
        			builder.append(year);
        			builder.append(", ");
        		}
        	}
        	
        	int end = builder.length() - 2;
        	if (0 < end) {
        		editions = builder.substring(0, end);
        	} else {
        		editions = "";
        	}
        	id = conf.getId();
//    		objectId = conf.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				case 1: return editions;
				default: return "";
			}
		}
    }
    
    public class ConferenceEditionTableEntry extends TableEntry{
    	public String name;
    	public String edition;
    	public String proceeding;
    	public ConferenceEditionTableEntry(ConferenceEdition confEd) {
    		int year = confEd.getYear();
    		if (0 != year) {
    			edition = Integer.toString(year);
    		} else {
    			edition = "No year";
    		}
    		
    		Conference conf = confEd.getConference();
    		if (null != conf) {
    			name = conf.getName();
    		} else {
    			name = "No name";
    		}
    		
    		Proceedings proc = confEd.getProceedings();
    		if (null != proc) {
    			proceeding = proc.getTitle();
    		}
    		id = confEd.getId();
//    		objectId = confEd.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				case 1: return edition;
				case 2: return proceeding;
				default: return "";
			}
		}
    }
    
    public class SeriesTableEntry extends TableEntry{
    	private String name;
    	private String publications;
    	SeriesTableEntry(Series ser) {
    		name = ser.getName();
    		
    		StringBuilder builder = new StringBuilder();
        	
        	for (Publication pub : ser.getPublications()) {
        		builder.append(pub.getTitle());
        		builder.append(", ");
        	}
        	
        	int end = builder.length() - 2;
        	if (0 < end) {
        		publications = builder.substring(0, end);
        	} else {
        		publications = "";
        	}
        	id = ser.getId();
//    		objectId = ser.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return name;
				case 1: return publications;
				default: return "";
			}
		}
    }
    
    public class SecondaryProceedingTableEntry extends TableEntry {
    	public String title;
    	public String conference;
    	public SecondaryProceedingTableEntry(Proceedings proc) {
    		id = proc.getId();
//    		objectId = proc.jdoZooGetOid();
    		title = proc.getTitle();
    		ConferenceEdition confEd = proc.getConferenceEdition();
    		if (null != confEd) {
    			Conference conf = confEd.getConference();
    			if (null != conf) {
    				conference = conf.getName();
    			} else {
    				conference = "";
    			}
    		} else {
    			conference = "";
    		}
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return title;
				case 1: return conference;
				default: return "";
			}
		}
    }
    
    
    public class SecondaryInProceedingTableEntry extends TableEntry {
    	public String title;
    	public String proceeding;
    	public SecondaryInProceedingTableEntry(InProceedings inProc) {
    		id = inProc.getId();
//    		objectId = inProc.jdoZooGetOid();
    		title = inProc.getTitle();
    		Proceedings proc = inProc.getProceedings();
    		if (null != proc) {
    			proceeding = proc.getTitle();
    		} else {
    			proceeding = "";
    		}
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return title;
				case 1: return proceeding;
				default: return "";
			}
		}
    }
    
    public class SecondaryConferenceEditionTableEntry extends TableEntry {
    	public String year;
    	public SecondaryConferenceEditionTableEntry(ConferenceEdition confEdit) {
    		int y = confEdit.getYear();
    		if (0 != y) {
    			year = Integer.toString(y);
    		} else {
    			year = "No year";
    		}
    		id = confEdit.getId();
//    		objectId = confEdit.jdoZooGetOid();
    	}
    	@Override
		public String getColumnContent(int i) {
			switch (i) {
				case 0: return year;
				default: return "";
			}
		}
    }
    // END section for table entry data types
    
    
    
    
    
    public class DeleteHandler<T extends TableEntry> implements EventHandler<ActionEvent>{
//    	Consumer<Long> delete;
    	Consumer<String> delete;
    	TableView<T> table;
    	
    	public DeleteHandler(TableView<T> t, Consumer<String> d) {
    		delete = d;
    		table = t;
    	}
    	
		@Override
		public void handle(ActionEvent event) {
			T pte = table.getSelectionModel().getSelectedItem();
			if (null != pte) {
//				delete.accept(pte.objectId);
				delete.accept(pte.id);
				setAllDataTabsDirty();
			} else {
				System.err.println("cannot delete, no row selected");
			}
			
		}
		
	}
    
    // START section for fields that reference FXML
    @FXML TabPane tabPane;
    @FXML Tab personTab;
    @FXML    private TableView<PersonTableEntry> personMainTable;
    @FXML    private Button personDeleteButton;
    @FXML    private TextField personSearchField;
    @FXML    private Button personSearchButton;
    @FXML    private Button personNextPageButton;
    @FXML    private Button personPreviousPageButton;
    @FXML    private TextField personCurrentPageField;
    @FXML    private Button personNewButton;
    @FXML    private TextField personNameField;
    @FXML    private Button personChangeNameButton;
    @FXML    private ChoiceBox<?> personProceedingDropdown;
    @FXML    private ChoiceBox<?> personInProceedingDropdown;
    @FXML    private Button personAddEditorButton;
    @FXML    private TextField personProceedingFilterField;
    @FXML    private TableView<SecondaryProceedingTableEntry> personProceedingTable;
    @FXML    private Button personRemoveProceedingButton;
    @FXML    private Button personAddAuthorButton;
    @FXML    private TextField personInProceedingFilterField;
    @FXML    private TableView<SecondaryInProceedingTableEntry> personInProceedingTable;
    @FXML    private Button personRemoveInProceedingButton;
    @FXML Tab proceedingTab;
    @FXML    private TableView<ProceedingTableEntry> proceedingMainTable;
    @FXML    private TextField proceedingSearchField;
    @FXML    private Button proceedingSearchButton;
    @FXML    private Button proceedingDeleteButton;
    @FXML    private Button proceedingNewButton;
    @FXML    private Button proceedingNextPageButton;
    @FXML    private Button proceedingPreviousPageButton;
    @FXML    private TextField proceedingCurrentPageField;
    @FXML    private TextField proceedingTitleField;
    @FXML    private Button proceedingChangeTitleButton;
    @FXML    private ChoiceBox<?> proceedingPublisherDropdown;
    @FXML    private Button proceedingChangePublisherButton;
    @FXML    private TextField proceedingPublisherFilterField;
    @FXML    private ChoiceBox<?> proceedingSeriesDropdown;
    @FXML    private Button proceedingChangeSeriesButton;
    @FXML    private TextField proceedingSeriesFilterField;
    @FXML    private TextField proceedingIsbnField;
    @FXML    private Button proceedingChangeIsbnButton;
    @FXML    private ChoiceBox<?> proceedingConferenceDropdown;
    @FXML    private Button proceedingChangeConferenceButton;
    @FXML    private TextField proceedingConferenceFilterField;
    @FXML    private TextField proceedingEditionFilterField;
    @FXML    private ChoiceBox<?> proceedingEditionDropdown;
    @FXML    private Button proceedingChangeEditionButton;
    @FXML    private TextField proceedingEditorFilterField;
    @FXML    private TableView<SecondaryPersonTableEntry> proceedingEditorTable;
    @FXML    private Button proceedingRemoveEditorButton;
    @FXML    private ChoiceBox<?> proceedingEditorDropdown;
    @FXML    private Button proceedingAddEditorButton;
    @FXML    Tab inProceedingTab;
    @FXML    private TableView<InProceedingTableEntry> inProceedingMainTable;
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
    @FXML    private TableView<SecondaryPersonTableEntry> inProceedingAuthorTable;
    @FXML    private Button inProceedingRemoveAuthorButton;
    @FXML    private TextField inProceedingTitleField;
    @FXML    private Button inProceedingChangeTitleButton;
    @FXML    private TextField inProceedingYearField;
    @FXML    private Button inProceedingChangeYearButton;
    @FXML    Tab publicationTab;
    @FXML    private TableView<PublicationTableEntry> publicationMainTable;
    @FXML    private TextField publicationSearchField;
    @FXML    private Button publicationSearchButton;
    @FXML    private Button publicationDeleteButton;
    @FXML    private Button publicationNextPageButton;
    @FXML    private Button publicationPreviousPageButton;
    @FXML    private TextField publicationCurrentPageField;
    @FXML    Tab publisherTab;
    @FXML    private TableView<PublisherTableEntry> publisherMainTable;
    @FXML    private Button publisherDeleteButton;
    @FXML    private TextField publisherSearchField;
    @FXML    private Button publisherSearchButton;
    @FXML    private Button publisherCreateButton;
    @FXML    private Button publisherNextPageButton;
    @FXML    private Button publisherPreviousPageButton;
    @FXML    private TextField publisherCurrentPageField;
    @FXML    private TextField publisherNameField;
    @FXML    private Button publisherChangeNameButton;
    @FXML    private ChoiceBox<?> publisherProceedingDropdown;
    @FXML    private Button publisherAddProceedingButton;
    @FXML    private TextField publisherProceedingFilterField;
    @FXML    private TableView<SecondaryProceedingTableEntry> publisherProceedingTable;
    @FXML    private Button publisherRemoveProceedingButton;
    @FXML    Tab conferenceTab;
    @FXML    private TableView<ConferenceTableEntry> conferenceMainTable;
    @FXML    private Button conferenceDeleteButton;
    @FXML    private TextField conferenceSearchField;
    @FXML    private Button conferenceSearchButton;
    @FXML    private Button conferenceCreateButton;
    @FXML    private Button conferenceNextPageButton;
    @FXML    private Button conferencePreviousPageButton;
    @FXML    private TextField conferenceCurrentPageField;
    @FXML    private TextField conferenceNameField;
    @FXML    private Button conferenceChangeNameButton;
    @FXML    private ChoiceBox<?> conferenceEditionDropdown;
    @FXML    private Button conferenceAddEditionButton;
    @FXML    private TextField conferenceEditionFilterField;
    @FXML    private TableView<SecondaryConferenceEditionTableEntry> conferenceEditionTable;
    @FXML    private Button conferenceRemoveEditionButton;
    @FXML    Tab conferenceEditionTab;
    @FXML    private TableView<ConferenceEditionTableEntry> conferenceEditionMainTable;
    @FXML    private Button conferenceEditionDeleteButton;
    @FXML    private TextField conferenceEditionSearchField;
    @FXML    private Button conferenceEditionSearchButton;
    @FXML    private Button conferenceEditionCreateButton;
    @FXML    private Button conferenceEditionNextPageButton;
    @FXML    private Button conferenceEditionPreviousPageButton;
    @FXML    private TextField conferenceEditionCurrentPageField;
    @FXML    private TextField conferenceEditionNameField;
    @FXML    private Button conferenceEditionChangeNameButton;
    @FXML    private TextField conferenceEditionEditionField;
    @FXML    private Button conferenceEditionChangeEditionButton;
    @FXML    Tab seriesTab;
    @FXML    private TableView<SeriesTableEntry> seriesMainTable;
    @FXML    private Button seriesDeleteButton;
    @FXML    private TextField seriesSearchField;
    @FXML    private Button seriesSearchButton;
    @FXML    private Button seriesCreateButton;
    @FXML    private Button seriesNextPageButton;
    @FXML    private Button seriesPreviousPageButton;
    @FXML    private TextField seriesCurrentPageField;
    @FXML    private TextField seriesNameField;
    @FXML    private Button seriesChangeNameButton;
    @FXML    private TextField seriesProceedingFilterField;
    @FXML    private Button seriesAddProceedingButton;
    @FXML    private ChoiceBox<?> seriesProceedingDropdown;
    @FXML    private TableView<SecondaryProceedingTableEntry> seriesProceedingTable;
    @FXML    private Button seriesRemoveProceedingButton;
    @FXML    Tab importTab;
    @FXML    private Button importButton;
    @FXML    private Label importStatusLabel;
    // END section for fields that reference FXML
}