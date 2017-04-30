package ch.ethz.globis.mtfobu.odb_project.ui2;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.Series;
//import ch.ethz.globis.mtfobu.odb_project.Database.QueryHelper;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SeriesTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SeriesTabController extends TabController<Series, SeriesTableEntry, SecondaryProceedingTableEntry, TableEntry> {

	public SeriesTabController(Controller c, TableView<SeriesTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton, TableView<SecondaryProceedingTableEntry> secondTable,
			Button deleteSecondReferenceButton, TableView<TableEntry> thirdTable, Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
		// TODO Auto-generated constructor stub
	}

	private TextField seriesNameField;
	private Button seriesChangeNameButton;
	private TextField seriesProceedingFilterField;
	private Button seriesAddProceedingButton;
	private ChoiceBox<?> seriesProceedingDropdown;
	
	public void initializeTabSpecificItems(
			TextField seriesNameField,
			Button seriesChangeNameButton,
			TextField seriesProceedingFilterField,
			Button seriesAddProceedingButton,
			ChoiceBox<?> seriesProceedingDropdown)  {
		
		  this.seriesNameField = seriesNameField;
		  this.seriesChangeNameButton = seriesChangeNameButton;
		  this.seriesProceedingFilterField = seriesProceedingFilterField;
		  this.seriesAddProceedingButton = seriesAddProceedingButton;
		  this.seriesProceedingDropdown = seriesProceedingDropdown;

	}
	
	public void initializeFunctions(Consumer<String> secondShowFunction) {
//		this.mainShowFunction = this::showSeries;
//		this.secondShowFunction = secondShowFunction;
//		this.searchFunction = c.db.seriesQueryHelper::queryForDomainObject;
	}
	
	private void showSeries(String id) {
//		Series series = c.db.getSeriesById(id);
//		
//		seriesNameField.setText(series.getName());
//		
//		seriesProceedingFilterField.setText("");
//		secondTableList.clear();
//		for (Publication pub : series.getPublications()) {
//			if (pub instanceof Proceedings) {
//				secondTableList.add(c.new SecondaryProceedingTableEntry((Proceedings)pub));
//			}
//		}
//		
//		c.tabPane.getSelectionModel().select(c.seriesTab);
	}


	@Override
	public void emptyFields() {
		seriesNameField.setText("");
		seriesProceedingFilterField.setText("");
		secondTableList.clear();
	}

	@Override
	public void updateMainView(Collection<Series> collection) {
		mainTableList.clear();
		for (Series series: collection) {
			mainTableList.add(c.new SeriesTableEntry(series));
		}
	}
	
	@Override
	void deleteRecord(String id) {
//		c.db.removeSeries(id);
	}


}
