package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


/**
 * A specialized type of publications, represents the proceedings released at
 * a certain conference edition. The proceedings contains all the articles published
 * at that conference edition.
 */
public class Proceedings implements Publication {
	//George: NOTE that most attributes DO NOT always exist. Check first if xml has that attribute.
	
	public Proceedings(String id){
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
		//George: These are actually editors (no authors in proceedings)
		return editors;
	}
	
	@Override
	public boolean removeAuthor(Person author) {
		//George: These are actually editors (no authors in proceedings)
		return this.editors.remove(author);
	}

	@Override
	public void setAuthors(List<Person> authors) {
		//George: These are actually editors (no authors in proceedings)
		this.editors = authors;
		
	}

	@Override
	public int getYear() {
		//George: Note that the Year defines the Conference Edition
		return year;
	}

	@Override
	public void setYear(int year) {
		//George: Note that the Year defines the Conference Edition
		this.year = year;
		
	}

	@Override
	public String getElectronicEdition() {
		//George: Does NOT always exist!
		return electronicEdition;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		//George: Does NOT always exist!
		this.electronicEdition = electronicEdition;
		
	}

	@Override
	public String getId() {
		//George: May be the key attribute in xml
		return id;
	}

	@Override
	public void setId(String id) {
		//George: May be the key attribute found in xml
		this.id = id;
		
	}

	public String getNote() {
		//George: Does NOT always exist!
		return note;
	}

	public void setNote(String note) {
		//George: Does NOT always exist!
		this.note = note;
		
	}

	public int getNumber() {
		//George: Does NOT always exist!
		return number;
	}

	public void setNumber(int number) {
		//George: Does NOT always exist!
		this.number = number;
		
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
		
	}

	public String getVolume() {
		//George: Does NOT always exist!
		return volume;
	}

	public void setVolume(String volume) {
		//George: Does NOT always exist!
		this.volume = volume;
		
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;		
	}

	public Series getSeries() {
		//George: Does NOT always exist!
		return series;
	}

	public void setSeries(Series series) {
		//George: Does NOT always exist!
		this.series = series;
		
	}

	public ConferenceEdition getConferenceEdition() {
		return confEdition;
	}

	public void setConferenceEdition(ConferenceEdition conferenceEdition) {
		this.confEdition = conferenceEdition;
		
	}

	public Set<InProceedings> getPublications() {
		//George: A Proceeding can have multiple inProceedings
		return inProceedings;
	}
	
	public boolean removePublications(InProceedings inProc) {
		return inProceedings.remove(inProc);
	}

	public void setPublications(Set<InProceedings> publications) {
		//George: A Proceeding can have multiple inProceedings
		this.inProceedings = publications;
		
	}
	
	public void removeReferencesFromOthers() {
		for (Person auth : this.getAuthors()) {
			auth.removeAuthoredPublication(this);
			auth.removeEditedPublication(this);
		}
		
		ConferenceEdition confEd = this.getConferenceEdition();
		if (null != confEd) {
			confEd.setConference(null);
		}
		
		for (InProceedings inProc : this.getPublications()) {
			inProc.setProceedings(null);
		}
		
		Publisher puber = this.getPublisher();
		if (null != puber) {
			puber.removePublications(this);
		}
		
		Series ser = this.getSeries();
		if (null != ser) {
			ser.removePublications(this);
		}
	}
	
	
	private List<Person> editors = new Vector<>();
	private String title;
	private Series series;
	private int year;
	private String isbn;
	private ConferenceEdition confEdition;
	private String id;
	private Publisher publisher;
	private Set<InProceedings> inProceedings = new HashSet<>();
	private String volume;
	private String note;
	private String electronicEdition;
	private int number;
	

}
