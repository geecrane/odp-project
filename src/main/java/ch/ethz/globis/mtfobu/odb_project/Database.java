package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;
import java.util.Set;

public interface Database {

    // GUI Functions
    public List<Publisher> getPublishers();

    public List<Publication> getPublications();

    public List<Proceedings> getProceedings();

    public List<InProceedings> getInProceedings();

    public List<Person> getPeople();

    public List<Conference> getConferences();

    public List<Series> getSeries();

    public List<ConferenceEdition> getConferenceEditions();

    // General Functions

    public boolean openDB();

    public void closeDB();

    public InProceedings getInProceedingsById(String id);

    public Proceedings getProceedingById(String id);

    public Publication getPublicationById(String id);

    public Publisher getPublisherByName(String name);

    /**
     * Get Person object. If lazy=false, loads authored & edited publications
     * 
     * @param id
     * @param lazy
     * @return Person
     */
    public Person getPersonById(String id, boolean lazy);

    /**
     * Get set of Publications. If lazy=false, loads authors & their
     * corresponding InProceedings
     * 
     * @param personName
     * @param lazy
     * @return
     */
    public Set<Publication> getAuthoredPublications(String personName, boolean lazy);

    /**
     * Get set of Publications. If lazy=false, loads editors & their
     * corresponding Proceedings
     * 
     * @param personName
     * @param lazy
     * @return
     */
    public Set<Publication> getEditedPublications(String personName, boolean lazy);

    /**
     * Get Conference object. If lazy=false, loads ConferenceEditions
     * 
     * @param confName
     * @param lazy
     * @return
     */
    public Conference getConferenceByName(String confName, boolean lazy);

    public Set<ConferenceEdition> getConfEditionsForConf(Conference conference);

    public Series getSeriesByName(String name);

    // Database Mutation Functions

    // Proceedings
    public void addProceeding(Proceedings proc);

    public void deleteProceedingById(String id);

    public void updateProceeding(Proceedings proc);

    // InProceedings
    public void addInProceeding(InProceedings inProc);

    public void deleteInProceedingById(String id);

    // Task functions


    // task 2+3
    /**
     * Find publications by filter (title, begin-offset, end-offset). This
     * enables to retrieve publications containing a certain search string in
     * the title while only a subset of all hits is returned. begin-offset and
     * end-offset define the start and end point of the retrieved range, e.g.
     * from the 20th to the 30th publication in the whole set.
     * 
     * @param title
     *            Title of the publication. Assumed to not be <b>null</b>
     * @param begin_offset
     *            Offset from the beginning of the first result. Has to be
     *            greater or equal to 0
     * @param end_offset
     *            Offset from the beginning of the first result. If negative all
     *            the results are returned
     * @return List containing the found publications
     */
    public List<Publication> getPublicationsByFilter(String title, int begin_offset, int end_offset);

    // task 4
    /**
     * Find the co-authors of a person with given name. Co-authors of a person
     * are all the authors that have published a publication together with this
     * person.
     * 
     * @param name
     *            Name of the person, assumed to not be <b>null</b>
     * @return List containing all the found Co-authors as a {@link Person}
     *         object.
     */
    public List<Person> getCoAuthors(String name);

    // task 5
    /**
     * Find the shortest path between two authors with the author distance. For
     * this, consider the graph consisting of authors nodes in the database with
     * edges between persons who are co-authors on a publication. The author
     * distance is the shortest distance (number of edges) between two persons
     * on such a graph.
     * 
     * @param authorIdA
     *            id of first author. Assumed to not be <b>null</b>
     * @param authorIdB
     *            id of second author. Assumed to not be <b>null</b>
     * @return distance between authors or -1 if no distance could be
     *         determined.
     */
    public int authorDistance(String authorIdA, String authorIdB);

    // task 6
    /**
     * Compute the global average number of authors per publication (only
     * InProceedings , not Proceedings )
     * 
     * @return average author distance
     */
    public double getAvgAuthorsInProceedings();

    // task 7
    /**
     * Counts the number of publications per year in a given interval of years.
     * This means it returns the number of publications that appeared in each of
     * the years in the specified interval.
     * 
     * @param yearLowerBound
     * @param yearUpperBound
     * @return List containing the results per year in the following format:<br>
     *         {@code [year]:[number of publications]}
     */
    public List<String> getNumberPublicationsPerYearInterval(int yearLowerBound, int yearUpperBound);

    // task 8
    public int getNumberOfPublicationsPerConferenceByName(String conferenceName);

    // task 9
    public int countEditorsAndAuthorsOfConferenceByName(String conferenceName);

    // task 10
    public List<Person> getAllAuthorsOfConferenceByName(String conferenceName);

    // task 11
    public List<Publication> getAllPublicationsOfConferenceByName(String conferenceName);

    // task 12
    public List<Person> getPeopleThatAreAuthorsAndEditors();

    // task 13
    public List<InProceedings> getPublicationsWhereAuthorIsLast(String authorName);

    // task 14
    public List<Publisher> task14(int yearLowerBound, int yearUpperBound);

}
