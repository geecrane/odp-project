package ch.ethz.globis.mtfobu.domains;

/**
 *  The base class for all domain objects.
 */
public interface DomainObject {

    public String getId();

    public void setId(String id);
    
    public void removeReferencesFromOthers();
    
}
