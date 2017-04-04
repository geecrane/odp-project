package ch.ethz.globis.mtfobu.odb_project.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import javax.jdo.Query;

import ch.ethz.globis.mtfobu.odb_project.InProceedings;
import ch.ethz.globis.mtfobu.odb_project.Proceedings;
import ch.ethz.globis.mtfobu.odb_project.Publication;
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
		
		searchButton.setOnAction((event) -> {
			String search[] = searchField.getText().split(";");
			if(search.length == 3){
			Function<ArrayList<Publication>, Void> fun = (pubs)-> {
				int start = (Integer.parseInt(search[1]) < pubs.size())? Integer.parseInt(search[1]) : 0;
				int stop = (Integer.parseInt(search[2]) < pubs.size()) ? Integer.parseInt(search[2]) : ((pubs.size() == 0 ) ? 0 : pubs.size()-1);
				mainTableList.clear();
				for (Publication pub: pubs.subList(start, stop)){
					mainTableList.add(c.new PublicationTableEntry(pub));
				}
				return null;
				};
				
			c.db.executeOnPublicationsByTitle(search[0],fun);
			}
			else loadData();
			
			
		});
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
		Collection<Publication> publications = new ArrayList<Publication>();
		
		c.pm.currentTransaction().begin();

		Query query = c.pm.newQuery(Proceedings.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Publication> publications1 = (Collection<Publication>) query.execute();

		publications.addAll(publications1);
		
		query.closeAll();

		query = c.pm.newQuery(InProceedings.class);
		query.setRange((queryPage[0]-1)*c.PAGE_SIZE, queryPage[0]*c.PAGE_SIZE);
		Collection<Publication> publications2 = ((Collection<Publication>) query.execute());

		publications.addAll(publications2);
		
		updateMainView(publications);

		query.closeAll();
		c.pm.currentTransaction().commit();
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

}
