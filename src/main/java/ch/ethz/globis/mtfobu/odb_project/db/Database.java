package ch.ethz.globis.mtfobu.odb_project.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.ConferenceEdition;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.domains.Series;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Test;
import static org.junit.Assert.*;

public interface Database {

	public String getDBTechnology();

	// GUI Functions
	/**
	 * returns all the publishers in the database
	 * 
	 * @return List of {@link Publisher} objects. <u>The {@code publications}
	 *         field is not initialized.</u>
	 */
	public List<Publisher> getPublishers();

	public <T extends Publication> List<T> getPublications();

	/**
	 * returns the proceedings in a database
	 * 
	 * @return List of {@link Proceedins} objects. The following fields are
	 *         Initialized if possible:<br>
	 *         {@code id, title, year, editors, series, isbn, conf, publisher, InProceedings, volume, note, electronicEdition, number}
	 *         <p>
	 *         where the following fields are not fully initialized:
	 *         <ul>
	 *         <li>{@link InProceedings} have only {@link InProceedings#id} set
	 *         </li>
	 *         <li>{@link Person} is initialized
	 *         {@linkplain #getPersonById(String, boolean) lazily}
	 *         </ul>
	 * 
	 */
	public List<Proceedings> getProceedings();

	/**
	 * returns the inproceedings in a database
	 * 
	 * @return List of {@link InProceedins} objects. The following fields are
	 *         Initialized if possible:<br>
	 *         {@code id, title, year, authors, note, electronicEdition}
	 *         <p>
	 * 
	 */
	public List<InProceedings> getInProceedings();

	/**
	 * returns list of all people in the database
	 * 
	 * @return List of {@link Person} objects. <u>Only the name and the id is
	 *         initialized</u>
	 */
	public List<Person> getPeople();

	public List<Conference> getConferences();

	public List<Series> getSeries();

	public List<ConferenceEdition> getConferenceEditions();

	// General Functions

	public boolean openDB();

	public void closeDB();

	public boolean create();

	public void importData(HashMap<String, Proceedings> proceedingsList,
			HashMap<String, InProceedings> inProceedingsList, HashMap<Integer, Series> seriesList,
			HashMap<String, Publisher> publishers, HashMap<String, ConferenceEdition> conferenceEditions,
			HashMap<String, Conference> conferences, HashMap<Integer, Person> people);

	public InProceedings getInProceedingsById(String id);

	public Proceedings getProceedingById(String id);

	public Publication getPublicationById(String id);

	/**
	 * get the publisher given its name
	 * 
	 * @param name
	 *            Name of the Publisher, may be {@code null}
	 * @return a {@link Publisher} or {@code null} if the publisher could not be
	 *         found
	 */
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

	/**
	 * verifies if the given Proceeding is valid, if there is no redundancy
	 * regarding the id and add it to the Database if this is the case. Use this
	 * function to add something to the database and verify if it worked.
	 * 
	 * @param proc
	 * @return A list containing all the error messages if any. If the list is
	 *         empty the Proceeding was successfully added
	 */
	// verified add for proceeding
	public default List<String> addProceedingValidated(Proceedings proc) {
		List<String> errorMessages = validateProceedings(proc);
		if (proc != null && getPublicationById(proc.getId()) != null)
			errorMessages.add(
					String.format("Can not add proceeding with id: %s. There is already a publiction with the same id.",
							proc.getId()));
		assert errorMessages != null : "An Empty list is expected if no constraint has been violated.";
		if (errorMessages.isEmpty()) {
			addProceeding(proc);
		}
		return errorMessages;
	}

	/**
	 * Verifies if a given Proceeding is valid
	 * 
	 * @param proc
	 * @return a list of constraint violation messages. If the list is empty the
	 *         Proceeding enforces the constraints.
	 */
	public default List<String> validateProceedings(Proceedings proc) {
		List<String> constraintViolationMessages = new ArrayList<>();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Proceedings>> violations = validator.validate(proc);
		System.out.println(violations.size());
		for (ConstraintViolation<Proceedings> cv : violations) {
			constraintViolationMessages.add(cv.getMessage());
		}
		return constraintViolationMessages;

	}

	public void deleteProceedingById(String id);

