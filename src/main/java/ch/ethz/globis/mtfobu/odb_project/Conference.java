package ch.ethz.globis.mtfobu.odb_project;

import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 * Represents a certain conference. A conference has more conference editions,
 * usually one edition per year.
 */
public class Conference extends ZooPC implements DomainObject {


	private String name;
	private String id;
	private Set<ConferenceEdition> conferences;
	
	public Conference() {
		// TODO Auto-generated constructor stub
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

}
