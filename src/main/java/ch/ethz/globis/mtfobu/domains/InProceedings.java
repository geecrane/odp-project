package ch.ethz.globis.mtfobu.domains;

import java.util.List;
import java.util.Vector;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.zoodb.api.impl.ZooPC;

import annotations.ProceedingExists;
//Implemented constrains: 6,7,9
//Successfully tested constrains: 6,7,9
/**
 * A type of article that was published as part of a conference proceedings.
 */

public class InProceedings extends ZooPC implements Publication {

	private String title;
	private List<Person> authors = new Vector<>();
	private int year;
	private String electronicEdition;
	private String id;
	// constraint 9
		@NotNull(message = "\"Note\" may be the empty string but not null")
		@Pattern(regexp="(Draft|Submitted|Accepted|Published|)", message = "note is must be an element of the set {Draft, Submitted, Accepted, Published}")
	private String note;
	//constraint 6
		@Pattern(regexp="\\d+-\\d+", message = "Page format is invalid")
	private String pages;
	// constraint 7
		@NotNull(message="Proceeding field of InProceeding can't be null")
		@ProceedingExists
	private Proceedings proceedings;
	
	//George: See comments I wrote in class Proceeding. Similar comments apply here
	
	private InProceedings() {
		//for ZooDB
	}
	public InProceedings(String id){
		this.id = id;
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
	public boolean removeAuthor(Person author) {
		zooActivateWrite();
		return this.authors.remove(author);
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


	public void setProceedings(Proceedings proceedings) {
		zooActivateWrite();
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
