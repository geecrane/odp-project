package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.Collection;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import ch.ethz.globis.mtfobu.odb_project.DomainObject;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import ch.ethz.globis.mtfobu.odb_project.QueryParameters;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

public abstract class TabController<DO extends DomainObject, TE1 extends TableEntry, TE2 extends TableEntry, TE3 extends TableEntry> {
	public Controller c;
	
	public TableView<TE1> mainTable;
	public ObservableList<TE1> mainTableList;
	
	public TextField searchField;
	public Button searchButton;
	
	public int[] queryPage = new int[] {1}; // Looks dumb but I need this to be able to pass a reference
	public long detailDisplayedId = 0L;
	
	public Button nextPageButton;
	public Button previousPageButton;
	public TextField currentPageField;
	
	public Button createRecordButton;
	public Button deleteRecordButton;
	
	
	public TableView<TE2> secondTable;
	
	public ObservableList<TE2> secondTableList;
	public Button deleteSecondReferenceButton;
	
	public TableView<TE3> thirdTable;
	
	public ObservableList<TE3> thirdTableList;
	public Button deleteThirdReferenceButton;
	
	public int numberOfTables = 0;
	
	public TabController(Controller c, TableView<TE1> mainTable,
			TextField searchField, Button searchButton,
			Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton,
			TableView<TE2> secondTable, Button deleteSecondReferenceButton,
			TableView<TE3> thirdTable, Button deleteThirdReferenceButton) {
		this.c = c;
		
		this.mainTable = mainTable;
		this.mainTableList = FXCollections.observableArrayList();
		this.numberOfTables = 1;
		
		this.searchField = searchField;
		this.searchButton = searchButton;
		
		this.nextPageButton = nextPageButton;
		this.previousPageButton = previousPageButton;
		this.currentPageField = currentPageField;

		this.createRecordButton = createRecordButton;
		this.deleteRecordButton = deleteRecordButton;
		
		if (null != secondTable) {
			this.secondTable = secondTable;
			this.deleteSecondReferenceButton = deleteSecondReferenceButton;
			this.secondTableList = FXCollections.observableArrayList();
			this.numberOfTables = 2;
		}
		
		if (null != thirdTable) {
			this.thirdTable = thirdTable;
			this.deleteThirdReferenceButton = deleteThirdReferenceButton;
			this.thirdTableList = FXCollections.observableArrayList();
			this.numberOfTables = 3;
		}

	}
	
	public Consumer<String> mainShowFunction;
	public Consumer<String> secondShowFunction;
	public Consumer<String> thirdShowFunction;
	public Function<QueryParameters, Collection<DO>> searchFunction;
	
	public void initializeFunctions(Consumer<String> mainShowFunction,
			Consumer<String> secondShowFunction, Consumer<String> thirdShowFunction,
			Function<QueryParameters, Collection<DO>> searchFunction) {
		this.mainShowFunction = mainShowFunction;
		this.secondShowFunction = secondShowFunction;
		this.thirdShowFunction = thirdShowFunction;
		this.searchFunction = searchFunction;
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
		
		mainTable.setRowFactory(new MyRowFactory<TE1>(mainShowFunction));
		mainTable.setItems(mainTableList);
		
		deleteRecordButton.setOnAction(c.new DeleteHandler<TE1>(mainTable, this::deleteRecord));

		
		nextPageButton.setOnAction((event) -> {handlePaging(1);});
		previousPageButton.setOnAction((event) -> {handlePaging(-1);});
		currentPageField.setOnAction((event) -> {handlePaging(0);});
		
		searchButton.setOnAction((event) -> {loadFromSearch();});
		searchField.setOnAction((event) -> {loadFromSearch();});
		
		
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
			secondTable.setRowFactory(new MyRowFactory<TE2>(secondShowFunction));
			secondTable.setItems(secondTableList);
			
		}
		
		if (numberOfTables > 2) {
			
			// Table 3
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
			thirdTable.setRowFactory(new MyRowFactory<TE3>(thirdShowFunction));
			thirdTable.setItems(thirdTableList);
			
		}
		
	}
	
	// Generally you need initializeTabSpecificItems(...) and initializeFunctions(...) in subclasses
	
	abstract void deleteRecord(String id);
	
