package ch.ethz.globis.mtfobu.odb_project;

import java.util.Set;

/**
 * Represents a person. The person can have the role of an author
 * or of an editor for some publications.
 */
public class Person implements DomainObject {

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

    public void setAuthoredPublications(Set<Publication> authoredPublications){
    	this.authoredPublications = authoredPublications;
    }

    public Set<Publication> getEditedPublications(){
    	return editedPublications;
    }

    public void setEditedPublications(Set<Publication> editedPublications){
    	this.editedPublications = editedPublications;
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
	private Set<Publication> authoredPublications;
	private Set<Publication> editedPublications;

}
