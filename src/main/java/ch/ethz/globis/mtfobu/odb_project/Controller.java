package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;

import ch.ethz.globis.mtfobu.odb_project.Controller.MyRowFactory;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
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
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
 
public class Controller {
	private final int PAGE_SIZE = 20;
	public PersistenceManager pm;
	
    public void initialize() {
    	pm = ZooJdoHelper.openOrCreateDB(Config.DATABASE_NAME);
    	
    	
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
		 	        c.pm = ZooJdoHelper.openDB(Config.DATABASE_NAME);
		 	        c.importButton.setDisable(false);
		         }
			});
			
			t.start();
	        
		});
		
    	tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
	        @Override
	        public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
	        	
	            if (newTab == personTab) {
	            	loadDataPersonTab();
	            } else if (newTab == proceedingTab) {
	            	loadDataProceedingTab();
	            } else if (newTab == inProceedingTab) {
	            	loadDataInProceedingTab();
	            } else if (newTab == publicationTab) {
	            	loadDataPublicationTab();
	            } else if (newTab == publisherTab) {
	            	loadDataPublisherTab();
	            } else if (newTab == conferenceTab) {
	            	loadDataConferenceTab();
	            } else if (newTab == conferenceEditionTab) {
	            	loadDataConferenceEditionTab();
	            } else if (newTab == seriesTab) {
	            	loadDataSeriesTab();
	            } else if (newTab == importTab) {
	            	
	            }
	        }
	    });
		
    	setUpPersonTab();
    	setUpProceedingTab();
    	setUpInProceedingTab();
    	setUpPublicationTab();
    	setUpPublisherTab();
    	setUpConferenceTab();
    	setUpConferenceEditionTab();
    	setUpSeriesTab();
    	
    	// Load person tab because that's the one the application starts with
		loadDataPersonTab();
    }
    
    // START section for person tab
    private ObservableList<PersonTableEntry> personMainTableList = FXCollections.observableArrayList();
    private ObservableList<SecondaryProceedingTableEntry> personProceedingTableList = FXCollections.observableArrayList();
    private ObservableList<SecondaryInProceedingTableEntry> personInProceedingTableList = FXCollections.observableArrayList();
    private int[] personQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
    
    private void setUpPersonTab() {
    	
    	// START main table stuff
    	new TabSetupHelper<PersonTableEntry>().setUpTable(personMainTable, personMainTableList, this::showPerson);
		new PagingSetupHelper().setUpPaging(personNextPageButton, personPreviousPageButton, personCurrentPageField, personQueryPage, this::loadDataPersonTab);
		
		personDeleteButton.setOnAction(new DeleteHandler<PersonTableEntry>(personMainTable, this::deletePerson));
    	// END main table stuff
		
		// START proceeding table stuff
		new TabSetupHelper<SecondaryProceedingTableEntry>().setUpTable(personProceedingTable, personProceedingTableList, this::showProceeding);
		// END proceeding table stuff
		
		// START inproceeding table stuff
		new TabSetupHelper<SecondaryInProceedingTableEntry>().setUpTable(personInProceedingTable, personInProceedingTableList, this::showInProceeding);
		// END inproceeding table stuff
    }
    
	private void loadDataPersonTab () {
		personMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Person.class);
        query.setRange((personQueryPage[0]-1)*PAGE_SIZE, personQueryPage[0]*PAGE_SIZE);
        Collection<Person> people = (Collection<Person>) query.execute();

        for (Person person: people) {
        	personMainTableList.add(new PersonTableEntry(person));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	private void showPerson(long objectId) {
		pm.currentTransaction().begin();
		Person person = (Person) pm.getObjectById(objectId);
		
		personNameField.setText(person.getName());
		
		personProceedingFilterField.setText("");
		personProceedingTableList.clear();
		for (Publication proc : person.getEditedPublications()) {
        	personProceedingTableList.add(new SecondaryProceedingTableEntry((Proceedings) proc));
        }
		
		personInProceedingFilterField.setText("");
		personInProceedingTableList.clear();
		for (Publication inProc : person.getAuthoredPublications()) {
        	personInProceedingTableList.add(new SecondaryInProceedingTableEntry((InProceedings) inProc));
        }
		
		pm.currentTransaction().commit();
		
		tabPane.getSelectionModel().select(personTab);
	}
	
	private void emptyPersonFields() {
		personNameField.setText("");
		personProceedingFilterField.setText("");
		personProceedingTableList.clear();
		personInProceedingFilterField.setText("");
		personInProceedingTableList.clear();
	}
	
	private void deletePerson(long objectId) {
		pm.currentTransaction().begin();
		Person person = (Person) pm.getObjectById(objectId);
		person.removeReferencesFromOthers();
		pm.deletePersistent(person);
		pm.currentTransaction().commit();
		loadDataPersonTab();
		emptyPersonFields();
	}
	// END section for person tab
	
	
	// START section for proceeding tab
	private ObservableList<ProceedingTableEntry> proceedingMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryPersonTableEntry> proceedingEditorTableList = FXCollections.observableArrayList();
	private int[] proceedingQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
    
	private void setUpProceedingTab() {
		
		// START main table stuff
		new TabSetupHelper<ProceedingTableEntry>().setUpTable(proceedingMainTable, proceedingMainTableList, this::showProceeding);
		new PagingSetupHelper().setUpPaging(proceedingNextPageButton, proceedingPreviousPageButton, proceedingCurrentPageField, proceedingQueryPage, this::loadDataProceedingTab);
		
		proceedingDeleteButton.setOnAction(new DeleteHandler<ProceedingTableEntry>(proceedingMainTable, this::deleteProceeding));
		// END main table stuff
		
		// START editor table stuff
		new TabSetupHelper<SecondaryPersonTableEntry>().setUpTable(proceedingEditorTable, proceedingEditorTableList, this::showPerson);
		// END editor table stuff
	}
	
	private void loadDataProceedingTab() {
		proceedingMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Proceedings.class);
        query.setRange((proceedingQueryPage[0]-1)*PAGE_SIZE, proceedingQueryPage[0]*PAGE_SIZE);
        Collection<Proceedings> proceedings = (Collection<Proceedings>) query.execute();

        for (Proceedings proc: proceedings) {
        	proceedingMainTableList.add(new ProceedingTableEntry(proc));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	private void showProceeding(long objectId) {
		pm.currentTransaction().begin();
		Proceedings proc = (Proceedings) pm.getObjectById(objectId);
		
		proceedingTitleField.setText(proc.getTitle());
		proceedingIsbnField.setText(proc.getIsbn());
		
		Publisher pub = proc.getPublisher();
		if (null != pub) {
			proceedingPublisherFilterField.setText(pub.getName());
		} else {
			proceedingPublisherFilterField.setText("");
		}
		
		ConferenceEdition edi = proc.getConferenceEdition();
		if (null != edi) {
			proceedingEditionFilterField.setText(Integer.toString(edi.getYear()));
			
			Conference conf = edi.getConference();
			if (null != conf) {
				proceedingConferenceFilterField.setText(conf.getName());
			} else {
				proceedingConferenceFilterField.setText("");
			}
			
		} else {
			proceedingEditionFilterField.setText("");
		}
		
		Series ser = proc.getSeries();
		if (null != ser) {
			proceedingSeriesFilterField.setText(ser.getName());
		} else {
			proceedingSeriesFilterField.setText("");
		}
		
		proceedingEditorFilterField.setText("");
		proceedingEditorTableList.clear();
		for (Person person : proc.getAuthors()) {
			proceedingEditorTableList.add(new SecondaryPersonTableEntry(person));
        }
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(proceedingTab);
	}
	
	private void emptyProceedingFields() {
		proceedingTitleField.setText("");
		proceedingIsbnField.setText("");
		proceedingPublisherFilterField.setText("");
		proceedingConferenceFilterField.setText("");
		proceedingEditionFilterField.setText("");
		proceedingSeriesFilterField.setText("");
		proceedingEditorFilterField.setText("");
		proceedingEditorTableList.clear();
	}
	
	private void deleteProceeding(long objectId) {
		pm.currentTransaction().begin();
		Proceedings proc = (Proceedings) pm.getObjectById(objectId);
		proc.removeReferencesFromOthers();
		pm.deletePersistent(proc);
		pm.currentTransaction().commit();
		loadDataProceedingTab();
		emptyProceedingFields();
	}
	// END section for proceeding tab
	
	
	// START section for inproceeding tab
	private ObservableList<InProceedingTableEntry> inProceedingMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryPersonTableEntry> inProceedingAuthorTableList = FXCollections.observableArrayList();
	private int[] inProceedingQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	private void setUpInProceedingTab() {
		
		// START main table stuff
		new TabSetupHelper<InProceedingTableEntry>().setUpTable(inProceedingMainTable, inProceedingMainTableList, this::showInProceeding);
		new PagingSetupHelper().setUpPaging(inProceedingNextPageButton, inProceedingPreviousPageButton, inProceedingCurrentPageField, inProceedingQueryPage, this::loadDataInProceedingTab);
		
		inProceedingDeleteButton.setOnAction(new DeleteHandler<InProceedingTableEntry>(inProceedingMainTable, this::deleteInProceeding));
		// END main table stuff
		
		// START author table stuff
		new TabSetupHelper<SecondaryPersonTableEntry>().setUpTable(inProceedingAuthorTable, inProceedingAuthorTableList, this::showPerson);
		// END author table stuff
	}
	
	private void loadDataInProceedingTab() {
		inProceedingMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(InProceedings.class);
        query.setRange((inProceedingQueryPage[0]-1)*PAGE_SIZE, inProceedingQueryPage[0]*PAGE_SIZE);
        Collection<InProceedings> inProceedings = (Collection<InProceedings>) query.execute();

        for (InProceedings inProc: inProceedings) {
        	inProceedingMainTableList.add(new InProceedingTableEntry(inProc));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	private void showInProceeding(long objectId) {
		pm.currentTransaction().begin();
		InProceedings inProc = (InProceedings) pm.getObjectById(objectId);
		
		inProceedingTitleField.setText(inProc.getTitle());
		inProceedingPagesField.setText(inProc.getPages());
		inProceedingYearField.setText(Integer.toString(inProc.getYear()));
		
		Proceedings proc = inProc.getProceedings();
		if(null != proc) {
			inProceedingProceedingFilterField.setText(proc.getTitle());
		}
		
		inProceedingAuthorFilterField.setText("");
		inProceedingAuthorTableList.clear();
		for (Person person : inProc.getAuthors()) {
			inProceedingAuthorTableList.add(new SecondaryPersonTableEntry(person));
        }
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(inProceedingTab);
	}
    
	private void emptyInProceedingFields() {
		inProceedingTitleField.setText("");
		inProceedingPagesField.setText("");
		inProceedingYearField.setText("");
		inProceedingProceedingFilterField.setText("");
		inProceedingAuthorFilterField.setText("");
		inProceedingAuthorTableList.clear();
	}
	
	private void deleteInProceeding(long objectId) {
		pm.currentTransaction().begin();
		InProceedings inProc = (InProceedings) pm.getObjectById(objectId);
		inProc.removeReferencesFromOthers();
		pm.deletePersistent(inProc);
		pm.currentTransaction().commit();
		loadDataInProceedingTab();
		emptyInProceedingFields();
	}
	// END section for inproceeding tab
	
	
	// START section for publication tab
	private ObservableList<PublicationTableEntry> publicationMainTableList = FXCollections.observableArrayList();
	private int[] publicationQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	private void setUpPublicationTab() {
		// START main table stuff
		new TabSetupHelper<PublicationTableEntry>().setUpTable(publicationMainTable, publicationMainTableList, this::showPublication);
		new PagingSetupHelper().setUpPaging(publicationNextPageButton, publicationPreviousPageButton, publicationCurrentPageField, publicationQueryPage, this::loadDataPublicationTab);
		
		publicationDeleteButton.setOnAction(new DeleteHandler<PublicationTableEntry>(publicationMainTable, this::deletePublication));
		// END main table stuff
	}
	
	private void loadDataPublicationTab() {
		publicationMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Proceedings.class);
        query.setRange((publicationQueryPage[0]-1)*PAGE_SIZE, publicationQueryPage[0]*PAGE_SIZE);
        Collection<Publication> publications = (Collection<Publication>) query.execute();

        for (Publication proc: publications) {
        	publicationMainTableList.add(new PublicationTableEntry(proc));
        }
        
        query.closeAll();
        
        query = pm.newQuery(InProceedings.class);
        query.setRange((publicationQueryPage[0]-1)*PAGE_SIZE, publicationQueryPage[0]*PAGE_SIZE);
        publications = (Collection<Publication>) query.execute();
        
        for (Publication inProc: publications) {
        	publicationMainTableList.add(new PublicationTableEntry(inProc));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	private void showPublication(long objectId) {
		boolean isProceeding = false;
		pm.currentTransaction().begin();
		Publication pub = (Publication) pm.getObjectById(objectId);
		isProceeding = pub instanceof Proceedings;
		pm.currentTransaction().commit();
		
		if (isProceeding) {
			showProceeding(objectId);
		} else {
			showInProceeding(objectId);
		}
	}
	
	private void deletePublication(long objectId) {
		boolean isProceeding = false;
		pm.currentTransaction().begin();
		Publication pub = (Publication) pm.getObjectById(objectId);
		isProceeding = pub instanceof Proceedings;
		pm.currentTransaction().commit();
		
		if (isProceeding) {
			deleteProceeding(objectId);
		} else {
			deleteInProceeding(objectId);
		}
		loadDataPublicationTab();
	}
	// End section for publication tab
	
	
	// START section for publisher tab
	private ObservableList<PublisherTableEntry> publisherMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryProceedingTableEntry> publisherProceedingTableList = FXCollections.observableArrayList();
    private int[] publisherQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	private void setUpPublisherTab() {
		// START main table stuff
		new TabSetupHelper<PublisherTableEntry>().setUpTable(publisherMainTable, publisherMainTableList, this::showPublisher);
		new PagingSetupHelper().setUpPaging(publisherNextPageButton, publisherPreviousPageButton, publisherCurrentPageField, publisherQueryPage, this::loadDataPublisherTab);
		
		// TODO Delete binding
		// END main table stuff
		
		// START proceeding table stuff
		new TabSetupHelper<SecondaryProceedingTableEntry>().setUpTable(publisherProceedingTable, publisherProceedingTableList, this::showProceeding);
		// END proceeding table stuff
	}
	
	private void loadDataPublisherTab() {
		publisherMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Publisher.class);
        query.setRange((publisherQueryPage[0]-1)*PAGE_SIZE, publisherQueryPage[0]*PAGE_SIZE);
        Collection<Publisher> publishers = (Collection<Publisher>) query.execute();

        for (Publisher puber: publishers) {
        	publisherMainTableList.add(new PublisherTableEntry(puber));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	private void showPublisher(long objectId) {
		pm.currentTransaction().begin();
		Publisher puber = (Publisher) pm.getObjectById(objectId);
		
		publisherNameField.setText(puber.getName());
		
		publisherProceedingFilterField.setText("");
		publisherProceedingTableList.clear();
		for (Publication pub : puber.getPublications()) {
			if (pub instanceof Proceedings) {
				Proceedings proc = (Proceedings) pub;
				publisherProceedingTableList.add(new SecondaryProceedingTableEntry(proc));
			}
        }
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(publisherTab);
	}
	// End section for publisher tab
	
	
	// START section for conferences tab
	private ObservableList<ConferenceTableEntry> conferenceMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryConferenceEditionTableEntry> conferenceEditionTableList = FXCollections.observableArrayList();
	private int[] conferenceQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	public void setUpConferenceTab() {
		// START main table stuff
		new TabSetupHelper<ConferenceTableEntry>().setUpTable(conferenceMainTable, conferenceMainTableList, this::showConference);
		new PagingSetupHelper().setUpPaging(conferenceNextPageButton, conferencePreviousPageButton, conferenceCurrentPageField, conferenceQueryPage, this::loadDataConferenceTab);
		
		// TODO Delete binding
		// END main table stuff
		
		// START edition table stuff
		new TabSetupHelper<SecondaryConferenceEditionTableEntry>().setUpTable(conferenceEditionTable, conferenceEditionTableList, this::showConferenceEdition);
		// END edition table stuff
	}
	
	public void loadDataConferenceTab() {
		conferenceMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Conference.class);
        query.setRange((conferenceQueryPage[0]-1)*PAGE_SIZE, conferenceQueryPage[0]*PAGE_SIZE);
        Collection<Conference> conferences = (Collection<Conference>) query.execute();

        for (Conference conf: conferences) {
        	conferenceMainTableList.add(new ConferenceTableEntry(conf));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	public void showConference(long objectId) {
		pm.currentTransaction().begin();
		Conference conf = (Conference) pm.getObjectById(objectId);
		
		conferenceNameField.setText(conf.getName());
		
		conferenceEditionFilterField.setText("");
		conferenceEditionTableList.clear();
		for (ConferenceEdition confEd : conf.getEditions()) {
			conferenceEditionTableList.add(new SecondaryConferenceEditionTableEntry(confEd));
		}
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(conferenceTab);
	}
	// END section for conferences tab
	
	
	// START section for conference editions tab
	private ObservableList<ConferenceEditionTableEntry> conferenceEditionMainTableList = FXCollections.observableArrayList();
	private int[] conferenceEditionQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	public void setUpConferenceEditionTab() {
		// START main table stuff
		new TabSetupHelper<ConferenceEditionTableEntry>().setUpTable(conferenceEditionMainTable, conferenceEditionMainTableList, this::showConferenceEdition);
		new PagingSetupHelper().setUpPaging(conferenceEditionNextPageButton, conferenceEditionPreviousPageButton, conferenceEditionCurrentPageField, conferenceEditionQueryPage, this::loadDataConferenceEditionTab);
		
		// TODO Delete binding
		// END main table stuff
	}
	
	public void loadDataConferenceEditionTab() {
		conferenceEditionMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(ConferenceEdition.class);
        query.setRange((conferenceEditionQueryPage[0]-1)*PAGE_SIZE, conferenceEditionQueryPage[0]*PAGE_SIZE);
        Collection<ConferenceEdition> conferenceEditions = (Collection<ConferenceEdition>) query.execute();

        for (ConferenceEdition confEd: conferenceEditions) {
        	conferenceEditionMainTableList.add(new ConferenceEditionTableEntry(confEd));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	public void showConferenceEdition(long objectId) {
		pm.currentTransaction().begin();
		ConferenceEdition confEd = (ConferenceEdition) pm.getObjectById(objectId);
		
		Conference conf = confEd.getConference();
		
		if (null != conf) {
			conferenceEditionNameField.setText(conf.getName());
		} else {
			conferenceEditionNameField.setText("");
		}
		
		int year = confEd.getYear();
		if (0 != year) {
			conferenceEditionEditionField.setText(Integer.toString(year));
		} else {
			conferenceEditionEditionField.setText("No year");
		}
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(conferenceEditionTab);
	}
	// END section for conference editions tab
	
	
	// START section for series tab
	private ObservableList<SeriesTableEntry> seriesMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryProceedingTableEntry> seriesProceedingTableList = FXCollections.observableArrayList();
	private int[] seriesQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	public void setUpSeriesTab() {
		// START main table stuff
		new TabSetupHelper<SeriesTableEntry>().setUpTable(seriesMainTable, seriesMainTableList, this::showSeries);
		new PagingSetupHelper().setUpPaging(seriesNextPageButton, seriesPreviousPageButton, seriesCurrentPageField, seriesQueryPage, this::loadDataSeriesTab);
		
		// TODO Delete binding
		// END main table stuff
		
		// START proceeding table stuff
		new TabSetupHelper<SecondaryProceedingTableEntry>().setUpTable(seriesProceedingTable, seriesProceedingTableList, this::showProceeding);
		// END proceeding table stuff
	}
	
	public void loadDataSeriesTab() {
		seriesMainTableList.clear();
		pm.currentTransaction().begin();

        Query query = pm.newQuery(Series.class);
        query.setRange((seriesQueryPage[0]-1)*PAGE_SIZE, seriesQueryPage[0]*PAGE_SIZE);
        Collection<Series> seriesPlural = (Collection<Series>) query.execute();

        for (Series series: seriesPlural) {
        	seriesMainTableList.add(new SeriesTableEntry(series));
        }
        
        query.closeAll();
        pm.currentTransaction().commit();
	}
	
	public void showSeries(long objectId) {
		pm.currentTransaction().begin();
		Series series = (Series) pm.getObjectById(objectId);
		
		seriesNameField.setText(series.getName());
		
		seriesProceedingFilterField.setText("");
		seriesProceedingTableList.clear();
		for (Publication pub : series.getPublications()) {
			if (pub instanceof Proceedings) {
				seriesProceedingTableList.add(new SecondaryProceedingTableEntry((Proceedings)pub));
			}
		}
		
		pm.currentTransaction().commit();
		tabPane.getSelectionModel().select(seriesTab);
	}
	// END section for series tab
	
	
    protected void onImport(ActionEvent event){
    	System.out.printf("Test");
    }
    
    
    protected void setImportStatus(String text){
    	importStatusLabel.setText(text);
    }
    
    // START section for table entry data types
    protected abstract class TableEntry {
    	public long objectId;
    	public String getColumnContent(int i) {
    		return "";
    	};
    }
    
    private class PersonTableEntry extends TableEntry{
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
        	
        	objectId = person.jdoZooGetOid();
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
       
    private class PublicationTableEntry extends TableEntry{
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
        	
        	if (pub instanceof Proceedings) {
        		objectId = ((Proceedings)pub).jdoZooGetOid();
        	} else {
        		objectId = ((InProceedings)pub).jdoZooGetOid();
        	}
        	
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
    
    private class ProceedingTableEntry extends TableEntry{
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
    		
        	
        	objectId = proc.jdoZooGetOid();
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
    
    private class InProceedingTableEntry extends TableEntry{
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
    		
        	objectId = inProc.jdoZooGetOid();
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
    
    private class PublisherTableEntry extends TableEntry{
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
    		
    		objectId = puber.jdoZooGetOid();
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
    
    private class SecondaryPersonTableEntry extends TableEntry {
    	public String name;
    	public SecondaryPersonTableEntry(Person person) {
    		objectId = person.jdoZooGetOid();
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
    
    
    private class ConferenceTableEntry extends TableEntry{
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
        	
    		objectId = conf.jdoZooGetOid();
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
    
    private class ConferenceEditionTableEntry extends TableEntry{
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
    		
    		objectId = confEd.jdoZooGetOid();
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
    
    private class SeriesTableEntry extends TableEntry{
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
    		
    		objectId = ser.jdoZooGetOid();
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
    
    private class SecondaryProceedingTableEntry extends TableEntry {
    	public String title;
    	public String conference;
    	public SecondaryProceedingTableEntry(Proceedings proc) {
    		objectId = proc.jdoZooGetOid();
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
    
    
    private class SecondaryInProceedingTableEntry extends TableEntry {
    	public String title;
    	public String proceeding;
    	public SecondaryInProceedingTableEntry(InProceedings inProc) {
    		objectId = inProc.jdoZooGetOid();
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
    
    private class SecondaryConferenceEditionTableEntry extends TableEntry {
    	public String year;
    	public SecondaryConferenceEditionTableEntry(ConferenceEdition confEdit) {
    		int y = confEdit.getYear();
    		if (0 != y) {
    			year = Integer.toString(y);
    		} else {
    			year = "No year";
    		}
    		objectId = confEdit.jdoZooGetOid();
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
    
    // an abstract paging handler that can be reused for all tabs and directions, by giving 
    // the constructor the specific text field, counter, function and paging direction
    private class PagingHandler implements EventHandler{
		private int[] pageCounter;
		private TextField currentPageField;
		private int direction;
		private Runnable loadData;
		
		public PagingHandler(int[] pc, TextField cp, int dir, Runnable ld) {
			pageCounter = pc;
			currentPageField = cp;
			direction = dir;
			loadData = ld;
		}
		
		@Override
		public void handle(Event event) {
			try {
				int t = Integer.parseInt(currentPageField.getText()) + direction;
				if (t >= 1) {
					if (0 != direction) {
						currentPageField.setText(Integer.toString(t));
					}
					pageCounter[0] = t;
					loadData.run();
				}
			} catch (NumberFormatException e) {
				// Do nothing
			}
		}
	}
    
    // much the same for the row factory an abstract type that should work for all tabs
    protected class MyRowFactory<T extends TableEntry> implements Callback<TableView<T>, TableRow<T>> {
    	Consumer<Long> show;
    	
    	public MyRowFactory(Consumer<Long> s) {
    		show = s;
    	}
    	
		@Override
		public TableRow<T> call(TableView<T> tv) {
			TableRow<T> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (! row.isEmpty() && event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 2) {
		        	T item = row.getItem();
		            show.accept(item.objectId);
		        }
		    });
		    return row ;
		}
		
	}
    
    private class DeleteHandler<T extends TableEntry> implements EventHandler<ActionEvent>{
    	Consumer<Long> delete;
    	TableView<T> table;
    	
    	public DeleteHandler(TableView<T> t, Consumer<Long> d) {
    		delete = d;
    		table = t;
    	}
    	
		@Override
		public void handle(ActionEvent event) {
			T pte = table.getSelectionModel().getSelectedItem();
			if (null != pte) {
				delete.accept(pte.objectId);
			} else {
				System.err.println("cannot delete, no row selected");
			}
			
		}
		
	}
    
    private class PagingSetupHelper  {
    	public void setUpPaging(Button buttonNext, Button buttonPrevious, TextField fieldCurrent, int[] pageNumber, Runnable loader) {
    		buttonNext.setOnAction(new PagingHandler(pageNumber, fieldCurrent, 1, loader));
    		buttonPrevious.setOnAction(new PagingHandler(pageNumber, fieldCurrent, -1, loader));
    		fieldCurrent.setOnAction(new PagingHandler(pageNumber, fieldCurrent, 0, loader));
    	}
    }
    
    private class TabSetupHelper<T extends TableEntry>  {
    	
    	public void setUpTable(TableView<T> tableView, ObservableList<T> dataList, Consumer<Long> showFunction) {
    		
    		ObservableList<TableColumn<T, ?>> columns = tableView.getColumns();
    		
    		for (int i = 0; i < columns.size(); i++) {
    			TableColumn<T,String> column = (TableColumn<T,String>) columns.get(i);
    			final int fin = i;
    			column.setCellValueFactory(new Callback<CellDataFeatures<T, String>, ObservableValue<String>>() {
    				public ObservableValue<String> call(CellDataFeatures<T, String> p) {
    					return new ReadOnlyObjectWrapper<String>(p.getValue().getColumnContent(fin));
    				}
    			});
    			
    		}
    		tableView.setRowFactory(new MyRowFactory<T>(showFunction));
    		tableView.setItems(dataList);
    	}
    }
    
    // START section for fields that reference FXML
    @FXML    private TabPane tabPane;
    @FXML    private Tab personTab;
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
    @FXML    private Button personAddEditorButton;
    @FXML    private TextField personProceedingFilterField;
    @FXML    private TableView<SecondaryProceedingTableEntry> personProceedingTable;
    @FXML    private Button personRemoveProceedingButton;
    @FXML    private Button personAddAuthorButton;
    @FXML    private TextField personInProceedingFilterField;
    @FXML    private TableView<SecondaryInProceedingTableEntry> personInProceedingTable;
    @FXML    private Button personRemoveInProceedingButton;
    @FXML    private Tab proceedingTab;
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
    @FXML    private Tab inProceedingTab;
    @FXML    private TableView<InProceedingTableEntry> inProceedingMainTable;
    @FXML    private TextField inProceedingProceedingFilterField;
    @FXML    private Button inProceedingSearchButton;
    @FXML    private Button inProceedingDeleteButton;
    @FXML    private Button inProceedingNextPageButton;
    @FXML    private Button inProceedingPreviousPageButton;
    @FXML    private TextField inProceedingCurrentPageField;
    @FXML    private Button inProceedingCreateButton;
    @FXML    private TextField inProceedingPagesField;
    @FXML    private Button inProceedingChangePagesButton;
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
    @FXML    private Tab publicationTab;
    @FXML    private TableView<PublicationTableEntry> publicationMainTable;
    @FXML    private TextField publicationSearchField;
    @FXML    private Button publicationSearchButton;
    @FXML    private Button publicationDeleteButton;
    @FXML    private Button publicationNextPageButton;
    @FXML    private Button publicationPreviousPageButton;
    @FXML    private TextField publicationCurrentPageField;
    @FXML    private Tab publisherTab;
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
    @FXML    private Tab conferenceTab;
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
    @FXML    private Tab conferenceEditionTab;
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
    @FXML    private Tab seriesTab;
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
    @FXML    private Tab importTab;
    @FXML    private Button importButton;
    @FXML    private Label importStatusLabel;
    // END section for fields that reference FXML
}