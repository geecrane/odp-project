package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;

/**
 * Defines the base state for a publication. Is inherited by all specialized
 * types of publications.
 */
public abstract class Publication implements DomainObject {

    public abstract String getTitle();

    public abstract void setTitle(String title);

    public abstract List<Person> getAuthors();

    public abstract void setAuthors(List<Person> authors);

    public abstract int getYear();

    public abstract void setYear(int year);

    public abstract String getElectronicEdition();

    public abstract void setElectronicEdition(String electronicEdition);
    
}