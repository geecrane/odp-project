package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;

import java.util.List;

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.odb_project.db.Database;
import ch.ethz.globis.mtfobu.odb_project.db.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AddProceedingController {
	private DatabaseManager.DBType selectedDB;
	private Database db;
	private final DatabaseManager dbm = new DatabaseManager();

	@FXML
	public void cancelAddProceeding(ActionEvent event) {
		System.out.println("OK");
		final Node source = (Node) event.getSource();
		final Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void addEditor(ActionEvent event) {
		editorList.add(new Person(newEditorNameField.getText()));
		editors.setItems(editorList);
	}

	@FXML
	public void removeEditor(ActionEvent event) {
		Person per = editors.getSelectionModel().getSelectedItem();
		if (per != null) {
			System.out.println("Deleted person " + per.getName());
			editorList.remove(per);
			editors.refresh();
		}
	}

	@FXML
	public void addProceeding(ActionEvent event) {
		if (idField.getText().isEmpty()) {
			errorMessageArea.appendText("Please specify an ID\n");
			return;
		}
		Proceedings proc = new Proceedings(idField.getText());
		proc.setTitle(title.getText());
		proc.setConference(new Conference(booktitle.getText()));
		proc.setPublisher(new Publisher(publisher.getText()));
		try {
			proc.setYear(Integer.parseInt(year.getText()));
		} catch (NumberFormatException ex) {
			errorMessageArea.appendText("Enrty for year is not valid\n");
		}
		proc.setIsbn(isbn.getText());

		List<Person> authors = new ArrayList<>();
		authors.addAll(editorList);
		proc.setAuthors(authors);

		List<String> errors = db.addProceedingValidated(proc);
		if (errors.isEmpty()) {
			// close window
			final Node source = (Node) event.getSource();
			final Stage stage = (Stage) source.getScene().getWindow();
			stage.close();
			Controller.mainController.tableData.get(selectedDB).proceedingsMasterData.add(proc);

		} else {
			for (String errorMessage : errors) {
				errorMessageArea.appendText(errorMessage + "\n");
			}
		}

	}

	@FXML
	public void initialize() {
		System.out.print("Initializing...");
		editorNameColumn.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
		editorList = FXCollections.observableArrayList();

		selectedDB = DatabaseManager.DBType.ZooDB;
		db = dbm.getDB(selectedDB);
		System.out.println("OK");

	}

	@FXML
	private SplitPane splitPane;
	@FXML
	private TextField idField;
	@FXML
	private TextField title;
	@FXML
	private TextField booktitle;
	@FXML
	private TextField publisher;
	@FXML
	private TextField year;
	@FXML
	private TextField isbn;
	@FXML
	private TableView<Person> editors;
	@FXML
	private TableColumn<Person, String> editorNameColumn;
	@FXML
	private ObservableList<Person> editorList;
	@FXML
	private Button addEditor;
	@FXML
	private Button deleteEditor;
	@FXML
	private TextField newEditorNameField;
	@FXML
	private TextArea errorMessageArea;
	@FXML
	private Button CancelButton;
	@FXML
	private Button AddButton;

}
