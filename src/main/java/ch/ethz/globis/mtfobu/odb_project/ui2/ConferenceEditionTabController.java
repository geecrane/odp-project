package ch.ethz.globis.mtfobu.odb_project.ui2;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Conference;
import ch.ethz.globis.mtfobu.odb_project.ConferenceEdition;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.ConferenceEditionTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConferenceEditionTabController extends TabController<ConferenceEdition, ConferenceEditionTableEntry, TableEntry, TableEntry> {
	
	public ConferenceEditionTabController(Controller c, TableView<ConferenceEditionTableEntry> mainTable,
			TextField searchField, Button searchButton, Button nextPageButton, Button previousPageButton,
			TextField currentPageField, Button createRecordButton, Button deleteRecordButton,
			TableView<TableEntry> secondTable, Button deleteSecondReferenceButton, TableView<TableEntry> thirdTable,
			Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
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
		//TODO
		//this.mainShowFunction = this::showConferenceEdition;
		//this.searchFunction = c.db.conferenceEditionQueryHelper::queryForDomainObject;
	}
	
	private void showConferenceEdition(String id) {
		//TODO
		//ConferenceEdition confEd = c.db.getConferenceEditionById(id);
		
//		Conference conf = confEd.getConference();
//		
//		if (null != conf) {
//			conferenceEditionNameField.setText(conf.getName());
//		} else {
//			conferenceEditionNameField.setText("");
//		}
//		
//		int year = confEd.getYear();
//		if (0 != year) {
//			conferenceEditionEditionField.setText(Integer.toString(year));
//		} else {
//			conferenceEditionEditionField.setText("No year");
//		}
//		
//		c.tabPane.getSelectionModel().select(c.conferenceEditionTab);
	}

	@Override
	public void emptyFields() {
		conferenceEditionNameField.setText("");
		conferenceEditionEditionField.setText("");
	}

	@Override
	public void updateMainView(Collection<ConferenceEdition> collection) {
		mainTableList.clear();
		
		for (ConferenceEdition confEd: collection) {
        	mainTableList.add(c.new ConferenceEditionTableEntry(confEd));
        }
	}

	@Override
	void deleteRecord(String id) {
		//c.db.removeConferenceEdition(id);
	}

}
