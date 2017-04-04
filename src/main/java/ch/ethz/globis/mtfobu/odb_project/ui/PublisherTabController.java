package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.Publisher;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.PublisherTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.SecondaryProceedingTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PublisherTabController extends TabController<Publisher, PublisherTableEntry, SecondaryProceedingTableEntry, TableEntry> {
	
	public PublisherTabController(Controller c, TableView<PublisherTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton, TableView<SecondaryProceedingTableEntry> secondTable,
			Button deleteSecondReferenceButton, TableView<TableEntry> thirdTable, Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
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
		
		c.pm.currentTransaction().begin();

		Query query = c.pm.newQuery(Publisher.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Publisher> publishers = (Collection<Publisher>) query.execute();

		updateMainView(publishers);

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

	@Override
	public void updateMainView(Collection<Publisher> collection) {
		mainTableList.clear();
		for (Publisher puber: collection) {
			mainTableList.add(c.new PublisherTableEntry(puber));
		}
	}

}
