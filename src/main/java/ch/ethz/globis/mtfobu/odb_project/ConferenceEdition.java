package ch.ethz.globis.mtfobu.odb_project;

/**
 * Represents one edition of a conference. It contains a reference
 * to the proceedings published after the conference edition.
 */

public class ConferenceEdition implements DomainObject {
	private String id;
	private Conference conference;
	private int year;
	private Proceedings proceedings;
	
	
	public ConferenceEdition(Conference conference, int year, Proceedings proceedings) {
		String idhash = Integer.toString(year)+proceedings.getId()+conference.getId();
		this.id = String.valueOf(idhash.hashCode());
		this.conference = conference;
		this.year = year;
		this.proceedings = proceedings;
	}
	
	
	//used as a backward compatibility to for the XmlImport
	public ConferenceEdition(String id2, Conference conference2, int year2, Proceedings proceedings2) {
		this.id = id2;
		this.conference = conference;
		this.year = year;
		this.proceedings = proceedings;
	}



	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public Conference getConference() {
		return conference;
	}

	public void setConference(Conference conference) {
		this.conference = conference;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
		
	}

	public Proceedings getProceedings() {
		return proceedings;
	}

	public void setProceedings(Proceedings proceeding) {
		this.proceedings = proceeding;
	}
	
	public void removeReferencesFromOthers() {
		this.setConference(null);
		this.setProceedings(null);
	}

}
