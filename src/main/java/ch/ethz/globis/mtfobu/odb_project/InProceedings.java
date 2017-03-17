package ch.ethz.globis.mtfobu.odb_project;

/**
 * A type of article that was published as part of a conference proceedings.
 */
public abstract class InProceedings extends Publication {

    public  abstract String getNote();

    public abstract void setNote(String note);

    public abstract String getPages();

    public abstract void setPages(String pages);

    public abstract Proceedings getProceedings();

    public abstract void setProceedings(Proceedings proceedings);
    
}