package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.ProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryPersonTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ProceedingTabController extends TabController<ProceedingTableEntry, SecondaryPersonTableEntry, TableEntry> {
	
	public ProceedingTabController(Controller c, TableView<ProceedingTableEntry> mainTable, TextField fieldSearch,
			Button buttonSearch, Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage,
			Button buttonCreateRecord, Button buttonDeleteRecord, TableView<SecondaryPersonTableEntry> secondTbl,
			Button btnDeleteRefSecond, TableView<TableEntry> thirdTbl, Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
	}

	private TextField proceedingTitleField;
	private Button proceedingChangeTitleButton;

	private ChoiceBox<?> proceedingPublisherDropdown;
	private Button proceedingChangePublisherButton;
	private TextField proceedingPublisherFilterField;

	private ChoiceBox<?> proceedingSeriesDropdown;
	private Button proceedingChangeSeriesButton;
	private TextField proceedingSeriesFilterField;

	private TextField proceedingIsbnField;
	private Button proceedingChangeIsbnButton;

	private ChoiceBox<?> proceedingConferenceDropdown;
	private Button proceedingChangeConferenceButton;
	private TextField proceedingConferenceFilterField;

	private TextField proceedingEditionFilterField;
	private ChoiceBox<?> proceedingEditionDropdown;
	private Button proceedingChangeEditionButton;

	private TextField proceedingEditorFilterField;
	private ChoiceBox<?> proceedingEditorDropdown;
	private Button proceedingAddEditorButton;

	
	
	public void initializeTabSpecificItems(
			 TextField proceedingTitleField,
			 Button proceedingChangeTitleButton,

			 ChoiceBox<?> proceedingPublisherDropdown,
			 Button proceedingChangePublisherButton,
			 TextField proceedingPublisherFilterField,

			 ChoiceBox<?> proceedingSeriesDropdown,
			 Button proceedingChangeSeriesButton,
			 TextField proceedingSeriesFilterField,

			 TextField proceedingIsbnField,
			 Button proceedingChangeIsbnButton,

			 ChoiceBox<?> proceedingConferenceDropdown,
			 Button proceedingChangeConferenceButton,
			 TextField proceedingConferenceFilterField,

			 TextField proceedingEditionFilterField,
			 ChoiceBox<?> proceedingEditionDropdown,
			 Button proceedingChangeEditionButton,

			 TextField proceedingEditorFilterField,
			 ChoiceBox<?> proceedingEditorDropdown,
			 Button proceedingAddEditorButton) {
		
		this.proceedingTitleField = proceedingTitleField;
		this.proceedingChangeTitleButton = proceedingChangeTitleButton;

		this.proceedingPublisherDropdown = proceedingPublisherDropdown;
		this.proceedingChangePublisherButton = proceedingChangePublisherButton;
		this.proceedingPublisherFilterField = proceedingPublisherFilterField;

		this.proceedingSeriesDropdown = proceedingSeriesDropdown;
		this.proceedingChangeSeriesButton = proceedingChangeSeriesButton;
		this.proceedingSeriesFilterField = proceedingSeriesFilterField;

		this.proceedingIsbnField = proceedingIsbnField;
		this.proceedingChangeIsbnButton = proceedingChangeIsbnButton;

		this.proceedingConferenceDropdown = proceedingConferenceDropdown;
		this.proceedingChangeConferenceButton = proceedingChangeConferenceButton;
		this.proceedingConferenceFilterField = proceedingConferenceFilterField;

		this.proceedingEditionFilterField = proceedingEditionFilterField;
		this.proceedingEditionDropdown = proceedingEditionDropdown;
		this.proceedingChangeEditionButton = proceedingChangeEditionButton;

		this.proceedingEditorFilterField = proceedingEditorFilterField;
		this.proceedingEditorDropdown = proceedingEditorDropdown;
		this.proceedingAddEditorButton = proceedingAddEditorButton;
		
	}
	
	public void initializeFunctions(Consumer<Long> secondShowFunction) {
		this.mainShowFunction = this::showProceeding;
		this.secondShowFunction = secondShowFunction;
	}

	private void showProceeding(long objectId) {
		c.database.executeOnObjectById(objectId, displayProceeding);
		c.tabPane.getSelectionModel().select(c.proceedingTab);
	}
	
	private final Function<Object, Integer> displayProceeding = (obj) -> {
		Proceedings proc = (Proceedings) obj;
		
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
		secondTableList.clear();
		for (Person person : proc.getAuthors()) {
			secondTableList.add(c.new SecondaryPersonTableEntry(person));
        }
		
		return 0;
	};
	
	@Override
	public void loadData() {
		mainTableList.clear();
		c.pm.currentTransaction().begin();

        Query query = c.pm.newQuery(Proceedings.class);
        query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
        Collection<Proceedings> proceedings = (Collection<Proceedings>) query.execute();

        for (Proceedings proc: proceedings) {
        	mainTableList.add(c.new ProceedingTableEntry(proc));
        }
        
        query.closeAll();
        c.pm.currentTransaction().commit();
	}
	
	@Override
	public void emptyFields() {
		proceedingTitleField.setText("");
		proceedingIsbnField.setText("");
		proceedingPublisherFilterField.setText("");
		proceedingConferenceFilterField.setText("");
		proceedingEditionFilterField.setText("");
		proceedingSeriesFilterField.setText("");
		proceedingEditorFilterField.setText("");
		secondTableList.clear();
	}
	


}
