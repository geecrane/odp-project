package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;

/**
 *  Represents a publisher. Besides the name it also contains references to all
 *  of the publications published by the publisher.
 */
public class Publisher implements DomainObject {

    public String getName(){
    	return this.name;
    }

    public void setName(String name){
    	this.name = name;
    }

    public Set<Publication> getPublications(){
    	return new HashSet<Publication>(this.publications);
    }

    public void setPublications(Set<Publication> publications){
    	this.publications = publications;
    }
    public void addPublication(Publication pub){
    	this.publications.add(pub);
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
	private Set<Publication> publications;
    
}