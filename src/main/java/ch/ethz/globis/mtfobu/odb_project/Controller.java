package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
 
public class Controller {
	private final int PAGE_SIZE = 20;
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
	            	loadDataPersonTab();
	            }
	            if (newTab == proceedingTab) {
	            	loadDataProceedingTab();
	            }
	            if (newTab == inProceedingTab) {
	            	loadDataInProceedingTab();
	            }
	            if (newTab == publicationTab) {
	            	loadDataPublicationTab();
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
		
    	setUpPersonTab();
    	setUpProceedingTab();
    	setUpInProceedingTab();
    	setUpPublicationTab();
    	
    	
    	
		loadDataPersonTab();
    }
    
    // START section for person tab
    private ObservableList<PersonTableEntry> personMainTableList = FXCollections.observableArrayList();
    private ObservableList<SecondaryProceedingTableEntry> personProceedingTableList = FXCollections.observableArrayList();
    private ObservableList<SecondaryInProceedingTableEntry> personInProceedingTableList = FXCollections.observableArrayList();
    private int[] personQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
    
    private void setUpPersonTab() {
    	
    	// START main table stuff
    	ObservableList<TableColumn<PersonTableEntry, ?>> mainTableColumns = personMainTable.getColumns();
		TableColumn<PersonTableEntry,String> mainNameCol = (TableColumn<PersonTableEntry,String>) mainTableColumns.get(0);
		TableColumn<PersonTableEntry,String> mainAuthEditCol = (TableColumn<PersonTableEntry,String>) mainTableColumns.get(1);
		
		mainNameCol.setCellValueFactory(new Callback<CellDataFeatures<PersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().name);
			}
		});
		
		mainAuthEditCol.setCellValueFactory(new Callback<CellDataFeatures<PersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().authoredEdited);
			}
		});
		
		personMainTable.setRowFactory(new MyRowFactory<PersonTableEntry>(this::showPerson));
		
		personNextPageButton.setOnAction(new PagingHandler(personQueryPage, personCurrentPageField, 1, this::loadDataPersonTab));
		personPreviousPageButton.setOnAction(new PagingHandler(personQueryPage, personCurrentPageField, -1, this::loadDataPersonTab));
		personCurrentPageField.setOnAction(new PagingHandler(personQueryPage, personCurrentPageField, 0, this::loadDataPersonTab));
		
		personMainTable.setItems(personMainTableList);
    	// END main table stuff
		
		// START proceeding table stuff
		ObservableList<TableColumn<SecondaryProceedingTableEntry, ?>> proceedingTableColumns = personProceedingTable.getColumns();
		TableColumn<SecondaryProceedingTableEntry,String> proceedingTitleCol = (TableColumn<SecondaryProceedingTableEntry,String>) proceedingTableColumns.get(0);
		TableColumn<SecondaryProceedingTableEntry,String> proceedingConferenceCol = (TableColumn<SecondaryProceedingTableEntry,String>) proceedingTableColumns.get(1);
		
		proceedingTitleCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryProceedingTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().title);
			}
		});
		
		proceedingConferenceCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryProceedingTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().conference);
			}
		});
		
		personProceedingTable.setRowFactory(new MyRowFactory<SecondaryProceedingTableEntry>(this::showProceeding));
		
		personProceedingTable.setItems(personProceedingTableList);
		// END proceeding table stuff
		
		// START inproceeding table stuff
		ObservableList<TableColumn<SecondaryInProceedingTableEntry, ?>> inProceedingTableColumns = personInProceedingTable.getColumns();
		TableColumn<SecondaryInProceedingTableEntry,String> inProceedingTitleCol = (TableColumn<SecondaryInProceedingTableEntry,String>) inProceedingTableColumns.get(0);
		TableColumn<SecondaryInProceedingTableEntry,String> inProceedingProceedingCol = (TableColumn<SecondaryInProceedingTableEntry,String>) inProceedingTableColumns.get(1);
		
		inProceedingTitleCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryInProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryInProceedingTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().title);
			}
		});
		
		inProceedingProceedingCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryInProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryInProceedingTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().proceeding);
			}
		});
		
		personInProceedingTable.setRowFactory(new MyRowFactory<SecondaryInProceedingTableEntry>(this::showInProceeding));
		
		personInProceedingTable.setItems(personInProceedingTableList);
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
	// END section for person tab
	
	
	// START section for proceeding tab
	private ObservableList<ProceedingTableEntry> proceedingMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryPersonTableEntry> proceedingEditorTableList = FXCollections.observableArrayList();
	private int[] proceedingQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
    
	private void setUpProceedingTab() {
		
		// START main table stuff
    	ObservableList<TableColumn<ProceedingTableEntry, ?>> mainTableColumns = proceedingMainTable.getColumns();
		TableColumn<ProceedingTableEntry,String> mainTitleCol = (TableColumn<ProceedingTableEntry,String>) mainTableColumns.get(0);
		TableColumn<ProceedingTableEntry,String> mainPublisherCol = (TableColumn<ProceedingTableEntry,String>) mainTableColumns.get(1);
		TableColumn<ProceedingTableEntry,String> mainConferenceCol = (TableColumn<ProceedingTableEntry,String>) mainTableColumns.get(2);
		
		mainTitleCol.setCellValueFactory(new Callback<CellDataFeatures<ProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<ProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().title);
			}
		});
		
		mainPublisherCol.setCellValueFactory(new Callback<CellDataFeatures<ProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<ProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().publisher);
			}
		});
		
		mainConferenceCol.setCellValueFactory(new Callback<CellDataFeatures<ProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<ProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().conference);
			}
		});
		
		proceedingMainTable.setRowFactory(new MyRowFactory<ProceedingTableEntry>(this::showProceeding));
		
		proceedingNextPageButton.setOnAction(new PagingHandler(proceedingQueryPage, proceedingCurrentPageField, 1, this::loadDataProceedingTab));
		proceedingPreviousPageButton.setOnAction(new PagingHandler(proceedingQueryPage, proceedingCurrentPageField, -1, this::loadDataProceedingTab));
		proceedingCurrentPageField.setOnAction(new PagingHandler(proceedingQueryPage, proceedingCurrentPageField, 0, this::loadDataProceedingTab));
		
		proceedingMainTable.setItems(proceedingMainTableList);
		// END main table stuff
		
		// START editor table stuff
		ObservableList<TableColumn<SecondaryPersonTableEntry, ?>> editorTableColumns = proceedingEditorTable.getColumns();
		TableColumn<SecondaryPersonTableEntry,String> editorNameCol = (TableColumn<SecondaryPersonTableEntry,String>) editorTableColumns.get(0);
		
		editorNameCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryPersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryPersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().name);
			}
		});
		
		
		proceedingEditorTable.setRowFactory(new MyRowFactory<SecondaryPersonTableEntry>(this::showPerson));
		
		proceedingEditorTable.setItems(proceedingEditorTableList);
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
	// END section for proceeding tab
	
	
	// START section for inproceeding tab
	private ObservableList<InProceedingTableEntry> inProceedingMainTableList = FXCollections.observableArrayList();
	private ObservableList<SecondaryPersonTableEntry> inProceedingAuthorTableList = FXCollections.observableArrayList();
	private int[] inProceedingQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	private void setUpInProceedingTab() {
		
		// START main table stuff
    	ObservableList<TableColumn<InProceedingTableEntry, ?>> mainTableColumns = inProceedingMainTable.getColumns();
		TableColumn<InProceedingTableEntry,String> mainTitleCol = (TableColumn<InProceedingTableEntry,String>) mainTableColumns.get(0);
		TableColumn<InProceedingTableEntry,String> mainYearCol = (TableColumn<InProceedingTableEntry,String>) mainTableColumns.get(1);
		TableColumn<InProceedingTableEntry,String> mainProceedingCol = (TableColumn<InProceedingTableEntry,String>) mainTableColumns.get(2);
		
		mainTitleCol.setCellValueFactory(new Callback<CellDataFeatures<InProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<InProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().title);
			}
		});
		
		mainYearCol.setCellValueFactory(new Callback<CellDataFeatures<InProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<InProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().year);
			}
		});
		
		mainProceedingCol.setCellValueFactory(new Callback<CellDataFeatures<InProceedingTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<InProceedingTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().proceeding);
			}
		});
		
		inProceedingMainTable.setRowFactory(new MyRowFactory<InProceedingTableEntry>(this::showInProceeding));
		
		inProceedingNextPageButton.setOnAction(new PagingHandler(inProceedingQueryPage, inProceedingCurrentPageField, 1, this::loadDataInProceedingTab));
		inProceedingPreviousPageButton.setOnAction(new PagingHandler(inProceedingQueryPage, inProceedingCurrentPageField, -1, this::loadDataInProceedingTab));
		inProceedingCurrentPageField.setOnAction(new PagingHandler(inProceedingQueryPage, inProceedingCurrentPageField, 0, this::loadDataInProceedingTab));
		
		inProceedingMainTable.setItems(inProceedingMainTableList);
		// END main table stuff
		
		// START author table stuff
		ObservableList<TableColumn<SecondaryPersonTableEntry, ?>> authorTableColumns = inProceedingAuthorTable.getColumns();
		TableColumn<SecondaryPersonTableEntry,String> authorNameCol = (TableColumn<SecondaryPersonTableEntry,String>) authorTableColumns.get(0);
		
		authorNameCol.setCellValueFactory(new Callback<CellDataFeatures<SecondaryPersonTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SecondaryPersonTableEntry, String> p) {
				return new ReadOnlyObjectWrapper<String>(p.getValue().name);
			}
		});
		
		
		inProceedingAuthorTable.setRowFactory(new MyRowFactory<SecondaryPersonTableEntry>(this::showPerson));
		
		inProceedingAuthorTable.setItems(inProceedingAuthorTableList);
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
    // END section for inproceeding tab
	
	
	// START section for publication tab
	private ObservableList<PublicationTableEntry> publicationMainTableList = FXCollections.observableArrayList();
	private int[] publicationQueryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	
	private void setUpPublicationTab() {
		// START main table stuff
    	ObservableList<TableColumn<PublicationTableEntry, ?>> mainTableColumns = publicationMainTable.getColumns();
		TableColumn<PublicationTableEntry,String> mainTitleCol = (TableColumn<PublicationTableEntry,String>) mainTableColumns.get(0);
		TableColumn<PublicationTableEntry,String> mainAuthEditCol = (TableColumn<PublicationTableEntry,String>) mainTableColumns.get(1);
		
		mainTitleCol.setCellValueFactory(new Callback<CellDataFeatures<PublicationTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PublicationTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().title);
			}
		});
		
		mainAuthEditCol.setCellValueFactory(new Callback<CellDataFeatures<PublicationTableEntry, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<PublicationTableEntry, String> proc) {
				return new ReadOnlyObjectWrapper<String>(proc.getValue().authorsEditors);
			}
		});
		
		
		
		publicationMainTable.setRowFactory(new MyRowFactory<PublicationTableEntry>(this::showPublication));
		
		publicationNextPageButton.setOnAction(new PagingHandler(publicationQueryPage, publicationCurrentPageField, 1, this::loadDataPublicationTab));
		publicationPreviousPageButton.setOnAction(new PagingHandler(publicationQueryPage, publicationCurrentPageField, -1, this::loadDataPublicationTab));
		publicationCurrentPageField.setOnAction(new PagingHandler(publicationQueryPage, publicationCurrentPageField, 0, this::loadDataPublicationTab));
		
		publicationMainTable.setItems(publicationMainTableList);
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
	// End section for publication tab
	
    protected void onImport(ActionEvent event){
    	System.out.printf("Test");
    }
    
    
    protected void setImportStatus(String text){
    	importStatusLabel.setText(text);
    }
    
    // START section for table entry data types
    private abstract class TableEntry {
    	public long objectId;
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
    }
    
    private class SecondaryPersonTableEntry extends TableEntry {
    	public String name;
    	public SecondaryPersonTableEntry(Person person) {
    		objectId = person.jdoZooGetOid();
        	name = person.getName();
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
    private class MyRowFactory<T extends TableEntry> implements Callback<TableView<T>, TableRow<T>> {
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
    @FXML    private TableView<?> publisherMainTable;
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
    @FXML    private ChoiceBox<?> publisherInProceedingDropdown;
    @FXML    private Button publisherAddInProceedingButton;
    @FXML    private TextField publisherInProceedingFilterField;
    @FXML    private TableView<?> publisherProdeedingTable;
    @FXML    private Button publisherRemoveProceedingButton;
    @FXML    private TableView<?> publisherInProceedingTable;
    @FXML    private Button publisherRemoveInProceedingButton;
    @FXML    private Tab conferenceTab;
    @FXML    private TableView<?> conferenceMainTable;
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
    @FXML    private TableView<?> conferenceEditionTable;
    @FXML    private Button conferenceRemoveEditionButton;
    @FXML    private Tab conferenceEditionTab;
    @FXML    private TableView<?> conferenceEditionMainTable;
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
    @FXML    private TableView<?> conferenceEditionProceedingTable;
    @FXML    private Button conferenceEditionRemoveProceedingButton;
    @FXML    private Tab seriesTab;
    @FXML    private TableView<?> serieMainTable;
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
    @FXML    private TableView<?> seriesProceedingTable;
    @FXML    private Button seriesRemoveProceedingButton;
    @FXML    private Tab importTab;
    @FXML    private Button importButton;
    @FXML    private Label importStatusLabel;
    // END section for fields that reference FXML
}