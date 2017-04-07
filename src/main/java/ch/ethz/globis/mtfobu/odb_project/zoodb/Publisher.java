package ch.ethz.globis.mtfobu.odb_project.zoodb;

import java.util.HashSet;
import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 *  Represents a publisher. Besides the name it also contains references to all
 *  of the publications published by the publisher.
 */
public class Publisher extends ZooPC implements DomainObject {
	private String name;
	private String id;
	private Set<Publication> publications = new HashSet<>();
	
	private Publisher() {
		// For ZooDB
	}
	public Publisher(String name) {
		this.name = name;
		this.id = name;
	}
	
	public void addPublication(Publication pub){
    	this.publications.add(pub);
    }

    public String getName(){
    	zooActivateRead();
    	return this.name;
    }

    public void setName(String name){
    	zooActivateWrite();
    	this.name = name;
    }

    public Set<Publication> getPublications(){
    	zooActivateRead();
    	return this.publications;
    }
    
    public boolean removePublications(Publication publication){
    	zooActivateWrite();
    	return publications.remove(publication);
    }

    public void setPublications(Set<Publication> publications){
    	zooActivateWrite();
    	this.publications = publications;
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
	
	public void removeReferencesFromOthers() {
		for (Publication pub : this.getPublications()) {
			Proceedings proc = (Proceedings) pub;
			proc.setPublisher(null);
		}
	}
    
}