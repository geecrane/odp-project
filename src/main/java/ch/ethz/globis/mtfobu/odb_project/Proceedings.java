package ch.ethz.globis.mtfobu.odb_project;

import java.util.Set;

/**
 * A specialized type of publications, represents the proceedings released at
 * a certain conference edition. The proceedings contains all the articles published
 * at that conference edition.
 */
public abstract class Proceedings extends Publication {

    public abstract String getNote();

    public abstract void setNote(String note);

    public abstract int getNumber();

    public abstract void setNumber(int number);

    public abstract Publisher getPublisher();

    public abstract void setPublisher(Publisher publisher);

    public abstract String getVolume();

    public abstract void setVolume(String volume);

    public abstract String getIsbn();

    public abstract void setIsbn(String isbn);

    public abstract Series getSeries();

    public abstract void setSeries(Series series);

    public abstract ConferenceEdition getConferenceEdition();

    public abstract void setConferenceEdition(ConferenceEdition conferenceEdition);

    public abstract Set<InProceedings> getPublications();

    public abstract void setPublications(Set<InProceedings> publications);

}