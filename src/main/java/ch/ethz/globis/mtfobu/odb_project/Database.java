package ch.ethz.globis.mtfobu.odb_project;

import java.util.List;

public interface Database {
	/**
	 * Used to retrieve the database instance. DO NOT try to instantiate the
	 * Database class manually!
	 * 
	 * @return The Database instance
	 */
	public Database getDatabase();

	// task 1
	/**
	 * Find a publication by id
	 * 
	 * @param pubID
	 *            Id of the publication. Assumed to not be <b>null</b>
	 * @return the publication or <b>null</b> otherwise.
	 */
	public Publication findPublicationById(String pubID);

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
	 * @return List containing the results per year in the following
	 * format:<br> {@code [year]:[number of publications]}
	 */
	public List<String> getNumberPublicationsPerYearInterval(int yearLowerBound, int yearUpperBound);
}
