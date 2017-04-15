package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;


/**
 *  Represents a publisher. Besides the name it also contains references to all
 *  of the publications published by the publisher.
 */
public class Publisher implements DomainObject {
	private String name;
	private String id;
	private Set<Publication> publications = new HashSet<>();
	
	public Publisher(String name) {
		this.name = name;
		this.id = name;
	}
	
	public void addPublication(Publication pub){
    	this.publications.add(pub);
    }

    public String getName(){
    	return this.name;
    }

    public void setName(String name){
    	this.name = name;
    }

    public Set<Publication> getPublications(){
    	return this.publications;
    }
    
    public boolean removePublications(Publication publication){
    	return publications.remove(publication);
    }

    public void setPublications(Set<Publication> publications){
    	this.publications = publications;
    }

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
		
	}
	
	public void removeReferencesFromOthers() {
		for (Publication pub : this.getPublications()) {
			Proceedings proc = (Proceedings) pub;
			proc.setPublisher(null);
		}
	}
    
}