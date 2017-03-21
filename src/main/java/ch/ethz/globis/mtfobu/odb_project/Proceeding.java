package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;
import java.util.Set;

import org.zoodb.api.impl.ZooPC;

public class Proceeding extends ZooPC implements Proceedings {
	//George: NOTE that most attributes DO NOT always exist. Check first if xml has that attribute.
	
	private void Proceeding() {
		// nothing to do here!
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
		//George: These are actually editors (no authors in proceedings)
		zooActivateRead();
		return editors;
	}

	@Override
	public void setAuthors(List<Person> authors) {
		//George: These are actually editors (no authors in proceedings)
		zooActivateWrite();
		this.editors = authors;
		
	}

	@Override
	public int getYear() {
		//George: Note that the Year defines the Conference Edition
		zooActivateRead();
		return year;
	}

	@Override
	public void setYear(int year) {
		//George: Note that the Year defines the Conference Edition
		zooActivateWrite();
		this.year = year;
		
	}

	@Override
	public String getElectronicEdition() {
		//George: Does NOT always exist!
		zooActivateRead();
		return electronicEdition;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		//George: Does NOT always exist!
		zooActivateWrite();
		this.electronicEdition = electronicEdition;
		
	}

	@Override
	public String getId() {
		//George: May be the key attribute in xml
		zooActivateRead();
		return id;
	}

	@Override
	public void setId(String id) {
		//George: May be the key attribute found in xml
		zooActivateWrite();
		this.id = id;
		
	}

	@Override
	public String getNote() {
		//George: Does NOT always exist!
		zooActivateRead();
		return note;
	}

	@Override
	public void setNote(String note) {
		//George: Does NOT always exist!
		zooActivateWrite();
		this.note = note;
		
	}

	@Override
	public int getNumber() {
		//George: Does NOT always exist!
		zooActivateRead();
		return number;
	}

	@Override
	public void setNumber(int number) {
		//George: Does NOT always exist!
		zooActivateWrite();
		this.number = number;
		
	}

	@Override
	public Publisher getPublisher() {
		zooActivateRead();
		return publisher;
	}

	@Override
	public void setPublisher(Publisher publisher) {
		zooActivateWrite();
		this.publisher = publisher;
		
	}

	@Override
	public String getVolume() {
		//George: Does NOT always exist!
		zooActivateRead();
		return volume;
	}

	@Override
	public void setVolume(String volume) {
		//George: Does NOT always exist!
		zooActivateWrite();
		this.volume = volume;
		
	}

	@Override
	public String getIsbn() {
		zooActivateRead();
		return isbn;
	}

	@Override
	public void setIsbn(String isbn) {
		zooActivateWrite();
		this.isbn = isbn;		
	}

	@Override
	public Series getSeries() {
		//George: Does NOT always exist!
		zooActivateRead();
		return series;
	}

	@Override
	public void setSeries(Series series) {
		//George: Does NOT always exist!
		zooActivateWrite();
		this.series = series;
		
	}

	@Override
	public ConferenceEdition getConferenceEdition() {
		zooActivateRead();
		return confEdition;
	}

	@Override
	public void setConferenceEdition(ConferenceEdition conferenceEdition) {
		zooActivateWrite();
		this.confEdition = conferenceEdition;
		
	}

	@Override
	public Set<InProceedings> getPublications() {
		//George: A Proceeding can have multiple inProceedings
		zooActivateRead();
		return inProceedings;
	}

	@Override
	public void setPublications(Set<InProceedings> publications) {
		//George: A Proceeding can have multiple inProceedings
		zooActivateWrite();
		this.inProceedings = publications;
		
	}
	private List<Person> editors;
	private String title;
	private String booktitle;//George: This is Conference name (use it as foreign key)
	private Series series;
	private int year;
	private String isbn;
	private ConferenceEdition confEdition;
	private String id;
	private Publisher publisher;
	Set<InProceedings> inProceedings;
	private String volume;
	private String note;
	private String electronicEdition;
	private int number;
	

}
