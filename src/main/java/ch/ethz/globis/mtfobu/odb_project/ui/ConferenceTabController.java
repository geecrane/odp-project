package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Conference;
import ch.ethz.globis.mtfobu.odb_project.ConferenceEdition;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.ConferenceTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryConferenceEditionTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConferenceTabController extends TabController<Conference, ConferenceTableEntry, SecondaryConferenceEditionTableEntry, TableEntry> {
	
	public ConferenceTabController(Controller c, TableView<ConferenceTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton,
			TableView<SecondaryConferenceEditionTableEntry> secondTable, Button deleteSecondReferenceButton,
			TableView<TableEntry> thirdTable, Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
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
			c.pm.currentTransaction().begin();

	        Query query = c.pm.newQuery(Conference.class);
	        query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
	        Collection<Conference> conferences = (Collection<Conference>) query.execute();

	        updateMainView(conferences);
	        
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

	@Override
	public void updateMainView(Collection<Conference> collection) {
		mainTableList.clear();
		
		for (Conference conf: collection) {
        	mainTableList.add(c.new ConferenceTableEntry(conf));
        }
		
	}

}
