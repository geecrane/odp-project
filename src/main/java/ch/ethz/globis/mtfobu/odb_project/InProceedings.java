package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;
import java.util.Vector;


/**
 * A type of article that was published as part of a conference proceedings.
 */

public class InProceedings implements Publication {

	private String title;
	private List<Person> authors = new Vector<>();
	private int year;
	private String electronicEdition;
	private String id;
	private String note;
	private String pages;
	private Proceedings proceedings;
	
	//George: See comments I wrote in class Proceeding. Similar comments apply here
	

	public InProceedings(String id){
		this.id = id;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;

	}

	@Override
	public List<Person> getAuthors() {
		return authors;
	}

	@Override
	public boolean removeAuthor(Person author) {
		return this.authors.remove(author);
	}
	
	@Override
	public void setAuthors(List<Person> authors) {
		this.authors = authors;

	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public void setYear(int year) {
		this.year = year;

	}

	@Override
	public String getElectronicEdition() {
		return electronicEdition;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		this.electronicEdition = electronicEdition;

	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;

	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;

	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;

	}

	public Proceedings getProceedings() {
		return proceedings;
	}


	public void setProceedings(Proceedings proceedings) {
		this.proceedings = proceedings;
	}
	
	public void removeReferencesFromOthers() {
		for (Person auth : this.getAuthors()){
			auth.removeAuthoredPublication(this);
		}
		Proceedings proc = this.getProceedings();
		if (null != proc) {
			proc.removePublications(this);
		}
	}

}
