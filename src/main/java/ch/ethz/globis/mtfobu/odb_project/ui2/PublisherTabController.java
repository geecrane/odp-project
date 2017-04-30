package ch.ethz.globis.mtfobu.odb_project.ui2;

import java.util.Collection;
import java.util.function.Consumer;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.Publisher;
//import ch.ethz.globis.mtfobu.odb_project.Database.QueryHelper;
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
	
	public void initializeFunctions(Consumer<String> secondShowFunction) {
//		TODO
//		this.mainShowFunction = this::showPublisher;
//		this.secondShowFunction = secondShowFunction;
//		this.searchFunction = c.db.publisherQueryHelper::queryForDomainObject;
	}
	
	
	private void showPublisher(String id) {
//		TODO
//		Publisher puber = c.db.getPublisherById(id);
//		
//		publisherNameField.setText(puber.getName());
//		
//		publisherProceedingFilterField.setText("");
//		secondTableList.clear();
//		for (Publication pub : puber.getPublications()) {
//			if (pub instanceof Proceedings) {
//				Proceedings proc = (Proceedings) pub;
//				secondTableList.add(c.new SecondaryProceedingTableEntry(proc));
//			}
//        }
//		
//		c.tabPane.getSelectionModel().select(c.publisherTab);
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

	@Override
	void deleteRecord(String id) {
//		c.db.removePublisher(id);
	}

	
}
