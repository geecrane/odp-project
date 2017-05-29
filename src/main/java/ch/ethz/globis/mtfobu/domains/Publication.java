package ch.ethz.globis.mtfobu.domains;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
//Implemented constrains: 4
//Successfully tested constrains: 4
/**
 * Defines the base state for a publication. Is inherited by all specialized
 * types of publications.
 */
public interface Publication extends DomainObject {

    public String getTitle();

    public void setTitle(String title);
    @NotNull(message = "The list of authors can't be null")
    @Size(min=1, message ="Come on! There needs to be at least one author")
    public List<Person> getAuthors();
    
    public boolean removeAuthor(Person author);
    
    public void setAuthors(List<Person> authors);

    public int getYear();

    public void setYear(int year);

    public String getElectronicEdition();

    public void setElectronicEdition(String electronicEdition);
    
}