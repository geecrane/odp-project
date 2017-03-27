package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import org.zoodb.api.impl.ZooPC;

import ch.ethz.globis.mtfobu.odb_project.Controller.DeleteHandler;
import ch.ethz.globis.mtfobu.odb_project.Controller.MyRowFactory;
import ch.ethz.globis.mtfobu.odb_project.Controller.PagingHandler;
import ch.ethz.globis.mtfobu.odb_project.Controller.PersonTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public abstract class TabController<TE1 extends TableEntry, TE2 extends TableEntry, TE3 extends TableEntry> {
	public Controller c;
	
	public TableView<TE1> mainTable;
	public ObservableList<TE1> mainTableList;
	
	public int[] queryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	public long detailDisplayedId = 0L;
	
	public Button buttonNextPage;
	public Button buttonPreviousPage;
	public TextField fieldCurrentPage;
	
	public Button buttonCreateRecord;
	public Button buttonDeleteRecord;
	
	
	public TableView<TE2> secondTable;
	
	public ObservableList<TE2> secondTableList;
	public Button buttonDeleteRefSecond;
	
	public TableView<TE3> thirdTable;
	
	public ObservableList<TE3> thirdTableList;
	public Button buttonDeleteRefThird;
	
	public int numberOfTables = 0;
	
	public TabController(Controller c, TableView<TE1> mainTable,
			Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage,
			Button buttonCreateRecord, Button buttonDeleteRecord,
			TableView<TE2> secondTbl, Button btnDeleteRefSecond,
			TableView<TE3> thirdTbl, Button btnDeleteRefthird) {
		this.c = c;
		
		this.mainTable = mainTable;
		this.mainTableList = FXCollections.observableArrayList();
		this.numberOfTables = 1;
		
		this.buttonNextPage = buttonNextPage;
		this.buttonPreviousPage = buttonPreviousPage;
		this.fieldCurrentPage = fieldCurrentPage;

		this.buttonCreateRecord = buttonCreateRecord;
		this.buttonDeleteRecord = buttonDeleteRecord;
		
		if (null != secondTbl) {
			secondTable = secondTbl;
			buttonDeleteRefSecond = btnDeleteRefSecond;
			secondTableList = FXCollections.observableArrayList();
			numberOfTables = 2;
		}
		
		if (null != thirdTbl) {
			thirdTable = thirdTbl;
			buttonDeleteRefThird = btnDeleteRefthird;
			thirdTableList = FXCollections.observableArrayList();
			numberOfTables = 3;
		}

	}
	
	public Consumer<Long> mainShowFunction;
	public Consumer<Long> deleteRecordFunction;
	public Consumer<Long> secondShowFunction;
	public Consumer<Long> thirdShowFunction;
	public Runnable mainDataLoader;
	
	public void initializeFunctions(Consumer<Long> mainShowFunction, Consumer<Long> deleteRecordFunction,
			Consumer<Long> secondShowFunction, Consumer<Long> thirdShowFunction, Runnable mainDataLoader) {
		this.mainShowFunction = mainShowFunction;
		this.secondShowFunction = secondShowFunction;
		this.thirdShowFunction = thirdShowFunction;
		this.deleteRecordFunction = deleteRecordFunction;
		this.mainDataLoader = mainDataLoader;
	}
	
	public void setUpTables() {
		ObservableList<TableColumn<TE1, ?>> columns = mainTable.getColumns();
		
		
		// Main Table
		for (int i = 0; i < columns.size(); i++) {
			TableColumn<TE1,String> column = (TableColumn<TE1,String>) columns.get(i);
			final int fin = i;
			column.setCellValueFactory(new Callback<CellDataFeatures<TE1, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(CellDataFeatures<TE1, String> p) {
					return new ReadOnlyObjectWrapper<String>(p.getValue().getColumnContent(fin));
				}
			});
			
		}
		
		mainTable.setRowFactory(c.new MyRowFactory<TE1>(mainShowFunction));
		mainTable.setItems(mainTableList);
		
		buttonDeleteRecord.setOnAction(c.new DeleteHandler<TE1>(mainTable, deleteRecordFunction));
		
		buttonNextPage.setOnAction(c.new PagingHandler(queryPage, fieldCurrentPage, 1, mainDataLoader));
		buttonPreviousPage.setOnAction(c.new PagingHandler(queryPage, fieldCurrentPage, -1, mainDataLoader));
		fieldCurrentPage.setOnAction(c.new PagingHandler(queryPage, fieldCurrentPage, 0, mainDataLoader));
		
		
		if (numberOfTables > 1) {
			
			// Table 2
			ObservableList<TableColumn<TE2, ?>> columns2 = secondTable.getColumns();
			
			for (int i = 0; i < columns2.size(); i++) {
				TableColumn<TE2,String> column = (TableColumn<TE2,String>) columns2.get(i);
				final int fin = i;
				column.setCellValueFactory(new Callback<CellDataFeatures<TE2, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<TE2, String> p) {
						return new ReadOnlyObjectWrapper<String>(p.getValue().getColumnContent(fin));
					}
				});
				
			}
			secondTable.setRowFactory(c.new MyRowFactory<TE2>(secondShowFunction));
			secondTable.setItems(secondTableList);
			
		}
		
		if (numberOfTables > 2) {
			
			// Table 2
			ObservableList<TableColumn<TE3, ?>> columns3 = thirdTable.getColumns();
			
			for (int i = 0; i < columns3.size(); i++) {
				TableColumn<TE3,String> column = (TableColumn<TE3,String>) columns3.get(i);
				final int fin = i;
				column.setCellValueFactory(new Callback<CellDataFeatures<TE3, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<TE3, String> p) {
						return new ReadOnlyObjectWrapper<String>(p.getValue().getColumnContent(fin));
					}
				});
				
			}
			thirdTable.setRowFactory(c.new MyRowFactory<TE3>(thirdShowFunction));
			thirdTable.setItems(thirdTableList);
			
		}
		
	}
	
	abstract public void loadData();
	
	
	
}
