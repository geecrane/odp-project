package ch.ethz.globis.mtfobu.odb_project;


import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.MyRowFactory;
import ch.ethz.globis.mtfobu.odb_project.Controller.PersonTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryInProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryProceedingTableEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class PersonTabController extends TabController<PersonTableEntry, SecondaryProceedingTableEntry, SecondaryInProceedingTableEntry> {
	
	public PersonTabController(Controller c, TableView<PersonTableEntry> mainTable, TextField fieldSearch,
			Button buttonSearch, Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage,
			Button buttonCreateRecord, Button buttonDeleteRecord, TableView<SecondaryProceedingTableEntry> secondTbl,
			Button btnDeleteRefSecond, TableView<SecondaryInProceedingTableEntry> thirdTbl, Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
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
	public void initializeFunctions(Consumer<Long> secondShowFunction, Consumer<Long> thirdShowFunction) {
		this.mainShowFunction = this::showPerson;
		this.secondShowFunction = secondShowFunction;
		this.thirdShowFunction = thirdShowFunction;
	}

	// loads PAGE_SIZE number of entries in the big table
	@Override
	public void loadData() {
		mainTableList.clear();
		c.pm.currentTransaction().begin();

        Query query = c.pm.newQuery(Person.class);
        query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
        Collection<Person> people = (Collection<Person>) query.execute();

        for (Person person: people) {
        	mainTableList.add(c.new PersonTableEntry(person));
        }
        
        query.closeAll();
        c.pm.currentTransaction().commit();
	}
	
	// shows a specific person in the bottom
	public void showPerson(Long objectId) {
		c.pm.currentTransaction().begin();
		Person person = (Person) c.pm.getObjectById(objectId);
		
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
		
		c.pm.currentTransaction().commit();
		
		c.tabPane.getSelectionModel().select(c.personTab);
	}

	// resets all fields in the bottom to empty
	private void emptyFields() {
		nameField.setText("");
		proceedingFilterField.setText("");
		secondTableList.clear();
		inProceedingFilterField.setText("");
		thirdTableList.clear();
	}
	
	// deletes a person, while first removing the references to it
	@Override
	public void deleteRecord(Long objectId) {
		c.pm.currentTransaction().begin();
		Person person = (Person) c.pm.getObjectById(objectId);
		person.removeReferencesFromOthers();
		c.pm.deletePersistent(person);
		c.pm.currentTransaction().commit();
		loadData();
		emptyFields();
	}
}