	// verified delete for proceeding
	// TODO: This method can not fail, since it removes the inproceedings
	// automatically. Should this really be done by this function?
	public default List<String> deleteProceedigByIdValidated(String id) {
		Proceedings proc = getProceedingById(id);
		List<InProceedings> inProcs = (List<InProceedings>) proc.getPublications();
		assert inProcs != null : "List of inproceedings of proceeding is missing";
		// delete referenced inproceedings to ensure constraint 7 holds.
		for (InProceedings inProc : inProcs) {
			System.out.println(
					String.format("( delete proceeding with id: %s ) Inproceeding with id: %s was deleted implicitly",
							id, inProc.getId()));
			deleteInProceedingByIdValidated(inProc.getId());
		}
		return new ArrayList<>();
	}

	public void updateProceeding(Proceedings proc);

	// verified update for proceeding
	public default List<String> updateProceedingValidated(Proceedings proc) {
		List<String> errorMessages = validateProceedings(proc);
		assert errorMessages != null : "An Empty list is expected if no constraint has been violated.";
		if (errorMessages.isEmpty()) {
			updateProceeding(proc);
		}
		return errorMessages;
	}

	// InProceedings
	public void addInProceeding(InProceedings inProc);

	/**
	 * Verifies if a given inproceeding is valid, if there are no <b>id</b>
	 * collisions and adds it if this is the case. Otherwise a list is returned
	 * containing the error messages
	 * 
	 * @param inProc
	 * @return
	 */
	// verified add inproceeding
	public default List<String> addInProceedingValidated(InProceedings inProc) {
		List<String> errorMessages = validateInProceedings(inProc);
		if (getPublicationById(inProc.getId()) != null)
			errorMessages.add(String.format(
					"Can not add inproceeding with id: %s. There is already a publiction with the same id.",
					inProc.getId()));
		assert errorMessages != null : "An Empty list is expected if no constraint has been violated.";
		if (errorMessages.isEmpty()) {
			addInProceeding(inProc);
		}
		return errorMessages;
	}

	/**
	 * Verifies if a given inproceeding is valid
	 * 
	 * @param inProc
	 * @return a list of constraint violation messages. If the list is empty the
	 *         inproceeding enforces the constraints.
	 */
	public default List<String> validateInProceedings(InProceedings inProc) {
		List<String> constraintViolationMessages = new ArrayList<>();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<InProceedings>> violations = validator.validate(inProc);
		System.out.println(violations.size());
		for (ConstraintViolation<InProceedings> cv : violations) {
			constraintViolationMessages.add(cv.getMessage());
		}
		return constraintViolationMessages;
	}

	public void deleteInProceedingById(String id);

	// verifed delete inproceedings
	// TODO: Think again. Sure that there is nothing to care about here?
	public default List<String> deleteInProceedingByIdValidated(String id) {
		deleteInProceedingById(id);
		return new ArrayList<>();
	}

	// verifed update inproceedings
	public default List<String> updateInProceedingsValidated(InProceedings inProc) {
		List<String> errorMessages = validateInProceedings(inProc);
		assert errorMessages != null : "An Empty list is expected if no constraint has been violated.";
		if (errorMessages.isEmpty()) {
			// TODO: it works but this is not how it was inteded to be done
			deleteInProceedingById(inProc.getId());
			addInProceeding(inProc);
		}
		return errorMessages;
	}

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
	 *            Title of the publication. Assumed to not be {@code null}
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
	 *            Name of the person, assumed to not be {@code null}
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
	 *            id of first author. Assumed to not be {@code null}
	 * @param authorIdB
	 *            id of second author. Assumed to not be {@code null}
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
	// TODO: This function assumes conference id to be equal to it's name.
	// Please rename that function
	/**
	 * Retrieve all publications of a given conference. This is the list of all
	 * the publications that were featured in any edition of a given conference.
	 * This does not include the proceedings for this conference.
	 * 
	 * @param conferenceId
	 *            ID of the conference. Returns {@code null} if conference does
	 *            not exist
	 * @return List of {@link Publication}. May be {@code null}. Note that all
	 *         this publications are InProceedings.
	 */
	public List<Publication> getAllPublicationsOfConferenceByName(String conferenceId);

	// task 12
	public List<Person> getPeopleThatAreAuthorsAndEditors();

	// task 13
	public List<InProceedings> getPublicationsWhereAuthorIsLast(String authorName);

	// task 14
	public List<Publisher> task14(int yearLowerBound, int yearUpperBound);

	public class ValidationAPIUnitTest {

		public ValidationAPIUnitTest() {
		}

		@Test
		public void testMemberWithNoValues() {
			Proceedings member = new Proceedings();

			// validate the input
			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Proceedings>> violations = validator.validate(member);
			assertEquals(violations.size(), 5);
		}
	}
	// </constraintviolation<member>

}
