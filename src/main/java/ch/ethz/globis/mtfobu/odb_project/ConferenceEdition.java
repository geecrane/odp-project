package ch.ethz.globis.mtfobu.odb_project;

/**
 * Represents one edition of a conference. It contains a reference
 * to the proceedings published after the conference edition.
 */
public interface ConferenceEdition extends DomainObject {

    public Conference getConference();

    public void setConference(Conference conference);

    public int getYear();

    public void setYear(int year);

    public Proceedings getProceedings();

    public void setProceedings(Proceedings proceedings);
    
}