//	public void deleteRecord(Long objectId) {
//		c.db.removeObjectById(objectId);
//		
//		reload();
//		emptyFields();
//	};
	
	abstract public void emptyFields();
	
	abstract public void updateMainView(Collection<DO> collection);
	
	public void reload() {
		if (lastLoadWasFromSearch) {
			loadFromSearch();
		} else {
			loadFromPaging();
		}
	}
	
	private boolean viewIsDirty;
	
	public void setDirty() {
		viewIsDirty = true;
	}
	
	private boolean lastLoadWasFromSearch = true;
	
	public void loadFromSearch() {
		viewIsDirty = false;
		lastLoadWasFromSearch = true;
		
		QueryParameters params = readAndParseSearchField();
		
		if (params.rangeEnd.isPresent() && params.rangeStart.isPresent() && params.rangeEnd.getAsLong() < params.rangeStart.getAsLong()) {
			params.rangeEnd = OptionalLong.empty();
		}
		
		if (params.rangeEnd.isPresent() || params.rangeStart.isPresent()) {
			params.isRanged = true;
		} else {
			readCurrentPageNumberFromUI();
			params.pageNumber = queryPage[0];
			params.isRanged = false;
		}
		
		if (params.searchTerm.equals("")) {
			params.isSearch = false;
		} else {
			params.isSearch = true;
		}
		updateMainView(searchFunction.apply(params));
	}
	
	public void loadFromPaging() {
		viewIsDirty = false;
		lastLoadWasFromSearch = false;
		
		QueryParameters params = readAndParseSearchField();
		params.isRanged = false;
		params.pageNumber = queryPage[0];
		if (params.searchTerm.equals("")) {
			params.isSearch = false;
		} else {
			params.isSearch = true;
		}
		updateMainView(searchFunction.apply(params));
	}
	
	private QueryParameters readAndParseSearchField() {
		String searchParams[] = searchField.getText().split(";");
		
		// Do nothing if the number of parameters is under 1, or there are more than 3,
		// unless it's 4 and the last is empty so that "search;1;20;" is also allowed.
		if( searchParams.length < 1 || (searchParams.length > 3 && !(searchParams.length == 4 && searchParams[3].equals("")) ) ) return null;
		
		QueryParameters params = new QueryParameters();
		
		params.rangeEnd = OptionalLong.empty();
		params.rangeStart = OptionalLong.empty();
		params.searchTerm = "";
		
		// The breaks are left out intentionally, I want this to fall through
		switch (searchParams.length) {
			case 3 :
				try {
					long stop = Long.parseLong(searchParams[2]);
					stop = (stop < 1) ? 1 : stop;
					params.rangeEnd = OptionalLong.of(stop);
				} catch (NumberFormatException e) {
					params.rangeEnd = OptionalLong.empty();
				}
				/* FALLTHROUGH */
			case 2 : 
				try {
					long start = Long.parseLong(searchParams[1]);
					start = (start < 0) ? 0 : start;
					params.rangeStart = OptionalLong.of(start);
				} catch (NumberFormatException e) {
					params.rangeStart = OptionalLong.empty();
				}
				/* FALLTHROUGH */
			default :
				params.searchTerm = searchParams[0];
		}
		
		return params;
	}
	
	private void readCurrentPageNumberFromUI() {
		try {
			int t = Integer.parseInt(currentPageField.getText());
			if (t >= 1) {
				queryPage[0] = t;
			}
		} catch (NumberFormatException e) {
			// Do nothing
		}
	}
	
	public void handlePaging(int direction) {
		try {
			int t = Integer.parseInt(currentPageField.getText()) + direction;
			if (t >= 1) {
				if (0 != direction) {
					currentPageField.setText(Integer.toString(t));
				}
				queryPage[0] = t;
				loadFromPaging();
			}
		} catch (NumberFormatException e) {
			// Do nothing
		}
	}
	
    
	// A row factory that takes a show function and generates rows that react to doubleclicks by running that function on their table entry
    protected class MyRowFactory<T extends TableEntry> implements Callback<TableView<T>, TableRow<T>> {
    	Consumer<String> show;
    	
    	public MyRowFactory(Consumer<String> s) {
    		show = s;
    	}
    	
		@Override
		public TableRow<T> call(TableView<T> tv) {
			TableRow<T> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (! row.isEmpty() && event.getButton()==MouseButton.PRIMARY && event.getClickCount() == 2) {
		        	T item = row.getItem();
		            show.accept(item.id);
		        }
		    });
		    return row ;
		}
		
	}
	
}
