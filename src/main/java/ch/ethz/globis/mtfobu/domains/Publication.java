package ch.ethz.globis.mtfobu.domains;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
//Implemented constrains: 4
//Successfully tested constrains: 4
/**
 * Defines the base state for a publication. Is inherited by all specialized
 * types of publications.
 */
public interface Publication extends DomainObject {
	// constraint 2
	@NotNull(message = "Title must not be null")
	// does not null mean that it should not be empty?
	@NotBlank(message = "Title must not be empty")
    public String getTitle();

    public void setTitle(String title);
    @NotNull(message = "The list of authors can't be null")
    @Size(min=1, message ="Come on! There needs to be at least one author")
    public List<Person> getAuthors();
    
    public boolean removeAuthor(Person author);
    
    public void setAuthors(List<Person> authors);
 // constraint 3
 	@Min(value = 1901, message = "Only Publications after 1900 are accepted")
 	@Max(value = 2018, message = "Only Publications before 2018 are accepted")
    public int getYear();

    public void setYear(int year);

    public String getElectronicEdition();

    public void setElectronicEdition(String electronicEdition);
    
    
}