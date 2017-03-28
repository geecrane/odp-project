package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SeriesTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SeriesTabController extends TabController<SeriesTableEntry, SecondaryProceedingTableEntry, TableEntry> {

	public SeriesTabController(Controller c, TableView mainTable, TextField fieldSearch, Button buttonSearch,
			Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage, Button buttonCreateRecord,
			Button buttonDeleteRecord, TableView secondTbl, Button btnDeleteRefSecond, TableView thirdTbl,
			Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
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
	
	public void initializeFunctions(Consumer<Long> secondShowFunction) {
		this.mainShowFunction = this::showSeries;
		this.secondShowFunction = secondShowFunction;
	}
	
	private void showSeries(Long objectId) {
		c.pm.currentTransaction().begin();
		Series series = (Series) c.pm.getObjectById(objectId);
		
		seriesNameField.setText(series.getName());
		
		seriesProceedingFilterField.setText("");
		secondTableList.clear();
		for (Publication pub : series.getPublications()) {
			if (pub instanceof Proceedings) {
				secondTableList.add(c.new SecondaryProceedingTableEntry((Proceedings)pub));
			}
		}
		
		c.pm.currentTransaction().commit();
		c.tabPane.getSelectionModel().select(c.seriesTab);
	}
	
	@Override
	public void loadData() {
		mainTableList.clear();
		c.pm.currentTransaction().begin();

		Query query = c.pm.newQuery(Series.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Series> seriesPlural = (Collection<Series>) query.execute();

		for (Series series: seriesPlural) {
			mainTableList.add(c.new SeriesTableEntry(series));
		}

		query.closeAll();
		c.pm.currentTransaction().commit();
	}

	@Override
	public void emptyFields() {
		seriesNameField.setText("");
		seriesProceedingFilterField.setText("");
		secondTableList.clear();
	}

}
