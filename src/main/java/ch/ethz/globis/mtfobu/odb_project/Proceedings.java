package ch.ethz.globis.mtfobu.odb_project;

import java.util.Set;

/**
 * A specialized type of publications, represents the proceedings released at
 * a certain conference edition. The proceedings contains all the articles published
 * at that conference edition.
 */
public interface Proceedings extends Publication {

    public String getNote();

    public void setNote(String note);

    public int getNumber();

    public void setNumber(int number);

    public Publisher getPublisher();

    public void setPublisher(Publisher publisher);

    public String getVolume();

    public void setVolume(String volume);

    public String getIsbn();

    public void setIsbn(String isbn);

    public Series getSeries();

    public void setSeries(Series series);

    public ConferenceEdition getConferenceEdition();

    public void setConferenceEdition(ConferenceEdition conferenceEdition);

    public Set<InProceedings> getPublications();

    public void setPublications(Set<InProceedings> publications);

}