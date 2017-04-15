package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;


/**
 *  Represents a series of publications. Besides the name it also contains references to all
 *  of the publications published by in the same series.
 */
public class Series implements DomainObject {
	private String name;
	private String id;
	private Set<Publication> publications = new HashSet<>();
	
	
	public Series(String name) {
		this.name = name;
		this.id = String.valueOf(name.hashCode());
	}
	
    public String getName(){
    	return this.name;
    }

    public void setName(String name){
    	this.name = name;
    }

    public Set<Publication> getPublications(){
    	return publications;
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
		for (Publication pub : this.getPublications()){
			Proceedings proc = (Proceedings) pub;
			proc.setSeries(null);
		}
	}
    
}