package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a certain conference. A conference has more conference editions,
 * usually one edition per year.
 */
public class Conference implements DomainObject {


	private String name;
	private String id;
	private Set<ConferenceEdition> conferences = new HashSet<>();
	
	public Conference(String name) {
		this.name = name;
		this.id = name;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;

	}

	public Set<ConferenceEdition> getEditions() {
		return conferences;
	}

	public void setEditions(Set<ConferenceEdition> editions) {
		this.conferences = editions;

	}

	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;

	}
	
	public void removeReferencesFromOthers() {
		for (ConferenceEdition confEd : this.getEditions()) {
			confEd.setConference(null);
		}
	}

}
