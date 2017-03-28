package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.PublisherTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.SecondaryProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PublisherTabController extends TabController<PublisherTableEntry, SecondaryProceedingTableEntry, TableEntry> {

	
	public PublisherTabController(Controller c, TableView mainTable, TextField fieldSearch, Button buttonSearch,
			Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage, Button buttonCreateRecord,
			Button buttonDeleteRecord, TableView secondTbl, Button btnDeleteRefSecond, TableView thirdTbl,
			Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
		// TODO Auto-generated constructor stub
	}
	
	private TextField publisherNameField;
	private Button publisherChangeNameButton;
	private ChoiceBox<?> publisherProceedingDropdown;
	private Button publisherAddProceedingButton;
	private TextField publisherProceedingFilterField;
	

	public void initializeTabSpecificItems(
			TextField publisherNameField,
			 Button publisherChangeNameButton,
			 
			 ChoiceBox<?> publisherProceedingDropdown,
			 Button publisherAddProceedingButton,
			 TextField publisherProceedingFilterField) {
		
		 this.publisherNameField = publisherNameField;
		 this.publisherChangeNameButton = publisherChangeNameButton;
		 
		 this.publisherProceedingDropdown = publisherProceedingDropdown;
		 this.publisherAddProceedingButton = publisherAddProceedingButton;
		 this.publisherProceedingFilterField = publisherProceedingFilterField;
	}
	
	public void initializeFunctions(Consumer<Long> secondShowFunction) {
		this.mainShowFunction = this::showPublisher;
		this.secondShowFunction = secondShowFunction;
	}
	
	
	@Override
	public void loadData() {
		mainTableList.clear();
		c.pm.currentTransaction().begin();

		Query query = c.pm.newQuery(Publisher.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Publisher> publishers = (Collection<Publisher>) query.execute();

		for (Publisher puber: publishers) {
			mainTableList.add(c.new PublisherTableEntry(puber));
		}

		query.closeAll();
		c.pm.currentTransaction().commit();
	}
	
	private void showPublisher(long objectId) {
		c.pm.currentTransaction().begin();
		Publisher puber = (Publisher) c.pm.getObjectById(objectId);
		
		publisherNameField.setText(puber.getName());
		
		publisherProceedingFilterField.setText("");
		secondTableList.clear();
		for (Publication pub : puber.getPublications()) {
			if (pub instanceof Proceedings) {
				Proceedings proc = (Proceedings) pub;
				secondTableList.add(c.new SecondaryProceedingTableEntry(proc));
			}
        }
		
		c.pm.currentTransaction().commit();
		c.tabPane.getSelectionModel().select(c.publisherTab);
	}

	@Override
	public void emptyFields() {
		publisherNameField.setText("");
		publisherProceedingFilterField.setText("");
		secondTableList.clear();
	}

}
