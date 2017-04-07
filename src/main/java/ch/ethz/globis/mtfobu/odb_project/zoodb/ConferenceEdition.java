package ch.ethz.globis.mtfobu.odb_project.zoodb;

import org.zoodb.api.impl.ZooPC;
/**
 * Represents one edition of a conference. It contains a reference
 * to the proceedings published after the conference edition.
 */

public class ConferenceEdition extends ZooPC implements DomainObject {
	private String id;
	private Conference conference;
	private int year;
	private Proceedings proceedings;
	
	private ConferenceEdition(){
		//for ZooDB
	}
	
	public ConferenceEdition(String id, Conference conference, int year, Proceedings proceedings) {
		this.id = id;
		this.conference = conference;
		this.year = year;
		this.proceedings = proceedings;
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

	public Conference getConference() {
		zooActivateRead();
		return conference;
	}

	public void setConference(Conference conference) {
		zooActivateWrite();
		this.conference = conference;
	}

	public int getYear() {
		zooActivateRead();
		return year;
	}

	public void setYear(int year) {
		zooActivateWrite();
		this.year = year;
		
	}

	public Proceedings getProceedings() {
		zooActivateRead();
		return proceedings;
	}

	public void setProceedings(Proceedings proceeding) {
		zooActivateWrite();
		this.proceedings = proceeding;
	}
	
	public void removeReferencesFromOthers() {
		this.setConference(null);
		this.setProceedings(null);
	}

}
