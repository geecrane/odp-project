package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;

import org.zoodb.api.impl.ZooPC;

/**
 * A type of article that was published as part of a conference proceedings.
 */

public class InProceedings extends ZooPC implements Publication {

	private String title;
	private List<Person> authors;
	private int year;
	private String electronicEdition;
	private String id;
	private String note;
	private String pages;
	private Proceedings proceedings;
	
	//George: See comments I wrote in class Proceeding. Same comments apply here
	public InProceedings() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getTitle() {
		zooActivateRead();
		return title;
	}

	@Override
	public void setTitle(String title) {
		zooActivateWrite();
		this.title = title;

	}

	@Override
	public List<Person> getAuthors() {
		zooActivateRead();
		return authors;
	}

	@Override
	public void setAuthors(List<Person> authors) {
		zooActivateWrite();
		this.authors = authors;

	}

	@Override
	public int getYear() {
		zooActivateRead();
		return year;
	}

	@Override
	public void setYear(int year) {
		zooActivateWrite();
		this.year = year;

	}

	@Override
	public String getElectronicEdition() {
		zooActivateRead();
		return electronicEdition;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		zooActivateWrite();
		this.electronicEdition = electronicEdition;

	}

	@Override
	public String getId() {
		zooActivateRead();
		return id;
	}

	@Override
	public void setId(String id) {
		zooActivateWrite();
		this.id = id;

	}

	public String getNote() {
		zooActivateRead();
		return note;
	}

	public void setNote(String note) {
		zooActivateWrite();
		this.note = note;

	}

	public String getPages() {
		zooActivateRead();
		return pages;
	}

	public void setPages(String pages) {
		zooActivateWrite();
		this.pages = pages;

	}

	public Proceedings getProceedings() {
		zooActivateRead();
		return proceedings;
	}


	public void setProceedings(Proceedings proceeding) {
		zooActivateWrite();
		this.proceedings = proceeding;

	}

}
