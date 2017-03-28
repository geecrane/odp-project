package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.ConferenceEditionTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConferenceEditionTabController extends TabController<ConferenceEditionTableEntry, TableEntry, TableEntry> {

	public ConferenceEditionTabController(Controller c, TableView mainTable, TextField fieldSearch, Button buttonSearch,
			Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage, Button buttonCreateRecord,
			Button buttonDeleteRecord, TableView secondTbl, Button btnDeleteRefSecond, TableView thirdTbl,
			Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
		// TODO Auto-generated constructor stub
	}
	
	private TextField conferenceEditionNameField;
	private Button conferenceEditionChangeNameButton;
	private TextField conferenceEditionEditionField;
	private Button conferenceEditionChangeEditionButton;
	
	
	public void initializeTabSpecificItems(
			TextField conferenceEditionNameField,
			Button conferenceEditionChangeNameButton,
			TextField conferenceEditionEditionField,
			Button conferenceEditionChangeEditionButton) {
		this.conferenceEditionNameField = conferenceEditionNameField;
		this.conferenceEditionChangeNameButton = conferenceEditionChangeNameButton;
		this.conferenceEditionEditionField = conferenceEditionEditionField;
		this.conferenceEditionChangeEditionButton = conferenceEditionChangeNameButton;
	}
	
	public void initializeFunctions() {
		this.mainShowFunction = this::showConferenceEdition;
	}
	
	private void showConferenceEdition(Long objectId) {
		c.pm.currentTransaction().begin();
		ConferenceEdition confEd = (ConferenceEdition) c.pm.getObjectById(objectId);
		
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
		
		c.pm.currentTransaction().commit();
		c.tabPane.getSelectionModel().select(c.conferenceEditionTab);
	}

	@Override
	public void loadData() {
			mainTableList.clear();
			c.pm.currentTransaction().begin();

	        Query query = c.pm.newQuery(ConferenceEdition.class);
	        query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
	        Collection<ConferenceEdition> conferenceEditions = (Collection<ConferenceEdition>) query.execute();

	        for (ConferenceEdition confEd: conferenceEditions) {
	        	mainTableList.add(c.new ConferenceEditionTableEntry(confEd));
	        }
	        
	        query.closeAll();
	        c.pm.currentTransaction().commit();
	}

	@Override
	public void emptyFields() {
		conferenceEditionNameField.setText("");
		conferenceEditionEditionField.setText("");
	}

}
