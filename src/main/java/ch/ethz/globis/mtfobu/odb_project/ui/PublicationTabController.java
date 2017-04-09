package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
import ch.ethz.globis.mtfobu.odb_project.Database.QueryHelper;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.PublicationTableEntry;
import ch.ethz.globis.mtfobu.odb_project.ui.Controller.TableEntry;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PublicationTabController extends TabController<Publication, PublicationTableEntry, TableEntry, TableEntry> {
	
	
	public PublicationTabController(Controller c, TableView<PublicationTableEntry> mainTable, TextField searchField,
			Button searchButton, Button nextPageButton, Button previousPageButton, TextField currentPageField,
			Button createRecordButton, Button deleteRecordButton, TableView<TableEntry> secondTable,
			Button deleteSecondReferenceButton, TableView<TableEntry> thirdTable, Button deleteThirdReferenceButton) {
		super(c, mainTable, searchField, searchButton, nextPageButton, previousPageButton, currentPageField, createRecordButton,
				deleteRecordButton, secondTable, deleteSecondReferenceButton, thirdTable, deleteThirdReferenceButton);
		// TODO Auto-generated constructor stub
		
	}

	
	
	
	public void initializeTabSpecificItems() {
		// None on this tab
	}
	
	public void initializeFunctions() {
		// None needed from outside
		this.mainShowFunction = this::showPublication;
		this.searchFunction = c.db::queryForPublication;
	}
	
	private void showPublication(Long objectId) {
		boolean isProceeding = false;
		c.pm.currentTransaction().begin();
		Publication pub = (Publication) c.pm.getObjectById(objectId);
		isProceeding = pub instanceof Proceedings;
		c.pm.currentTransaction().commit();
		
		if (isProceeding) {
			c.proceedingTabController.mainShowFunction.accept(objectId);
		} else {
			c.inProceedingTabController.mainShowFunction.accept(objectId);
		}
	}


	@Override
	public void emptyFields() {
		// No Fields to empty
	}

	@Override
	public void updateMainView(Collection<Publication> collection) {
		mainTableList.clear();
		for (Publication proc: collection) {
			mainTableList.add(c.new PublicationTableEntry(proc));
		}
	}

	@Override
	void deleteRecord(String id) {
		c.db.removeProceedings(id);
		c.db.removeInProceedings(id);
	}
}
