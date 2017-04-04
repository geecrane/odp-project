package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;

import ch.ethz.globis.mtfobu.odb_project.Database.QueryHelper;
import ch.ethz.globis.mtfobu.odb_project.DomainObject;
import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Person;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.InProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.PublicationTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryPersonTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class InProceedingTabController extends TabController<InProceedings, InProceedingTableEntry, SecondaryPersonTableEntry, TableEntry> {
	
	public InProceedingTabController(Controller c, TableView<InProceedingTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton, TableView<SecondaryPersonTableEntry> secondTable,
			Button deleteSecondReferenceButton, TableView<TableEntry> thirdTable, Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
		// TODO Auto-generated constructor stub
		
	}
	private TextField inProceedingPagesField;
	private Button inProceedingChangePagesButton;
	private TextField inProceedingProceedingFilterField;
	private ChoiceBox<?> inProceedingProceedingDropdown;
	private Button inProceedingChangeProceedingButton;
	private ChoiceBox<?> inProceedingAuthorDropdown;
	private Button inProceedingAddAuthorButton;
	private TextField inProceedingAuthorFilterField;
	private TextField inProceedingTitleField;
	private Button inProceedingChangeTitleButton;
	private TextField inProceedingYearField;
	private Button inProceedingChangeYearButton;
	
	public void initializeTabSpecificItems(
			 TextField inProceedingPagesField,
			 Button inProceedingChangePagesButton,
			 
			 TextField inProceedingProceedingFilterField,
			 ChoiceBox<?> inProceedingProceedingDropdown,
			 Button inProceedingChangeProceedingButton,
			 
			 ChoiceBox<?> inProceedingAuthorDropdown,
			 Button inProceedingAddAuthorButton,
			 TextField inProceedingAuthorFilterField,
			 
			 TextField inProceedingTitleField,
			 Button inProceedingChangeTitleButton,
			 
			 TextField inProceedingYearField,
			 Button inProceedingChangeYearButton) {
		
		 this.inProceedingPagesField = inProceedingPagesField;
		 this.inProceedingChangePagesButton = inProceedingChangePagesButton;
		 
		 this.inProceedingProceedingFilterField = inProceedingProceedingFilterField;
		 this.inProceedingProceedingDropdown = inProceedingProceedingDropdown;
		 this.inProceedingChangeProceedingButton = inProceedingChangeProceedingButton;
		 
		 this.inProceedingAuthorDropdown = inProceedingAuthorDropdown;
		 this.inProceedingAddAuthorButton = inProceedingAddAuthorButton;
		 this.inProceedingAuthorFilterField = inProceedingAuthorFilterField;
		 
		 this.inProceedingTitleField = inProceedingTitleField;
		 this.inProceedingChangeTitleButton = inProceedingChangeTitleButton;
		 
		 this.inProceedingYearField = inProceedingYearField;
		 this.inProceedingChangeYearButton = inProceedingChangeYearButton;
		
	}

	public void initializeFunctions(Consumer<Long> secondShowFunction) {
		this.mainShowFunction = this::showInProceeding;
		this.secondShowFunction = secondShowFunction;
		this.searchFunction = c.db.new QueryHelper<InProceedings>(InProceedings.class, "title")::queryForDomainObject;
	}
	
	
	private void showInProceeding(Long objectId) {

		c.db.executeOnObjectById(objectId, show_in_proceeding);
		c.tabPane.getSelectionModel().select(c.inProceedingTab);
	}
	private final Function<Object,Integer> show_in_proceeding = ( obj) -> {
		InProceedings inProc = (InProceedings) obj;
		this.inProceedingTitleField.setText(inProc.getTitle());
		this.inProceedingPagesField.setText(inProc.getPages());
		this.inProceedingYearField.setText(Integer.toString(inProc.getYear()));
		
		Proceedings proc = inProc.getProceedings();
		if(null != proc) {
			this.inProceedingProceedingFilterField.setText(proc.getTitle());
		}
		
		this.inProceedingAuthorFilterField.setText("");
		secondTableList.clear();
		for (Person person : inProc.getAuthors()) {
			secondTableList.add(c.new SecondaryPersonTableEntry(person));
        }
		return 0;
    };

	@Override
	public void updateMainView(Collection<InProceedings> inProceedings) {
		mainTableList.clear();
		for (InProceedings inProc : inProceedings) {
			mainTableList.add(c.new InProceedingTableEntry(inProc));
		}
	}

	
	@Override
	public void emptyFields() {
		inProceedingTitleField.setText("");
		inProceedingPagesField.setText("");
		inProceedingYearField.setText("");
		inProceedingProceedingFilterField.setText("");
		inProceedingAuthorFilterField.setText("");
		secondTableList.clear();
	}

}
