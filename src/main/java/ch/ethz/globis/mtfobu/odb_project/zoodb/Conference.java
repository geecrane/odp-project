package ch.ethz.globis.mtfobu.odb_project.zoodb;

import java.util.HashSet;
import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 * Represents a certain conference. A conference has more conference editions,
 * usually one edition per year.
 */
public class Conference extends ZooPC implements DomainObject {


	private String name;
	private String id;
	private Set<ConferenceEdition> conferences = new HashSet<>();
	
	private Conference() {
		// For ZooDB
	}
	public Conference(String name) {
		this.name = name;
		this.id = name;
	}
	
	public String getName() {
		zooActivateRead();
		return this.name;
	}

	public void setName(String name) {
		zooActivateWrite();
		this.name = name;

	}

	public Set<ConferenceEdition> getEditions() {
		zooActivateRead();
		return conferences;
	}

	public void setEditions(Set<ConferenceEdition> editions) {
		zooActivateWrite();
		this.conferences = editions;

	}

	public String getId() {
		zooActivateRead();
		return id;
	}

	@Override
	public void setId(String id) {
		zooActivateWrite();
		this.id = id;

	}
	
	public void removeReferencesFromOthers() {
		for (ConferenceEdition confEd : this.getEditions()) {
			confEd.setConference(null);
		}
	}

}
