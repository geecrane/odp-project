package ch.ethz.globis.mtfobu.odb_project.ui;


import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.ConferenceEdition;
import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Person;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
//import ch.ethz.globis.mtfobu.odb_project.Database.QueryHelper;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.PersonTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryInProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryProceedingTableEntry;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PersonTabController extends TabController<Person, PersonTableEntry, SecondaryProceedingTableEntry, SecondaryInProceedingTableEntry> {

	public PersonTabController(Controller c, TableView<PersonTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton, TableView<SecondaryProceedingTableEntry> secondTable,
			Button deleteSecondReferenceButton, TableView<SecondaryInProceedingTableEntry> thirdTable,
			Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
		// TODO Auto-generated constructor stub
	}

	// person tab specific fields
	private TextField nameField;
	private Button changeNameButton;
	private TextField proceedingFilterField;
	private ChoiceBox proceedingDropdown;
	private Button addEditorButton;
	private TextField inProceedingFilterField;
	private ChoiceBox inProceedingDropdown;
	private Button addAuthorButton;
	
	public void initializeTabSpecificItems(TextField nameField, Button changeNameButton,
			TextField proceedingFilterField, ChoiceBox proceedingDropdown, Button addEditorButton,
			TextField inProceedingFilterField, ChoiceBox inProceedingDropdown, Button addAuthorButton){
		this.nameField = nameField;
		this.changeNameButton = changeNameButton;
		this.proceedingFilterField = proceedingFilterField;
		this.proceedingDropdown = proceedingDropdown;
		this.addEditorButton = addEditorButton;
		this.inProceedingFilterField = inProceedingFilterField;
		this.inProceedingDropdown = inProceedingDropdown;
		this.addAuthorButton = addAuthorButton;
	}
	
	// some functions are right here so we pass them in directly
	public void initializeFunctions(Consumer<String> secondShowFunction, Consumer<String> thirdShowFunction) {
//		TODO
		this.mainShowFunction = this::showPerson;
		this.secondShowFunction = secondShowFunction;
		this.thirdShowFunction = thirdShowFunction;
//		this.searchFunction = c.db.personQueryHelper::queryForDomainObject;
	}	
	
	// shows a specific person in the bottom
	public void showPerson(String id) {
		Person person = c.db.getPersonById(id);
		
		nameField.setText(person.getName());
		
		proceedingFilterField.setText("");
		secondTableList.clear();
		for (Publication proc : person.getEditedPublications()) {
			secondTableList.add(c.new SecondaryProceedingTableEntry((Proceedings) proc));
        }
		
		inProceedingFilterField.setText("");
		thirdTableList.clear();
		for (Publication inProc : person.getAuthoredPublications()) {
			thirdTableList.add(c.new SecondaryInProceedingTableEntry((InProceedings) inProc));
        }
		c.tabPane.getSelectionModel().select(c.personTab);
	}

	// resets all fields in the bottom to empty
	@Override
	public void emptyFields() {
		nameField.setText("");
		proceedingFilterField.setText("");
		secondTableList.clear();
		inProceedingFilterField.setText("");
		thirdTableList.clear();
	}

	@Override
	public void updateMainView(Collection<Person> collection) {
		mainTableList.clear();
		for (Person person: collection) {
        	mainTableList.add(c.new PersonTableEntry(person));
        }
	}
	
	@Override
	void deleteRecord(String id) {
//		TODO
//		c.db.removePerson(id);
	}
}
