package ch.ethz.globis.mtfobu.odb_project;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a person. The person can have the role of an author
 * or of an editor for some publications.
 */
public class Person implements DomainObject {

	
	public Person(String name) {
		this.name = name;
		this.id = String.valueOf(name.hashCode());
	}
	
    public String getName()
    {
    	return name;
    }
    
    public void setName(String name){
    	this.name = name;
    }

    public Set<Publication> getAuthoredPublications(){
    	return this.authoredPublications;
    }
    
    public boolean removeAuthoredPublication(Publication publication){
    	return authoredPublications.remove(publication);
    }

    public void setAuthoredPublications(Set<Publication> authoredPublications){
    	this.authoredPublications = authoredPublications;
    }

    public Set<Publication> getEditedPublications(){
    	return editedPublications;
    }
    
    public boolean removeEditedPublication(Publication publication){
    	return editedPublications.remove(publication);
    }

    public void setEditedPublications(Set<Publication> editedPublications){
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
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
		
	}
	
	private String id;
	private String name;
	private Set<Publication> authoredPublications = new HashSet<>();
	private Set<Publication> editedPublications = new HashSet<>();

}
