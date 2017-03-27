package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 * Represents a person. The person can have the role of an author
 * or of an editor for some publications.
 */
public class Person extends ZooPC implements DomainObject {

	private Person() {
		// ZooDB
	}
	
	public Person(String name) {
		this.name = name;
		this.id = String.valueOf(name.hashCode());
	}
	
    public String getName()
    {
    	zooActivateRead();
    	return name;
    }
    
    public void setName(String name){
    	zooActivateWrite();
    	this.name = name;
    }

    public Set<Publication> getAuthoredPublications(){
    	zooActivateRead();
    	return this.authoredPublications;
    }
    
    public boolean removeAuthoredPublication(Publication publication){
    	zooActivateWrite();
    	return authoredPublications.remove(publication);
    }

    public void setAuthoredPublications(Set<Publication> authoredPublications){
    	zooActivateWrite();
    	this.authoredPublications = authoredPublications;
    }

    public Set<Publication> getEditedPublications(){
    	zooActivateRead();
    	return editedPublications;
    }
    
    public boolean removeEditedPublication(Publication publication){
    	zooActivateWrite();
    	return editedPublications.remove(publication);
    }

    public void setEditedPublications(Set<Publication> editedPublications){
    	zooActivateWrite();
    	this.editedPublications = editedPublications;
    }
    
    public void removeReferencesFromOthers() {
    	for (Publication pub : this.getAuthoredPublications()) {
			pub.removeAuthor(this);
		}
		for (Publication pub : this.getEditedPublications()) {
			pub.removeAuthor(this);
		}
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
	
	private String id;
	private String name;
	private Set<Publication> authoredPublications = new HashSet<>();
	private Set<Publication> editedPublications = new HashSet<>();

}
