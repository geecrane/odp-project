package ch.ethz.globis.mtfobu.domains;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.zoodb.api.impl.ZooPC;

import annotations.IDValid;

//Implemented constrains: 2,3

/**
 * A specialized type of publications, represents the proceedings released at a
 * certain conference edition. The proceedings contains all the articles
 * published at that conference edition.
 */
public class Proceedings extends ZooPC implements Publication {
	// George: NOTE that most attributes DO NOT always exist. Check first if xml
	// has that attribute.

	public Proceedings() {
		// for ZooDB
	}

	public Proceedings(String id) {
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
		// George: These are actually editors (no authors in proceedings)
		zooActivateRead();
		return editors;
	}

	@Override
	public boolean removeAuthor(Person author) {
		// George: These are actually editors (no authors in proceedings)
		zooActivateWrite();
		return this.editors.remove(author);
	}

	@Override
	public void setAuthors(List<Person> authors) {
		// George: These are actually editors (no authors in proceedings)
		zooActivateWrite();
		this.editors = authors;

	}

	@Override
	public int getYear() {
		// George: Note that the Year defines the Conference Edition
		zooActivateRead();
		return year;
	}

	@Override
	public void setYear(int year) {
		// George: Note that the Year defines the Conference Edition
		zooActivateWrite();
		this.year = year;

	}

	@Override
	public String getElectronicEdition() {
		// George: Does NOT always exist!
		zooActivateRead();
		return electronicEdition;
	}

	@Override
	public void setElectronicEdition(String electronicEdition) {
		// George: Does NOT always exist!
		zooActivateWrite();
		this.electronicEdition = electronicEdition;

	}

	@Override
	public String getId() {
		// George: May be the key attribute in xml
		zooActivateRead();
		return id;
	}

	@Override
	public void setId(String id) {
		// George: May be the key attribute found in xml
		zooActivateWrite();
		this.id = id;

	}

	public String getNote() {
		// George: Does NOT always exist!
		zooActivateRead();
		return note;
	}

	public void setNote(String note) {
		// George: Does NOT always exist!
		zooActivateWrite();
		this.note = note;

	}

	public int getNumber() {
		// George: Does NOT always exist!
		zooActivateRead();
		return number;
	}

	public void setNumber(int number) {
		// George: Does NOT always exist!
		zooActivateWrite();
		this.number = number;

	}

	public Publisher getPublisher() {
		zooActivateRead();
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		zooActivateWrite();
		this.publisher = publisher;

	}

	public String getVolume() {
		// George: Does NOT always exist!
		zooActivateRead();
		return volume;
	}

	public void setVolume(String volume) {
		// George: Does NOT always exist!
		zooActivateWrite();
		this.volume = volume;

	}

	public String getIsbn() {
		zooActivateRead();
		return isbn;
	}

	public void setIsbn(String isbn) {
		zooActivateWrite();
		this.isbn = isbn;
	}

	public Series getSeries() {
		// George: Does NOT always exist!
		zooActivateRead();
		return series;
	}

	public void setSeries(Series series) {
		// George: Does NOT always exist!
		zooActivateWrite();
		this.series = series;

	}

	public Conference getConference() {
		return this.conf;
	}

	public void setConference(Conference conf) {
		this.conf = conf;
	}

	public ConferenceEdition getConferenceEdition() {
		zooActivateRead();
		return confEdition;
	}

	public void setConferenceEdition(ConferenceEdition conferenceEdition) {
		zooActivateWrite();
		this.confEdition = conferenceEdition;

	}

	public Set<InProceedings> getPublications() {
		// George: A Proceeding can have multiple inProceedings
		zooActivateRead();
		return inProceedings;
	}

	public boolean removePublications(InProceedings inProc) {
		zooActivateWrite();
		return inProceedings.remove(inProc);
	}

	public void setPublications(Set<InProceedings> publications) {
		// George: A Proceeding can have multiple inProceedings
		zooActivateWrite();
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
	@NotNull(message = "Proceeding without an id!")
	//@IDValid(message = "(Proceeding) id: ${id} is invalid because of redundancy")
	private String id;
	private String title;
	private int year;
	private List<Person> editors = new Vector<>();
	private Series series;
	private String isbn;
	private Conference conf;
	private ConferenceEdition confEdition;
	private Publisher publisher;
	private Set<InProceedings> inProceedings = new HashSet<>();
	private String volume;
	private String note;
	private String electronicEdition;
	private int number;

}
