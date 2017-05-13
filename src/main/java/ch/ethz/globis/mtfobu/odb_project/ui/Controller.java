package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.jdo.PersistenceManager;

import org.basex.query.value.item.Int;
import org.zoodb.jdo.ZooJdoHelper;

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.ConferenceEdition;
import ch.ethz.globis.mtfobu.domains.DomainObject;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.domains.Series;
import ch.ethz.globis.mtfobu.odb_project.Config;
import ch.ethz.globis.mtfobu.odb_project.XmlImport;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseBaseX;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseManager;
import ch.ethz.globis.mtfobu.odb_project.db.ZooImport;
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
    private Database db;
    private DatabaseManager.DBType selectedDB;
    private final DatabaseManager dbm = new DatabaseManager();

    // mainTable DATA
    private Hashtable<DatabaseManager.DBType, TableData> tableData = new Hashtable<>();

    // TODO task4: Class XmlImport needs this function
    public void setImportStatus(String text) {
	// TODO Auto-generated method stub

    }

    public void initialize() {
	// build empty hash
	for (DatabaseManager.DBType type : DatabaseManager.DBType.values()) {
	    tableData.put(type, new TableData());
	}

	// select initial database type
	selectedDB = DatabaseManager.DBType.ZooDB;

	// get db instance of selected type
	db = dbm.getDB(selectedDB);

	initAllColumns();

	new Thread(new Runnable() {
	    public void run() {
		loadPeople();
		loadProceedings();
		loadPublishers();
		loadConf();
		loadConfEds();
		loadSeries();
		//loadPublications();
		loadInProceedings();
	    }
	}).start();

    }

    private void initAllColumns() {
	initializePeopleMainColumns();
	initializeProceedingsMainColumns();
	initializeInproceedingsMainColumns();
	initializePublicationsMainColumns();
	initializePublishersMainColumns();
	initializeConfMainColumns();
	initializeConfEdMainColumns();
	initializeSeriesMainColumns();

    }

    // general

    // START Series

    private void loadSeries() {
	// init table data
	List<Series> ps = db.getSeries();
	tableData.get(selectedDB).seriesMasterData.addAll(ps);
	FilteredList<Series> filteredData = setTable(tableData.get(selectedDB).seriesMasterData, seriesMainTable);

	// search field
	searchSetupSeries(filteredData);
	seriesTab.setDisable(false);
    }

    private void searchSetupSeries(FilteredList<Series> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	seriesSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(inProc -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (inProc.getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else {
		    for (Publication p : inProc.getPublications()) {
			if (String.valueOf(p.getTitle()).toLowerCase().contains(lowerCaseFilter))
			    return true;
		    }
		}
		return false; // Does not match.
	    });
	});
    }

    private void initializeSeriesMainColumns() {
	// Initialize InProceedings Columns
	seriesNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

	seriesProcsColumn.setCellValueFactory(cellData -> {
	    String editions = "";
	    for (Publication p : cellData.getValue().getPublications()) {
		editions += p.getTitle() + ", ";
	    }
	    return new SimpleStringProperty(editions);
	});

	// initialize authors column
	seriesPubsTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	// seriesPubsTitleColumn.setCellValueFactory(cellData -> new
	// SimpleStringProperty(((Proceedings)cellData.getValue()).getConference().getName()));
    }

    @FXML
    public void seriesMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{

	    // initialize data
	    Series selected = seriesMainTable.getSelectionModel().getSelectedItem();
	    loadSeriesProcs(selected);

	    // editable fields
	    seriesNameField.setText(selected.getName());

	}
    }

    private void loadSeriesProcs(Series selected) {
	Set<Publication> ce = selected.getPublications();
	ObservableList<Publication> masterData = FXCollections.observableArrayList();
	masterData.addAll(ce);

	// load table
	FilteredList<Publication> filteredData = setTable(masterData, seriesProceedingTable);
    }

    @FXML
    private void updateSeries(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Series selected = seriesMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "seriesChangeNameButton":
	    selected.setName(seriesNameField.getText());
	    seriesMainTable.refresh();
	    break;
	case "seriesDeleteButton":
	    tableData.get(selectedDB).seriesMasterData.remove(selected);
	    seriesMainTable.refresh();
	    break;
	case "seriesRemoveProceedingButton":
	    Publication p = seriesProceedingTable.getSelectionModel().getSelectedItem();
	    selected.getPublications().remove(p);
	    loadSeriesProcs(selected);
	    seriesMainTable.refresh();
	    break;
	default:
	    break;
	}

    }

    // Start confEds
    // END Series

    // START ConferenceEditions
    private void loadConfEds() {
	// init table data
	List<ConferenceEdition> ps = db.getConferenceEditions();
	tableData.get(selectedDB).confEdMasterData.addAll(ps);
	FilteredList<ConferenceEdition> filteredData = setTable(tableData.get(selectedDB).confEdMasterData,
		conferenceEditionMainTable);

	// search field
	searchSetupConfEds(filteredData);
	conferenceEditionTab.setDisable(false);
    }

    private void searchSetupConfEds(FilteredList<ConferenceEdition> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	conferenceEditionSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(inProc -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (inProc.getConference().getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else if (String.valueOf(inProc.getYear()).toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else if (inProc.getProceedings().getTitle().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		}
		return false; // Does not match.
	    });
	});
    }

    private void initializeConfEdMainColumns() {
	// Initialize InProceedings Columns
	confEdNameColumn.setCellValueFactory(
		cellData -> new SimpleStringProperty(cellData.getValue().getConference().getName()));
	confEdEditionColumn.setCellValueFactory(
		cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
	confEdProceedingColumn.setCellValueFactory(
		cellData -> new SimpleStringProperty(cellData.getValue().getProceedings().getTitle()));
    }

    @FXML
    public void confEdMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{

	    // initialize data
	    ConferenceEdition selected = conferenceEditionMainTable.getSelectionModel().getSelectedItem();

	    // editable fields
	    conferenceEditionNameField.setText(selected.getConference().getName());
	    conferenceEditionEditionField.setText(String.valueOf(selected.getYear()));

	}
    }

    @FXML
    private void updateConfEds(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	ConferenceEdition selected = conferenceEditionMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "conferenceEditionChangeNameButton":
	    selected.getConference().setName(conferenceEditionNameField.getText());
	    conferenceEditionMainTable.refresh();
	    break;
	case "conferenceEditionChangeEditionButton":
	    selected.setYear(Integer.parseInt(conferenceEditionEditionField.getText()));
	    conferenceEditionMainTable.refresh();
	case "conferenceEditionDeleteButton":
	    tableData.get(selectedDB).confEdMasterData.remove(selected);
	    conferenceEditionMainTable.refresh();
	    break;
	default:
	    break;
	}

    }

    // END ConferencesEditions

    // START Conferences
    private void loadConf() {
	// init table data
	List<Conference> ps = db.getConferences();
	tableData.get(selectedDB).confMasterData.addAll(ps);
	FilteredList<Conference> filteredData = setTable(tableData.get(selectedDB).confMasterData, conferenceMainTable);

	// search field
	searchSetupConf(filteredData);
	conferenceTab.setDisable(false);
    }

    private void searchSetupConf(FilteredList<Conference> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	conferenceSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(inProc -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (inProc.getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else {
		    for (ConferenceEdition p : inProc.getEditions()) {
			if (String.valueOf(p.getYear()).toLowerCase().contains(lowerCaseFilter))
			    return true;
		    }
		}
		return false; // Does not match.
	    });
	});
    }

    private void initializeConfMainColumns() {
	// Initialize InProceedings Columns
	confMainTableNameColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

	confMainTableEditionsColumn.setCellValueFactory(cellData -> {
	    String editions = "";
	    for (ConferenceEdition p : cellData.getValue().getEditions()) {
		editions += p.getYear() + ", ";
	    }
	    return new SimpleStringProperty(editions);
	});

	// initialize authors column
	confEditionsYearColumn.setCellValueFactory(
		cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));

    }

    @FXML
    public void confMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{

	    // initialize data
	    Conference selected = conferenceMainTable.getSelectionModel().getSelectedItem();
	    loadConfEditions(selected);

	    // editable fields
	    conferenceNameField.setText(selected.getName());

	}
    }

    private void loadConfEditions(Conference selected) {
	Set<ConferenceEdition> ce = selected.getEditions();
	ObservableList<ConferenceEdition> masterData = FXCollections.observableArrayList();
	masterData.addAll(ce);

	// load table
	FilteredList<ConferenceEdition> filteredData = setTable(masterData, conferenceEditionTable);
    }

    @FXML
    private void updateConf(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Conference selected = conferenceMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "conferenceChangeNameButton":
	    selected.setName(conferenceNameField.getText());
	    conferenceMainTable.refresh();
	    break;
	case "conferenceDeleteButton":
	    tableData.get(selectedDB).confMasterData.remove(selected);
	    conferenceMainTable.refresh();
	    break;
	case "conferenceRemoveEditionButton":
	    ConferenceEdition p = conferenceEditionTable.getSelectionModel().getSelectedItem();
	    selected.getEditions().remove(p);
	    loadConfEditions(selected);
	    conferenceMainTable.refresh();
	    break;
	default:
	    break;
	}

    }

    // END Conferences

    // START Publishers
    private void loadPublishers() {
	// init table data
	List<Publisher> ps = db.getPublishers();
	tableData.get(selectedDB).publishersMasterData.addAll(ps);
	FilteredList<Publisher> filteredData = setTable(tableData.get(selectedDB).publishersMasterData,
		publisherMainTable);

	// search field
	searchSetupPublishers(filteredData);
	publisherTab.setDisable(false);
    }

    private void searchSetupPublishers(FilteredList<Publisher> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	publisherSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(inProc -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (inProc.getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else {
		    for (Publication p : inProc.getPublications()) {
			if (p.getTitle().toLowerCase().contains(lowerCaseFilter))
			    return true;
		    }
		}
		return false; // Does not match.
	    });
	});
    }

    private void initializePublishersMainColumns() {
	// Initialize InProceedings Columns
	publisherMainTableNameColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

	publisherMainTablePublishedColumn.setCellValueFactory(cellData -> {
	    String published = "";
	    for (Publication p : cellData.getValue().getPublications()) {
		published += p.getTitle() + ", ";
	    }
	    return new SimpleStringProperty(published);
	});

	// initialize authors column
	publisherProceedingsTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	publisherProceedingsConferenceColumn.setCellValueFactory(cellData -> {
	    String name = "";
	    Proceedings p = cellData.getValue();
	    ConferenceEdition ce = p.getConferenceEdition(); // could be null
	    if (ce != null) {
		name = ce.getConference().getName();
	    }
	    return new SimpleStringProperty(name);
	});

    }

    @FXML
    public void publishersMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{

	    // initialize data
	    Publisher selected = publisherMainTable.getSelectionModel().getSelectedItem();
	    loadPublisherProceedings(selected);

	    // editable fields
	    publisherNameField.setText(selected.getName());

	}
    }

    private void loadPublisherProceedings(Publisher selected) {
	Set<Publication> authors = selected.getPublications();
	ObservableList<Publication> masterData = FXCollections.observableArrayList();
	masterData.addAll(authors);

	// load table
	FilteredList<Publication> filteredData = setTable(masterData, publisherProceedingTable);
    }

    @FXML
    private void updatePublishers(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Publisher selected = publisherMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "publisherChangeNameButton":
	    selected.setName(publisherNameField.getText());
	    publisherMainTable.refresh();
	    break;
	case "publisherDeleteButton":
	    tableData.get(selectedDB).publishersMasterData.remove(selected);
	    publisherMainTable.refresh();
	    break;
	case "publisherRemoveProceedingButton":
	    Publication p = publisherProceedingTable.getSelectionModel().getSelectedItem();
	    selected.getPublications().remove(p);
	    loadPublisherProceedings(selected);
	    publisherMainTable.refresh();
	    break;
	default:
	    break;
	}

    }

    // END Publishers

    // START Publications Tab
    private void initializePublicationsMainColumns() {
	// Initialize Proceedings Columns
	publicationMainTableTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	publicationMainTableAuthorsColumn.setCellValueFactory(cellData -> {
	    String authors = "";
	    for (Person p : cellData.getValue().getAuthors()) {
		authors += p.getName() + ", ";
	    }
	    return new SimpleStringProperty(authors);
	});
    }

    private void loadPublications() {
	// init table data
	List<Publication> pubs = db.getPublications();
	tableData.get(selectedDB).publicationsMasterData.addAll(pubs);

	FilteredList<Publication> filteredData = setTable(tableData.get(selectedDB).publicationsMasterData,
		publicationMainTable);

	// search field
	searchSetupPublications(filteredData);
	publicationTab.setDisable(false);
    }

    private void searchSetupPublications(FilteredList<Publication> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	publicationSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(p -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (p.getTitle().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else {
		    for (Person per : p.getAuthors()) {
			if (per.getName().toLowerCase().contains(lowerCaseFilter))
			    return true; // filter matches author
		    }
		    return false;
		}

	    });
	});
    }

    @FXML
    public void publicationsMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{

	}
    }

    @FXML
    private void updatePublications(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Publication selected = publicationMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "publicationDeleteButton":
	    tableData.get(selectedDB).publicationsMasterData.remove(selected);
	    publicationMainTable.refresh();
	    break;
	default:
	    break;
	}

    }

    // END Publications Tab

    // START Proceedings Tab
    private void initializeProceedingsMainColumns() {
	// Initialize Proceedings Columns
	proceedingsMainTableTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	proceedingsMainTablePublisherColumn.setCellValueFactory(cellData -> {
	    String name = "";
	    Publisher p = cellData.getValue().getPublisher();
	    if (p != null) {
		name = p.getName();
	    }
	    return new SimpleStringProperty(name);
	});

	proceedingsMainTableConferenceColumn.setCellValueFactory(cellData -> {
	    String name = "";
	    Proceedings p = cellData.getValue();
	    ConferenceEdition ce = p.getConferenceEdition(); // could be null
	    if (ce != null) {
		name = ce.getConference().getName();
	    }
	    return new SimpleStringProperty(name);
	});

	// initialize editors column
	proceedingEditorTableNameColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

    }

    private void loadProceedings() {
	// init table data
	List<Proceedings> procs = db.getProceedings();
	tableData.get(selectedDB).proceedingsMasterData.addAll(procs);

	FilteredList<Proceedings> filteredData = setTable(tableData.get(selectedDB).proceedingsMasterData,
		proceedingMainTable);

	// search field
	searchSetupProceedings(filteredData);
	proceedingTab.setDisable(false);
    }

    private void searchSetupProceedings(FilteredList<Proceedings> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	proceedingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(p -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (p.getTitle().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else if (p.getPublisher() != null
			&& p.getPublisher().getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else if (p.getConferenceEdition() != null && p.getConferenceEdition().getConference() != null
			&& p.getConferenceEdition().getConference().getName() != null
			&& p.getConferenceEdition().getConference().getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.

		}

		// TODO some conferences seem to have no name
		if (p.getConferenceEdition() != null && 
			p.getConferenceEdition().getConference() != null
			&& p.getConferenceEdition().getConference().getName() == null) {
		    System.err.println(p.getTitle());
		}
		return false; // Does not match.
	    });
	});
    }

    @FXML
    public void proceedingsMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{
	    // initialize data
	    Proceedings selected = proceedingMainTable.getSelectionModel().getSelectedItem();
	    loadProceedingsEditors(selected);

	    // editable fields
	    proceedingTitleField.setText(selected.getTitle());
	    proceedingIsbnField.setText(selected.getIsbn());

	}
    }

    private void loadProceedingsEditors(Proceedings selected) {
	List<Person> authors = selected.getAuthors();
	ObservableList<Person> masterData = FXCollections.observableArrayList();
	masterData.addAll(authors);

	// load table
	FilteredList<Person> filteredData = setTable(masterData, proceedingEditorTable);
    }

    @FXML
    private void updateProceedings(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Proceedings selected = proceedingMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "proceedingChangeTitleButton":
	    selected.setTitle(proceedingTitleField.getText());
	    proceedingMainTable.refresh();
	    break;
	case "proceedingChangeIsbnButton":
	    selected.setIsbn(proceedingIsbnField.getText());
	    proceedingMainTable.refresh();
	    break;
	case "proceedingDeleteButton":
	    tableData.get(selectedDB).proceedingsMasterData.remove(selected);
	    proceedingMainTable.refresh();
	    break;
	case "proceedingRemoveEditorButton":
	    Person p2 = proceedingEditorTable.getSelectionModel().getSelectedItem();
	    selected.getAuthors().remove(p2);
	    loadProceedingsEditors(selected);
	    break;
	default:
	    break;
	}

    }

    // END Proceedings Tab

    // START people tab
    private void loadPeople() {
	// init table data
	List<Person> people = db.getPeople();
	tableData.get(selectedDB).peopleMasterData.addAll(people);

	FilteredList<Person> filteredData = setTable(tableData.get(selectedDB).peopleMasterData, personMainTable);

	// search field
	searchSetupPeople(filteredData);
	personTab.setDisable(false);
    }

    private void initializePeopleMainColumns() {
	// Initialize InProceedings Columns
	personMainTableNameColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

	// proceedings
	peopleProceedingsTableTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	
	peopleProceedingsTableConferenceColumn.setCellValueFactory(cellData -> {
	    String name = "";
	    Proceedings p = cellData.getValue();
	    ConferenceEdition ce = p.getConferenceEdition(); // could be null
	    if (ce != null) {
		name = ce.getConference().getName();
	    }
	    return new SimpleStringProperty(name);
	});

	// inproceedings
	peopleInProceedingsTableTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	peopleInProceedingsTableProceedingsColumn.setCellValueFactory(cellData -> {
	    Proceedings proc = cellData.getValue().getProceedings();
	    if (proc != null)
		return new SimpleStringProperty(proc.getTitle());
	    else
		return new SimpleStringProperty("");
	});

    }

    private void searchSetupPeople(FilteredList<Person> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	personSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(p -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (p.getName().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		}
		return false; // Does not match.
	    });
	});
    }

    @FXML
    public void peopleMainClickItem(MouseEvent event) {
	if (event.getClickCount() > 0) // Checking double click
	{
	    // initialize data
	    Person selected = personMainTable.getSelectionModel().getSelectedItem();
	    preloadPeopleProceedings(selected);

	    // editable fields
	    personNameField.setText(selected.getName());

	}
    }

    private void preloadPeopleProceedings(Person selected) {
	// proceedings
	Set<Publication> procs = selected.getEditedPublications();
	if (procs == null) {
	    procs = db.getEditedPublications(selected.getName(), false);
	    selected.setEditedPublications(procs);
	}

	// inproceedings
	Set<Publication> inprocs = selected.getAuthoredPublications();
	if (inprocs == null) {
	    inprocs = db.getAuthoredPublications(selected.getName(), false);
	    selected.setAuthoredPublications(inprocs);
	}

	loadPeopleSubTables(procs, inprocs);
    }

    private void loadPeopleSubTables(Set<Publication> proceedings, Set<Publication> inProceedings) {
	ObservableList<Publication> masterData = FXCollections.observableArrayList();
	masterData.addAll(proceedings);

	// load table
	FilteredList<Publication> filteredData = setTable(masterData, personProceedingTable);

	// inproceedings
	masterData = FXCollections.observableArrayList();
	masterData.addAll(inProceedings);

	// load table
	filteredData = setTable(masterData, personInProceedingTable);
    }

    @FXML
    private void updatePeople(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	Person selected = personMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "personChangeNameButton":
	    selected.setName(personNameField.getText());
	    personMainTable.refresh();
	    break;
	case "personRemoveProceedingButton":
	    Publication p = personProceedingTable.getSelectionModel().getSelectedItem();
	    selected.getEditedPublications().remove(p);
	    loadPeopleSubTables(selected.getEditedPublications(), selected.getAuthoredPublications());
	    break;
	case "personDeleteButton":
	    tableData.get(selectedDB).peopleMasterData.remove(selected);
	    personMainTable.refresh();
	    break;
	case "personRemoveInProceedingButton":
	    Publication p2 = personInProceedingTable.getSelectionModel().getSelectedItem();
	    selected.getAuthoredPublications().remove(p2);
	    loadPeopleSubTables(selected.getEditedPublications(), selected.getAuthoredPublications());
	    break;
	default:
	    break;
	}

    }

    // END people tab

    // START InProceedings tab
    private void loadInProceedings() {
	// init table data
	List<InProceedings> inProcs = db.getInProceedings();
	tableData.get(selectedDB).inProceedingsMasterData.addAll(inProcs);
	FilteredList<InProceedings> filteredData = setTable(tableData.get(selectedDB).inProceedingsMasterData,
		inProceedingMainTable);

	// search field
	searchSetupInproceedings(filteredData);
	inProceedingTab.setDisable(false);
    }

    private void searchSetupInproceedings(FilteredList<InProceedings> filteredData) {

	// Set the filter Predicate whenever the filter changes.
	inProceedingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
	    filteredData.setPredicate(inProc -> {
		// If filter text is empty, display all
		if (newValue == null || newValue.isEmpty()) {
		    return true;
		}

		// Compare first name and last name of every person with filter
		// text.
		String lowerCaseFilter = newValue.toLowerCase();

		if (inProc.getTitle().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches title.
		} else if (String.valueOf(inProc.getYear()).contains(lowerCaseFilter)) {
		    return true; // Filter matches year.
		} else if (inProc.getProceedings() != null
			&& inProc.getProceedings().getTitle().toLowerCase().contains(lowerCaseFilter)) {
		    return true; // Filter matches Proceedings
		}
		return false; // Does not match.
	    });
	});
    }

    private void initializeInproceedingsMainColumns() {
	// Initialize InProceedings Columns
	inProceedingMainTableTitleColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
	inProceedingMainTableYearColumn.setCellValueFactory(
		cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getYear())));
	inProceedingMainTableProceedingsColumn.setCellValueFactory(cellData -> {
	    Proceedings proc = cellData.getValue().getProceedings();
	    String procTitle = "";
	    if (proc != null) {
		procTitle = cellData.getValue().getProceedings().getTitle();
	    }
	    return new SimpleStringProperty(procTitle);
	});

	// initialize authors column
	inProceedingAuthorTableNameColumn
		.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

    }

    @FXML
    public void inProceedingsMainClickItem(MouseEvent event) {
	if (event.getClickCount() == 2) // Checking double click
	{
	    InProceedings selected = inProceedingMainTable.getSelectionModel().getSelectedItem();
	    if(selected.getProceedings() != null){
		tabPane.getSelectionModel().select(1);
		proceedingSearchField.setText(selected.getProceedings().getTitle());
	    }
	    

	}

	if (event.getClickCount() > 0) // Checking double click
	{

	    // initialize data
	    InProceedings selected = inProceedingMainTable.getSelectionModel().getSelectedItem();
	    loadInProceedingsAuthors(selected);

	    // editable fields
	    inProceedingTitleField.setText(selected.getTitle());
	    inProceedingPagesField.setText(selected.getPages());
	    inProceedingYearField.setText(String.valueOf(selected.getYear()));

	}
    }

    private void loadInProceedingsAuthors(InProceedings selected) {
	List<Person> authors = selected.getAuthors();
	ObservableList<Person> masterData = FXCollections.observableArrayList();
	masterData.addAll(authors);

	// load table
	FilteredList<Person> filteredData = setTable(masterData, inProceedingAuthorTable);
    }

    @FXML
    private void updateInProceedings(ActionEvent event) {
	// Button was clicked, do something...
	String srcId = ((Button) event.getSource()).getId();
	InProceedings selected = inProceedingMainTable.getSelectionModel().getSelectedItem();
	switch (srcId) {
	case "inProceedingChangeTitleButton":
	    selected.setTitle(inProceedingTitleField.getText());
	    inProceedingMainTable.refresh();
	    break;
	case "inProceedingChangePagesButton":
	    selected.setPages(inProceedingPagesField.getText());
	    inProceedingMainTable.refresh();
	    break;
	case "inProceedingChangeYearButton":
	    selected.setYear(Integer.parseInt(inProceedingYearField.getText()));
	    inProceedingMainTable.refresh();
	    break;
	case "inProceedingDeleteButton":
	    tableData.get(selectedDB).inProceedingsMasterData.remove(selected);
	    inProceedingMainTable.refresh();
	    break;
	case "inProceedingRemoveAuthorButton":
	    Person p = inProceedingAuthorTable.getSelectionModel().getSelectedItem();
	    selected.getAuthors().remove(p);
	    loadInProceedingsAuthors(selected);
	    break;
	default:
	    break;
	}

    }

    // END InProceedings tab

    private <P extends DomainObject> FilteredList<P> setTable(ObservableList<P> masterData, TableView<P> tableView) {
	// Wrap the ObservableList in a FilteredList. Display all data
	// (unfiltered)
	FilteredList<P> filteredData = new FilteredList<>(masterData, p -> true);

	// Wrap the FilteredList in a SortedList.
	SortedList<P> sortedData = new SortedList<>(filteredData);

	// Bind the SortedList comparator to the TableView comparator
	sortedData.comparatorProperty().bind(tableView.comparatorProperty());

	// Add sorted (and filtered) data to the table.
	tableView.setItems(sortedData);
	return filteredData;
    }

    // import and db selection
    @FXML
    private void onImport(ActionEvent event) {
	switch (this.selectedDB) {
	case BaseX:
	    // do nothing
	    break;
	case MongoDB:
	    // TODO
	    break;
	case ZooDB:
	    final Controller parameter = this;
	    new Thread(new Runnable() {
		Controller c = parameter;

		public void run() {
		    c.importButton.setDisable(true);
		    // db.create();
		    ZooImport importer = new ZooImport(db, c);
		    importer.ImportFromXML("src/main/resources/dblp_filtered.xml");
		    c.importButton.setDisable(false);
		}
	    }).start();
	    break;
	default:
	    this.selectedDB = DatabaseManager.DBType.BaseX;
	    break;
	}

    }

    @FXML
    private void onDbSelect(ActionEvent event) {
	String selection = dbSelector.getSelectionModel().getSelectedItem().toString();
	switch (selection) {
	case "BaseX":
	    this.selectedDB = DatabaseManager.DBType.BaseX;
	    break;
	case "MongoDB":
	    this.selectedDB = DatabaseManager.DBType.MongoDB;
	    break;
	case "ZooDB":
	    this.selectedDB = DatabaseManager.DBType.ZooDB;
	    break;
	default:
	    this.selectedDB = DatabaseManager.DBType.BaseX;
	    break;
	}

	db = dbm.getDB(selectedDB);
	System.out.println(selection);
    }

    // START section for fields that reference FXML
    @FXML
    TabPane tabPane;

    @FXML
    ComboBox dbSelector;

    @FXML
    Button importButton;

    // Start Series
    @FXML
    Tab seriesTab;
    @FXML
    private TableView<Series> seriesMainTable;
    @FXML
    private TableColumn<Series, String> seriesNameColumn;
    @FXML
    private TableColumn<Series, String> seriesProcsColumn;
    @FXML
    private TableColumn<Publication, String> seriesPubsTitleColumn;
    @FXML
    private TableColumn<Publication, String> seriesPubsConfColumn;
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
    private TextField seriesCurrentPageField;
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
    private TableView<Publication> seriesProceedingTable;
    @FXML
    private Button seriesRemoveProceedingButton;
    // End SEries

    // Start confeds
    @FXML
    Tab conferenceEditionTab;
    @FXML
    private TableView<ConferenceEdition> conferenceEditionMainTable;
    @FXML
    private TableColumn<ConferenceEdition, String> confEdNameColumn;
    @FXML
    private TableColumn<ConferenceEdition, String> confEdEditionColumn;
    @FXML
    private TableColumn<ConferenceEdition, String> confEdProceedingColumn;
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
    private TextField conferenceEditionCurrentPageField;
    @FXML
    private TextField conferenceEditionNameField;
    @FXML
    private Button conferenceEditionChangeNameButton;
    @FXML
    private TextField conferenceEditionEditionField;
    @FXML
    private Button conferenceEditionChangeEditionButton;
    // End Confeds

    // START Conference
    @FXML
    Tab conferenceTab;
    @FXML
    private TableView<Conference> conferenceMainTable;
    @FXML
    private TableColumn<Conference, String> confMainTableEditionsColumn;
    @FXML
    private TableColumn<Conference, String> confMainTableNameColumn;
    @FXML
    private TableColumn<ConferenceEdition, String> confEditionsYearColumn;
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
    private TextField conferenceCurrentPageField;
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
    private TableView<ConferenceEdition> conferenceEditionTable;
    @FXML
    private Button conferenceRemoveEditionButton;
    // END Conference

    // Start Publications
    @FXML
    Tab publicationTab;
    @FXML
    private TableColumn<Publication, String> publicationMainTableTitleColumn;
    @FXML
    private TableColumn<Publication, String> publicationMainTableAuthorsColumn;
    @FXML
    private TableView<Publication> publicationMainTable;
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
    private TextField publicationCurrentPageField;
    // End Publications

    // Start Proceedings
    @FXML
    Tab proceedingTab;
    @FXML
    private TableColumn<Proceedings, String> proceedingsMainTableTitleColumn;
    @FXML
    private TableColumn<Proceedings, String> proceedingsMainTablePublisherColumn;
    @FXML
    private TableColumn<Proceedings, String> proceedingsMainTableConferenceColumn;
    @FXML
    private TableColumn<Person, String> proceedingEditorTableNameColumn;
    @FXML
    private TableView<Proceedings> proceedingMainTable;
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
    private TextField proceedingCurrentPageField;
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
    private TextField proceedingEditionFilterField;
    @FXML
    private ChoiceBox<?> proceedingEditionDropdown;
    @FXML
    private Button proceedingChangeEditionButton;
    @FXML
    private TextField proceedingEditorFilterField;
    @FXML
    private TableView<Person> proceedingEditorTable;
    @FXML
    private Button proceedingRemoveEditorButton;
    @FXML
    private ChoiceBox<?> proceedingEditorDropdown;
    @FXML
    private Button proceedingAddEditorButton;
    // End Proceedings

    // Start People
    @FXML
    Tab personTab;
    @FXML
    private TableView<Person> personMainTable;
    @FXML
    private TableColumn<Person, String> personMainTableNameColumn;
    @FXML
    private TextField personSearchField;
    @FXML
    private TextField personNameField;
    @FXML
    private TableView<Publication> personProceedingTable;
    @FXML
    private TableColumn<Proceedings, String> peopleProceedingsTableTitleColumn;
    @FXML
    private TableColumn<Proceedings, String> peopleProceedingsTableConferenceColumn;
    @FXML
    private TableView<Publication> personInProceedingTable;
    @FXML
    private TableColumn<InProceedings, String> peopleInProceedingsTableTitleColumn;
    @FXML
    private TableColumn<InProceedings, String> peopleInProceedingsTableProceedingsColumn;

    // End People

    // Start Publishers
    @FXML
    Tab publisherTab;
    @FXML
    private TableView<Publisher> publisherMainTable;
    @FXML
    private TableColumn<Publisher, String> publisherMainTableNameColumn;
    @FXML
    private TableColumn<Publisher, String> publisherMainTablePublishedColumn;
    @FXML
    private TableColumn<Proceedings, String> publisherProceedingsTitleColumn;
    @FXML
    private TableColumn<Proceedings, String> publisherProceedingsConferenceColumn;
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
    private TextField publisherCurrentPageField;
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
    private TableView<Publication> publisherProceedingTable;
    @FXML
    private Button publisherRemoveProceedingButton;
    // END Publishers

    // Start Inproceedings
    @FXML
    Tab inProceedingTab;
    @FXML
    private TableView<InProceedings> inProceedingMainTable;
    @FXML
    private TableColumn<InProceedings, String> inProceedingMainTableTitleColumn;
    @FXML
    private TableColumn<InProceedings, String> inProceedingMainTableYearColumn;
    @FXML
    private TableColumn<InProceedings, String> inProceedingMainTableProceedingsColumn;

    @FXML
    private TableView<Person> inProceedingAuthorTable;
    @FXML
    private TableColumn<Person, String> inProceedingAuthorTableNameColumn;

    @FXML
    private TextField inProceedingSearchField;
    @FXML
    private Button inProceedingSearchButton;// TODO remove this
    @FXML
    private Button inProceedingDeleteButton;
    @FXML
    private Button inProceedingCreateButton;
    @FXML
    private TextField inProceedingPagesField;
    @FXML
    private Button inProceedingChangePagesButton;
    @FXML
    private TextField inProceedingProceedingFilterField;
    @FXML
    private ChoiceBox<?> inProceedingProceedingDropdown;
    @FXML
    private Button inProceedingChangeProceedingButton;
    @FXML
    private ChoiceBox<?> inProceedingAuthorDropdown;
    @FXML
    private Button inProceedingAddAuthorButton;
    @FXML
    private TextField inProceedingAuthorFilterField;
    // @FXML private TableView<SecondaryPersonTableEntry>
    // inProceedingAuthorTable;
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

    // END section for fields that reference FXML
}