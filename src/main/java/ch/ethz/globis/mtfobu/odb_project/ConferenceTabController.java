package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.ConferenceTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryConferenceEditionTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConferenceTabController extends TabController<ConferenceTableEntry, SecondaryConferenceEditionTableEntry, TableEntry> {

	public ConferenceTabController(Controller c, TableView<ConferenceTableEntry> mainTable, TextField fieldSearch,
			Button buttonSearch, Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage,
			Button buttonCreateRecord, Button buttonDeleteRecord,
			TableView<SecondaryConferenceEditionTableEntry> secondTbl, Button btnDeleteRefSecond,
			TableView<TableEntry> thirdTbl, Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
		// TODO Auto-generated constructor stub
	}
	
	private TextField conferenceNameField;
	private Button conferenceChangeNameButton;
	private ChoiceBox<?> conferenceEditionDropdown;
	private Button conferenceAddEditionButton;
	private TextField conferenceEditionFilterField;
        
	public void initializeTabSpecificItems(
			TextField conferenceNameField,
			 Button conferenceChangeNameButton,
			 ChoiceBox<?> conferenceEditionDropdown,
			 Button conferenceAddEditionButton,
			 TextField conferenceEditionFilterField) {
		  this.conferenceNameField = conferenceNameField;
		  this.conferenceChangeNameButton = conferenceChangeNameButton;
		  this.conferenceEditionDropdown = conferenceEditionDropdown;
		  this.conferenceAddEditionButton = conferenceAddEditionButton;
		  this.conferenceEditionFilterField = conferenceEditionFilterField;
	}
	
	public void initializeFunctions(Consumer<Long> secondShowFunction) {
		mainShowFunction = this::showConference;
		this.secondShowFunction = secondShowFunction;
	}

	@Override
	public void loadData() {
			mainTableList.clear();
			c.pm.currentTransaction().begin();

	        Query query = c.pm.newQuery(Conference.class);
	        query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
	        Collection<Conference> conferences = (Collection<Conference>) query.execute();

	        for (Conference conf: conferences) {
	        	mainTableList.add(c.new ConferenceTableEntry(conf));
	        }
	        
	        query.closeAll();
	        c.pm.currentTransaction().commit();
	}

	private void showConference(long objectId) {
		c.pm.currentTransaction().begin();
		Conference conf = (Conference) c.pm.getObjectById(objectId);
		
		conferenceNameField.setText(conf.getName());
		
		conferenceEditionFilterField.setText("");
		secondTableList.clear();
		for (ConferenceEdition confEd : conf.getEditions()) {
			secondTableList.add(c.new SecondaryConferenceEditionTableEntry(confEd));
		}
		
		c.pm.currentTransaction().commit();
		c.tabPane.getSelectionModel().select(c.conferenceTab);
	}
	
	@Override
	public void emptyFields() {
		conferenceNameField.setText("");
		conferenceEditionFilterField.setText("");
		secondTableList.clear();

	}

}
