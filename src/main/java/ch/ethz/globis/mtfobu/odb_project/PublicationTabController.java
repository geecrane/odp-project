package ch.ethz.globis.mtfobu.odb_project;

import java.util.Collection;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.Controller.PublicationTableEntry;
import ch.ethz.globis.mtfobu.odb_project.Controller.TableEntry;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class PublicationTabController extends TabController<PublicationTableEntry, TableEntry, TableEntry> {

	public PublicationTabController(Controller c, TableView mainTable, TextField fieldSearch, Button buttonSearch,
			Button buttonNextPage, Button buttonPreviousPage, TextField fieldCurrentPage, Button buttonCreateRecord,
			Button buttonDeleteRecord, TableView secondTbl, Button btnDeleteRefSecond, TableView thirdTbl,
			Button btnDeleteRefthird) {
		super(c, mainTable, fieldSearch, buttonSearch, buttonNextPage, buttonPreviousPage, fieldCurrentPage, buttonCreateRecord,
				buttonDeleteRecord, secondTbl, btnDeleteRefSecond, thirdTbl, btnDeleteRefthird);
		// TODO Auto-generated constructor stub
	}
	
	
	
	
	
	public void initializeTabSpecificItems() {
		// None on this tab
	}
	
	public void initializeFunctions() {
		// None needed from outside
		this.mainShowFunction = this::showPublication;
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
	public void loadData() {
		mainTableList.clear();
		c.pm.currentTransaction().begin();

		Query query = c.pm.newQuery(Proceedings.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Publication> publications = (Collection<Publication>) query.execute();

		for (Publication proc: publications) {
			mainTableList.add(c.new PublicationTableEntry(proc));
		}

		query.closeAll();

		query = c.pm.newQuery(InProceedings.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		publications = (Collection<Publication>) query.execute();

		for (Publication inProc: publications) {
			mainTableList.add(c.new PublicationTableEntry(inProc));
		}

		query.closeAll();
		c.pm.currentTransaction().commit();
	}

	@Override
	public void emptyFields() {
		// No Fields to empty
	}

}
