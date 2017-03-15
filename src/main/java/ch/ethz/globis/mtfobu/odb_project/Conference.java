package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a certain conference. A conference has more conference editions,
 * usually one edition per year.
 */
public class Conference implements DomainObject {

    public String getName(){
    	return this.name;
    }

    public void setName(String name){
    	this.name = name;
    }

    public Set<ConferenceEdition> getEditions(){
    	return new HashSet<ConferenceEdition>(this.conferences);
    }

    public void setEditions(Set<ConferenceEdition> editions){
    	this.conferences = editions;
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
	private String name;
	private Set<ConferenceEdition> conferences;
    
}