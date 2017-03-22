package ch.ethz.globis.mtfobu.odb_project;

import java.util.Set;

import org.zoodb.api.impl.ZooPC;

/**
 * Represents a person. The person can have the role of an author
 * or of an editor for some publications.
 */
public class Person extends ZooPC implements DomainObject {

	public Person() {
		// TODO Auto-generated constructor stub
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

    public void setAuthoredPublications(Set<Publication> authoredPublications){
    	zooActivateWrite();
    	this.authoredPublications = authoredPublications;
    }

    public Set<Publication> getEditedPublications(){
    	zooActivateRead();
    	return editedPublications;
    }

    public void setEditedPublications(Set<Publication> editedPublications){
    	zooActivateWrite();
    	this.editedPublications = editedPublications;
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
	private Set<Publication> authoredPublications;
	private Set<Publication> editedPublications;

}
