package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;
import java.util.Set;

public class Proceeding extends Proceedings {

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Person> getAuthors() {
		return this.editors;
	}

	@Override
	public void setAuthors(List<Person> authors) {
		this.editors = authors;
		
	}

	@Override
	public int getYear() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setYear(int year) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getElectronicEdition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getNote() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNote(String note) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumber(int number) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Publisher getPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPublisher(Publisher publisher) {

		
	}

	@Override
	public String getVolume() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVolume(String volume) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getIsbn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsbn(String isbn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Series getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSeries(Series series) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ConferenceEdition getConferenceEdition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConferenceEdition(ConferenceEdition conferenceEdition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<InProceedings> getPublications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPublications(Set<InProceedings> publications) {
		// TODO Auto-generated method stub
		
	}
	private List<Person> editors;
	private String title;
	private String booktitle;
	private Series series;
	private int year;
	private String isbn;
	private String url;
	private ConferenceEdition confEdit;
	

}
