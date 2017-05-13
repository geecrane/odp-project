package ch.ethz.globis.mtfobu.domains;

import java.util.HashSet;
import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 *  Represents a series of publications. Besides the name it also contains references to all
 *  of the publications published by in the same series.
 */
public class Series extends ZooPC implements DomainObject {
	private String name;
	private String id;
	private Set<Publication> publications = new HashSet<>();
	
	private Series() {
		// TODO Auto-generated constructor stub
	}
	
	public Series(String name) {
		this.name = name;
		this.id = String.valueOf(name.hashCode());
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
    	return publications;
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
		for (Publication pub : this.getPublications()){
			Proceedings proc = (Proceedings) pub;
			proc.setSeries(null);
		}
	}
    
}