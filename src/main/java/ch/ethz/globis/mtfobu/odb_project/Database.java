package ch.ethz.globis.mtfobu.odb_project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.basex.BaseXServer;
import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.bson.BSON;
import org.bson.conversions.Bson;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.sun.javafx.scene.control.behavior.OptionalBoolean;

//This is our version
import ch.ethz.globis.mtfobu.odb_project.BaseXClient.Query;
//This version is used if the BaseX Server is also executed by the program. 
// Since I was not able to get a proper description and because the previous 
// version was linked on the task sheet, I used the previous one.

//import org.basex.api.client.Query;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

//George: Instatiate db  only once!
//Otherwise, creates a new server every time!
public class Database {
	private final String dbName;
	private ClientSession session = null;
	final String defaultHost = "localhost";
	final int defaultPort = 1984;
	final String defaultUsername = "admin";
	final String defaultPassword = "admin";
	final String rootDocumentName = "dblp_filtered.xml";

	private static class Singleton {
		private static final Database instance = new Database(Config.DATABASE_NAME);
	}

	/**
	 * Used to retrieve the database instance. DO NOT try to instantiate the
	 * Database class manually!
	 * 
	 * @return The Database instance
	 */
	public static Database getDatabase() {
		return Singleton.instance;
	}

	private Database(String dbName) {

		this.dbName = dbName;

		this.session = createSession();

		if (this.session != null) {
			if (!openDB()) {
				// db doesnt exist. Create a new one!
				if (create()) {
					openDB();
				}
			}
		}

	}

	private boolean openDB() {
		// try to open DB
		try {
			session.execute(new Open(dbName));
			return true;
		} catch (IOException e) {
			System.err.printf("DB: %s Not found!\n", dbName);
			return false;
		}

	}

	/**
	 * Creates a connection to database
	 * 
	 * @return ClientSession
	 */
	private ClientSession createSession() {
		// try to connect
		try {
			return new ClientSession(defaultHost, defaultPort, defaultUsername, defaultPassword);
		} catch (IOException e1) {
			System.err.println("Could not connect to Server!");
			return null;
		}
	}

	/**
	 * This will drop the database if already exists Will create DB and import
	 * xml. Note:(XmlImport Not Needed)!
	 */
	public boolean create() {

		try {
			// drop if exists
			session.execute(new DropDB(dbName));
		} catch (IOException e1) {
			// Nothing to delete!
		}

		try {
			session.execute(new CreateDB(dbName,
					String.format("%s/src/main/resources/dblp_filtered.xml", System.getProperty("user.dir"))));
			return true;
		} catch (IOException e) {
			System.err.printf("Could not create database: %s! Check XML path!\n", dbName);
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * @return A list of all InProceedings
	 */
	public List<InProceedings> getInProceedings() {
		ArrayList<InProceedings> inProcs = new ArrayList<>();
		String queryString = "for $inProc in //inproceedings return data($inProc/@key)";
		ClientQuery query;
		try {
			query = session.query(queryString);
			while (query.more()) {
				inProcs.add(getInProceedingsById(query.next()));
			}

		} catch (IOException e) {
			System.err.println("Could not query for inProceedings in getInProceedings()");
		}

		return inProcs;
	}

	// George: get inProceedings by id
	public InProceedings getInProceedingsById(String id) {
		InProceedings inProc = new InProceedings(id);
		String InProcByIDQuery = "for $inProc in root/inproceedings where $inProc/@key=\"" + id + "\" return $inProc";
		ClientQuery query;
		try {
			query = session.query(InProcByIDQuery);
			String queryResult = null;
			if (query.more()) {
				queryResult = query.next();

				// The result is in XML format therefore JDOM is used in order
				// to parse it
				SAXBuilder builder = new SAXBuilder();
				InputStream stream = new ByteArrayInputStream(queryResult.getBytes("UTF-8"));
				try {
					Document inprocXML = builder.build(stream);
					Element rootNode = inprocXML.getRootElement();
					inProc.setTitle(rootNode.getChildText("title"));
					inProc.setYear(Integer.parseInt(rootNode.getChildText("year")));
					List<Element> authors = rootNode.getChildren("author");
					List<Person> authorList = new ArrayList<>();
					for (Element author : authors)
						authorList.add(new Person(author.getText()));
					inProc.setAuthors(authorList);
					inProc.setNote(rootNode.getChildText("note"));
					inProc.setPages(rootNode.getChildText("pages"));
				} catch (JDOMException e) {
					System.out.println("Error: The query result was not in the expected XML format");
					e.printStackTrace();
					return null;
				}
				// System.out.println(queryResult);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return inProc;
	}

	public Proceedings getProceedingById(String id) {
		String proceedingByIDQuery = "for $proc in root/proceedings where $proc/@key=\"" + id + "\" return $proc";
		ClientQuery query;
		try {
			query = session.query(proceedingByIDQuery);
			String queryResult = null;
			if (query.more()) {
				queryResult = query.next();
				System.out.println(queryResult);
				// The result is in XML format therefore JDOM is used in order
				// to parse it
				return XmlToObject.XmlToProceeding(queryResult, this);

			} else {
				System.out.println("A proceeding with id: " + id + " could not be found");
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// TASK 1:
	// Seba: get publication given the id. The result is either a proceeding or
	// an inproceeding
	// TODO: Not all attributes of a proceeding are set
	public Publication getPublicationById(String id) {
		String PublicationByIDQuery = "for $pub in root/proceedings | root/inproceedings where $pub/@key=\"" + id
				+ "\" return $pub";
		ClientQuery query;
		try {
			query = session.query(PublicationByIDQuery);
			String queryResult = null;
			if (query.more()) {
				queryResult = query.next();
				System.out.println(queryResult);
				// The result is in XML format therefore JDOM is used in order
				// to parse it
				return XmlToObject.XmlToPublication(queryResult, this);

			} else {
				System.out.println("A publication with id: " + id + " could not be found");
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// Helper function task 1
	public Series getSeriesByName(String name) {
		// TODO: there is a little bit more to be done here
		return new Series(name);
	}

	// Helper function task 1
	public Publisher getPublisherByName(String name) {
		Publisher pub = new Publisher(name);
		Set<Publication> pubs = new HashSet<>();
		String getPublicationsFromPublisher = "let $publisherName := \"" + name
				+ "\" for $pub in //proceedings | //inproceedings where $pub/publisher=$publisherName return $pub";
		// TODO: Verify that it is sound to use the publisher name as an ID
		pub.setId(name);
		ClientQuery query;
		try {
			query = session.query(getPublicationsFromPublisher);
			while (query.more()) {
				String publication = query.next();
				pubs.add(XmlToObject.XmlToPublication(publication, this));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pub.setPublications(pubs);
		return pub;
	}

	// TASK 2+3:
	public List<Publication> getPublicationsByFilter(String title, int begin_offset, int end_offset) {
		assert (begin_offset >= 0);
		List<Publication> pubs = new ArrayList<>();
		String PublicationsByFilter;
		// return all results
		if (end_offset - begin_offset < 0) {
			PublicationsByFilter = "let $title := \"" + title + "\" for $pub in root/proceedings | root/inproceedings"
					+ " where contains($pub/title, $title)" + "order by $pub/title" + " return $pub";
		}
		// filter results by offset
		else {
			PublicationsByFilter = "(let $title := \"" + title + "\" for $pub in root/proceedings | root/inproceedings"
					+ " where contains($pub/title, $title)" + "order by $pub/title" + " return $pub)[position() = "
					+ begin_offset + " to " + end_offset + "]";
		}
		ClientQuery query;
		try {
			query = session.query(PublicationsByFilter);
			while (query.more()) {
				String queryResult = query.next();
				System.out.println(queryResult);
				pubs.add(XmlToObject.XmlToPublication(queryResult, this));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pubs;
	}

	// TASK 4:
	public List<Person> getCoAuthors(String name) {
		List<Person> coAuthors = new ArrayList<>();
		String CoAuthorsGivenAuthor = "distinct-values(let $author := \"" + name
				+ "\" for $pub in root/proceedings | root/inproceedings"
				+ " let $index := index-of(($pub/author | $pub/editor), $author)" + " let $coAuthor := if ($index > 0)"
				+ " then remove(($pub/author | $pub/editor), $index) else ()" + " return $coAuthor)";
		ClientQuery query;
		try {
			query = session.query(CoAuthorsGivenAuthor);
			while (query.more()) {
				String coAutherName = query.next();
				// this uses the characteristic of the above query to only
				// return the names
				coAuthors.add(getPersonByName(coAutherName));
				System.out.println("Found co-auther: " + coAutherName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return coAuthors;
	}

	// Helper function task 4
	public Person getPersonByName(String name) {
		// TODO: There is a little bit more to be done here
		return new Person(name);
	}

	// TASK 5:
	// Query to use: TODO: There is a bug at line 369
	// declare function local:buildOrigin($authorName as xs:string){
	// <author><name>{string($authorName)}</name><dist>{number(0)}</dist></author>
	// };
	// declare function local:getCoAuthors($author as xs:string, $dist as
	// xs:integer, $rootNode as element()){
	// for $coauthor in distinct-values(
	// for $pub in $rootNode/proceedings | $rootNode/inproceedings
	// let $index := index-of(($pub/author | $pub/editor), $author)
	// let $coAuthor := if ($index > 0)
	// then remove(($pub/author | $pub/editor), $index)
	// else ()
	// return $coAuthor)
	// return
	// <author><name>{string($coauthor)}</name><dist>{number($dist)}</dist></author>
	// };
	// declare function local:updateWorkingSet($workingSet, $updateSet){
	// for $authorOld in $workingSet, $authorNew in $updateSet
	// let $author := ( if($authorOld/name=$authorNew/name)
	// then (if ($authorOld/dist<=$authorNew/dist)
	// then $authorOld
	// else $authorNew)
	// else $authorOld)
	// return $author
	// };
	// declare function local:getAuthorDist($doneSet, $workingSet, $authorName
	// as xs:string, $rootNode as element())
	// {
	// for $author in $workingSet
	// where $author/dist=min($workingSet/dist)
	// let $newDoneSet := ($doneSet | $author)
	// let $newWorkingSet :=
	// local:updateWorkingSet($workingSet,local:getCoAuthors($author,
	// $author/dist,$rootNode))
	// let $dist := (if($author/name=$authorName)
	// then $author/dist
	// else local:getAuthorDist($newDoneSet, $newWorkingSet, $authorName,
	// $rootNode))
	// return $dist
	//
	// };
	// let $author := local:buildOrigin("Peter Buneman")
	// return local:getAuthorDist($author,local:getCoAuthors($author/name,1,
	// root), "Fred Brown", root )


	
	// TASK 6:
	public double getAvgAuthorsInProceedings(){
		String avgAuthorsInProceedingsQuery = "avg(for $inProc in //inproceedings return count($inProc/author))";
		ClientQuery query;
		try {
			query = session.query(avgAuthorsInProceedingsQuery);
			assert(query.more());
			return Double.parseDouble(query.next());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Double.NaN;
		
	}
	// Seba: get person by id
	// id is expected to be the name of the person
	public Person getPersonById(String id) {
		Person per = new Person(id);
		per.setId(id);
		per.setAuthoredPublications(getAuthoredPublications(per.getName()));
		per.setEditedPublications(getEditedPublications(per.getName()));
		return per;
	}

	public Set<Publication> getAuthoredPublications(String personName) {
		Set<Publication> authoredPubs = new HashSet<>();
		String AuthoredInProceedings = "let $PersonName := \"" + personName
				+ "\" for $inproc in root/inproceedings where index-of(($inproc/author), $PersonName)!=0 return $inproc";
		ClientQuery query;
		try {
			query = session.query(AuthoredInProceedings);
			while (query.more()) {
				String authoredInProceeding = query.next();
				authoredPubs.add(XmlToObject.XmlToInProceeding(authoredInProceeding, this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return authoredPubs;
	}

	public Set<Publication> getEditedPublications(String personName) {
		Set<Publication> authoredPubs = new HashSet<>();
		String editedProceedings = "let $PersonName := \"" + personName
				+ "\" for $proc in root/proceedings where index-of(($proc/editor), $PersonName)!=0 return $proc";
		ClientQuery query;
		try {
			query = session.query(editedProceedings);
			while (query.more()) {
				String editedProceeding = query.next();
				authoredPubs.add(XmlToObject.XmlToProceeding(editedProceeding, this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return authoredPubs;
	}

	public Conference getConferenceByName(String confName) {
		Conference conf = new Conference(confName);
		conf.setId(confName);
		conf.setEditions(getConfEditionsForConf(conf));
		return conf;
	}

	public Set<ConferenceEdition> getConfEditionsForConf(Conference conference) {
		Set<ConferenceEdition> confEdits = new HashSet<>();
		String confEditionsGivenConfQuery = "let $confName := \"" + conference.getName()
				+ "\" for $proc in root/proceedings where $proc/booktitle=$confName"
				+ " return <ConfEdit>{$proc/year,$proc}</ConfEdit>";
		ClientQuery query;

		try {
			query = session.query(confEditionsGivenConfQuery);
			while (query.more()) {
				String confedit = query.next();
				confEdits.add(XmlToObject.XmlToConferenceEdition(confedit, conference, this));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return confEdits;
	}

	// public String getPersonIdFromName(String name) {
	// String id;
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PEOPLE_COLLECTION);
	// MongoCursor<Document> doc = collection.find(eq(Config.PEOPLE_NAME,
	// name)).iterator();
	// id = doc.next().getString("_id");
	// if (doc.hasNext())
	// System.out.println("Warning: multiple people share the same name");
	// return id;
	// }
	//
	// public String getConferenceIdFromName(String name) {
	// String id;
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	// MongoCursor<Document> doc =
	// collection.find(eq(Config.CONFERENCE_COLLECTION, name)).iterator();
	// id = doc.next().getString("_id");
	// if (doc.hasNext())
	// System.out.println("Warning: multiple conferences share the same name");
	// return id;
	// }
	//
	// // George: get publisher by id
	// public Publisher getPublisherById(String series_id) {
	// // TODO:implement
	// return null;
	// }
	//
	// // George: get publications for a series by series id
	// public Set<Publication> getSeriesPublicationsById(String series_id) {
	// // TODO:implement
	// return null;
	// }
	//
	// // George: get ConferenceEdition By ID
	// public ConferenceEdition getConferenceEditionById(String id) {
	// // TODO:implement
	// return null;
	// }
	//
	// // George: get Conference By ID
	// public Conference getConferenceById(String id) {
	// // TODO:implement
	// return null;
	// }
	//
	// // George: Get Series by ID
	// public Series getSeriesById(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.SERIES_COLLECTION);
	// Document doc = collection.find(eq("_id", id)).first();
	//
	// if (doc != null) {
	// String name = doc.get(Config.SERIES_NAME).toString();
	// Set<Publication> publications = getSeriesPublicationsById(id);
	// Series s = new Series(name);
	// s.setPublications(publications);
	// return s;
	//
	// } else {
	// return null;
	// }
	//
	// }
	//
	// // Helper class to construct a function for querying or searching with
	// // paging or ranged accesses for any domain object
	// public class QueryHelper<DO extends DomainObject> {
	// private String collectionToUse;
	// private String fieldToSearch;
	// private Function<Document, DO> parser;
	//
	// public QueryHelper(String collectionToUse, String fieldToSearch,
	// Function<Document, DO> parser) {
	// this.collectionToUse = collectionToUse;
	// this.fieldToSearch = fieldToSearch;
	// this.parser = parser;
	// }
	//
	// public Collection<DO> queryForDomainObject(QueryParameters p) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(collectionToUse);
	// MongoCursor<Document> cursor;
	//
	// if (p.isRanged) {
	// long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
	// long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() :
	// Long.MAX_VALUE;
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch)).skip((int)
	// begin)
	// .limit((int) (end - begin)).iterator();
	// } else {
	// cursor = collection.find().sort(ascending(fieldToSearch)).skip((int)
	// begin)
	// .limit((int) (end - begin)).iterator();
	// }
	//
	// } else {
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch))
	// .skip((p.pageNumber - 1) *
	// Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
	// } else {
	// cursor = collection.find().sort(ascending(fieldToSearch))
	// .skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
	// }
	//
	// }
	//
	// Collection<DO> coll = new ArrayList<DO>(Config.PAGE_SIZE);
	//
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// coll.add(parser.apply(doc));
	// }
	//
	// return coll;
	// }
	// }
	//
	// // Special case for the Publications Query
	// public Collection<Publication> queryForPublications(QueryParameters p) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// MongoCursor<Document> cursor;
	//
	// if (p.isRanged) {
	// long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
	// long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() :
	// Long.MAX_VALUE;
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE))
	// .skip((int) begin).limit((int) (end - begin)).iterator();
	// } else {
	// cursor =
	// collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((int)
	// begin)
	// .limit((int) (end - begin)).iterator();
	// }
	//
	// } else {
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE))
	// .skip((p.pageNumber - 1) *
	// Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
	// } else {
	// cursor = collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE))
	// .skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
	// }
	//
	// }
	//
	// List<Publication> list = new ArrayList<Publication>(2 *
	// Config.PAGE_SIZE);
	//
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// list.add(makeInProceedingsObject(doc));
	// }
	//
	// collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	//
	// if (p.isRanged) {
	// long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
	// long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() :
	// Long.MAX_VALUE;
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int)
	// begin)
	// .limit((int) (end - begin)).iterator();
	// } else {
	// cursor =
	// collection.find().sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int)
	// begin)
	// .limit((int) (end - begin)).iterator();
	// }
	//
	// } else {
	//
	// if (p.isSearch) {
	// cursor =
	// collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE))
	// .skip((p.pageNumber - 1) *
	// Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
	// } else {
	// cursor = collection.find().sort(ascending(Config.PROCEEDINGS_TITLE))
	// .skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
	// }
	//
	// }
	//
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// list.add(makeProceedingsObject(doc));
	//
	// }
	//
	// // sorting the combined result list again, otherwise all InProceedings
	// // are at the top
	// list.sort(java.util.Comparator.comparing(publication ->
	// publication.getTitle()));
	//
	// return list;
	// }
	//
	// // Query Helper for Proceedings
	// public QueryHelper<ConferenceEdition> conferenceEditionQueryHelper = new
	// QueryHelper<ConferenceEdition>(
	// Config.CONFERENCE_EDITION_COLLECTION, Config.CONFERENCE_EDITION_YEAR,
	// this::makeConferenceEditionObject);
	// public QueryHelper<Conference> conferenceQueryHelper = new
	// QueryHelper<Conference>(Config.CONFERENCE_COLLECTION,
	// Config.CONFERENCE_NAME, this::makeConferenceObject);
	// public QueryHelper<InProceedings> inProceedingsQueryHelper = new
	// QueryHelper<InProceedings>(
	// Config.INPROCEEDINGS_COLLECTION, Config.INPROCEEDINGS_TITLE,
	// this::makeInProceedingsObject);
	// public QueryHelper<Person> personQueryHelper = new
	// QueryHelper<Person>(Config.PEOPLE_COLLECTION, Config.PEOPLE_NAME,
	// this::makePersonObject);
	// public QueryHelper<Proceedings> proceedingsQueryHelper = new
	// QueryHelper<Proceedings>(Config.PROCEEDINGS_COLLECTION,
	// Config.PROCEEDINGS_TITLE, this::makeProceedingsObject);
	// // No publication query handler, it's a bit of a special case
	// public QueryHelper<Publisher> publisherQueryHelper = new
	// QueryHelper<Publisher>(Config.PUBLISHER_COLLECTION,
	// Config.PUBLISHER_NAME, this::makePublisherObject);
	// public QueryHelper<Series> seriesQueryHelper = new
	// QueryHelper<Series>(Config.SERIES_COLLECTION, Config.SERIES_NAME,
	// this::makeSeriesObject);
	//
	// private ConferenceEdition makeConferenceEditionObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// private Conference makeConferenceObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// private InProceedings makeInProceedingsObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// private Person makePersonObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// private Proceedings makeProceedingsObject(Document doc) {
	//
	// Proceedings proceedings = new Proceedings((String)
	// doc.get(Config.MONGODB_PRIMARY_KEY));
	//
	// String title = (String) doc.get(Config.PROCEEDINGS_TITLE);
	// int year = (int) doc.get(Config.PROCEEDINGS_YEAR);
	// String isbn = (String) doc.get(Config.PROCEEDINGS_ISBN);
	// String volume = (String) doc.get(Config.PROCEEDINGS_VOLUME);
	// String note = (String) doc.get(Config.PROCEEDINGS_NOTE);
	// String ee = (String) doc.get(Config.PROCEEDINGS_ELECTRONIC_EDITION);
	// int number = (int) doc.get(Config.PROCEEDINGS_NUMBER);
	//
	// proceedings.setTitle(title);
	// proceedings.setYear(year);
	// proceedings.setIsbn(isbn);
	// proceedings.setVolume(volume);
	// proceedings.setNote(note);
	// proceedings.setElectronicEdition(ee);
	// proceedings.setNumber(number);
	//
	// String series_key = (String) doc.get(Config.PROCEEDINGS_SERIES_KEY);
	// Series series = getSeriesById(series_key);
	// proceedings.setSeries(series);
	//
	// String conf_ed_key = (String)
	// doc.get(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY);
	// ConferenceEdition conferenceEdition =
	// getConferenceEditionById(conf_ed_key);
	// proceedings.setConferenceEdition(conferenceEdition);
	//
	// String publisher_key = (String)
	// doc.get(Config.PROCEEDINGS_PUBLISHER_KEY);
	// Publisher publisher = getPublisherById(publisher_key);
	// proceedings.setPublisher(publisher);
	//
	// ArrayList<String> editor_keys = (ArrayList<String>)
	// doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
	// ArrayList<Person> authors = new ArrayList<>();
	// for (String key : editor_keys) {
	// Person p = getPersonById(key);
	// authors.add(p);
	// }
	// proceedings.setAuthors(authors);
	//
	// ArrayList<String> inproceedings_keys = (ArrayList<String>)
	// doc.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
	// HashSet<InProceedings> inproceedingsList = new HashSet<>();
	// for (String key : inproceedings_keys) {
	// InProceedings inProceedings = getInProceedingsById(key);
	// inproceedingsList.add(inProceedings);
	// }
	// proceedings.setPublications(inproceedingsList);
	//
	// return proceedings;
	// }
	//
	// private Publisher makePublisherObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// private Series makeSeriesObject(Document doc) {
	// // TODO:implement
	// return null;
	// }
	//
	// // get inproceedings where author (by id) appears last
	// public List<InProceedings> getInproceedingsAuthorLast(String id) {
	// /*
	// * db.getCollection('inproceedings').aggregate([ { $project: {
	// * author_keys: { $slice: [ "$author_keys", -1 ] } } }, { $match: {
	// * author_keys: "1785178126" } } ])
	// */
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// ArrayList<InProceedings> results = new ArrayList<>();
	//
	// Block<Document> block = new Block<Document>() {
	// @Override
	// public void apply(final Document document) {
	// System.out.println(document.toJson());
	// String id = (String) document.get("_id");
	// InProceedings inProceedings = getInProceedingsById(id);
	// if (inProceedings != null)
	// results.add(inProceedings);
	// }
	// };
	// BasicDBList author_keys = new BasicDBList();
	// author_keys.add("$author_keys");
	// author_keys.add(-1);
	//
	// collection.aggregate(Arrays.asList(
	// Aggregates.project(
	// Projections.fields(new BasicDBObject("author_keys", new
	// BasicDBObject("$slice", author_keys)))),
	// Aggregates.match(eq("author_keys", id)))).forEach(block);
	//
	// return results;
	// }
	//
	// // task 4
	// public List<Person> getCoAuthores(String authorId) {
	// MongoCursor<Document> cursor;
	// Set<String> foundCoAu = new HashSet<>();
	// MongoCollection<Document> col;
	//
	// col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// cursor = col.find(eq(Config.INPROCEEDINGS_AUTHOR_KEYS,
	// authorId)).iterator();
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// @SuppressWarnings("unchecked")
	// ArrayList<String> ids = (ArrayList<String>)
	// doc.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
	// for (String i : ids) {
	// if (!foundCoAu.contains(i)) {
	// foundCoAu.add(i);
	// }
	// }
	//
	// }
	// col = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// cursor = col.find(eq(Config.PROCEEDINGS_EDITOR_KEYS,
	// authorId)).iterator();
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// @SuppressWarnings("unchecked")
	// ArrayList<String> ids = (ArrayList<String>)
	// doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
	// for (String i : ids) {
	// if (!foundCoAu.contains(i)) {
	// foundCoAu.add(i);
	// }
	// }
	//
	// }
	// foundCoAu.remove(authorId);
	// ArrayList<Person> coAuthors = new ArrayList<>(foundCoAu.size());
	//
	// for (String i : foundCoAu) {
	// coAuthors.add(getPersonById(i));
	// }
	//
	// return coAuthors;
	//
	// }
	//
	// // task 5
	// public int authorDistance(String authorIdA, String authorIdB) {
	// if (authorIdA == authorIdB)
	// return 0;
	// HashMap<Integer, Integer> doneSet = new HashMap<>();
	// HashMap<Integer, Integer> remainingSet = new HashMap<>();
	//
	// int currentAuthor = Integer.parseInt(authorIdA);
	// doneSet.put(currentAuthor, 0);
	//
	// HashSet<Integer> coAuthors =
	// getCoAuthoresIds(Integer.toString(currentAuthor));
	// for (Integer i : coAuthors) {
	// remainingSet.put(i, 1);
	// }
	//
	// while (!remainingSet.isEmpty()) {
	// // get nearest author
	// int currentdist = Integer.MAX_VALUE;
	// for (Integer i : remainingSet.keySet()) {
	// if (remainingSet.get(i) < currentdist) {
	// currentAuthor = i;
	// currentdist = remainingSet.get(i);
	// }
	// }
	// if (currentAuthor != Integer.parseInt(authorIdB)) {
	// coAuthors = getCoAuthoresIds(Integer.toString(currentAuthor));
	// doneSet.put(currentAuthor, currentdist);
	// remainingSet.remove(currentAuthor);
	// for (Integer id : coAuthors) {
	// if (doneSet.containsKey(id))
	// continue;
	// if (remainingSet.containsKey(id)) {
	// // path is shorter than a previously known one
	// if (remainingSet.get(id) > doneSet.get(currentAuthor) + 1) {
	// remainingSet.replace(id, doneSet.get(currentAuthor) + 1);
	// }
	// // path is longer
	// else {
	// // discard
	// }
	// } else {
	// remainingSet.put(id, doneSet.get(currentAuthor) + 1);
	// }
	// }
	// } else
	// return currentdist;
	//
	// }
	// System.out.println("Task 5: author distance couldn't be determined.
	// Author not found!");
	// return 0;
	// }
	//
	// // task 5 helper function
	// private HashSet<Integer> getCoAuthoresIds(String authorId) {
	// MongoCursor<Document> cursor;
	// HashSet<Integer> foundCoAu = new HashSet<>();
	// MongoCollection<Document> col;
	//
	// col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// cursor = col.find(eq(Config.INPROCEEDINGS_AUTHOR_KEYS,
	// authorId)).iterator();
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// @SuppressWarnings("unchecked")
	// ArrayList<String> ids = (ArrayList<String>)
	// doc.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
	// for (String i : ids) {
	// int n = Integer.parseInt(i);
	// if (!foundCoAu.contains(n)) {
	// foundCoAu.add(n);
	// }
	// }
	//
	// }
	// col = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// cursor = col.find(eq(Config.PROCEEDINGS_EDITOR_KEYS,
	// authorId)).iterator();
	// while (cursor.hasNext()) {
	// Document doc = cursor.next();
	// @SuppressWarnings("unchecked")
	// ArrayList<String> ids = (ArrayList<String>)
	// doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
	// for (String i : ids) {
	// int n = Integer.parseInt(i);
	// if (!foundCoAu.contains(n)) {
	// foundCoAu.add(n);
	// }
	// }
	//
	// }
	// foundCoAu.remove(Integer.parseInt(authorId));
	//
	// return foundCoAu;
	//
	// }
	//
	// // task 6
	// // TODO: Seems to work but PIA to verify without GUI
	// public double globalAvgAuthors() {
	// double result = 0;
	// int nInPro = 0;
	// MongoCollection<Document> col =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// String inProcMap = "function(){emit(this._id, " + "this." +
	// Config.INPROCEEDINGS_AUTHOR_KEYS + ".length );}";
	// String inProcReduce = "function(key, authors){return
	// Array.sum(authors);}"; // Array.sum(authors)
	// MapReduceIterable<Document> res = col.mapReduce(inProcMap, inProcReduce);
	// for (Document doc : res) {
	// result += doc.getDouble("value");
	// nInPro++;
	// }
	// result /= (double) nInPro;
	//
	// return result;
	// }
	//
	// // task 7
	// // @retrun: The hashmap takes as key the year and returns the number of
	// // publications as value
	// // @param: the bounds are not inclusive
	// public HashMap<Integer, Integer> noPublicationsPerYear(int
	// yearLowerBound, int yearUpperBound) {
	// System.out.println("task 7: please look at the code. There is a bug I
	// have not been able to find. ");
	// HashMap<Integer, Integer> result = new HashMap<>();
	//
	// Block<Document> block = new Block<Document>() {
	// @Override
	// public void apply(final Document document) {
	// System.out.println(document.toJson());
	// int year = document.getInteger("year");
	// int pubs = document.getInteger("publications");
	// result.put(year, pubs);
	// }
	// };
	// MongoCollection<Document> col =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// DBObject matchFields = new BasicDBObject("$gt", yearLowerBound);
	// matchFields.put("$lt", yearUpperBound);
	// Bson match = new BasicDBObject("$match", new BasicDBObject("year",
	// matchFields));
	//
	// DBObject groupFields = new BasicDBObject("_id", "$year");
	// groupFields.put("publications", new BasicDBObject("$sum", 1));
	// Bson group = new BasicDBObject("$group", groupFields);
	//
	// // TODO: I can't find out how the heck the Java driver uses aggregates
	// // since seriously it sucks
	//
	// // mongodb.getCollection('inproceedings').aggregate( [
	// // {$match: { year: {$gt:1980, $lt:2013}} },
	// // {$group: { _id: "$year", publications: {$sum: 1}}}
	// // ])
	//
	// MongoIterable<Document> doc =
	// col.aggregate(Arrays.asList(Aggregates.match(match),
	// Aggregates.project(group)));// .forEach(block);
	//
	// for (Document d : doc) {
	// System.out.println(d.toJson()); // for test purposes
	// }
	//
	// // repeat for proceedings
	// col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// // match and group fields stay the same
	// col.aggregate(Arrays.asList(Aggregates.match(match),
	// Aggregates.project(group))).forEach(block);
	//
	// // String inProcMap = "function(){emit(this." +
	// // Config.INPROCEEDINGS_YEAR + ", this._id);}";
	// // String inProcReduce = "function(year, inProcs){retrun
	// // Array.sum(inProcs);}";
	// // MapReduceIterable<Document> res = col.mapReduce(inProcMap,
	// // inProcReduce);
	// // for(Document doc: res){
	// // //This is utterly ugly but
	// // year = doc.getInteger(Config.INPROCEEDINGS_YEAR);
	// // if
	// // result+=doc.getDouble("value");
	// // nInPro++;
	// // }
	// return result;
	//
	// }
	//
	// // task 8
	// // @retrun: the key is the ConferenceID and the value the number of
	// // publications
	// public HashMap<String, Integer> noPublicationsPerConference() {
	// System.out.println("task 8: please look at the code. There is a bug I
	// have not been able to find. ");
	// // TODO: Same as for task 7
	// // db.getCollection('conferences').aggregate( [
	// // {$unwind : "$edition_keys"},
	// // {$group: { _id: "$_id", pubs: {$sum: 1}}}
	// // ])
	// HashMap<String, Integer> result = new HashMap<>();
	//
	// Block<Document> block = new Block<Document>() {
	// @Override
	// public void apply(final Document document) {
	// System.out.println(document.toJson());
	// String name = document.getString("_id");
	// int pubs = document.getInteger("publications");
	// result.put(name, pubs);
	// }
	// };
	// MongoCollection<Document> col =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	//
	// Bson unwind = new BasicDBObject("$unwind", "$" +
	// Config.CONFERENCE_EDITION_KEYS);
	//
	// DBObject groupFields = new BasicDBObject("_id", "$_id");
	// groupFields.put("publications", new BasicDBObject("$sum", 1));
	// Bson group = new BasicDBObject("$group", groupFields);
	//
	// col.aggregate(Arrays.asList(Aggregates.unwind(Config.CONFERENCE_EDITION_KEYS),
	// Aggregates.project(group)))
	// .forEach(block);
	//
	// return result;
	//
	// }
	//
	// // task 9
	// public int countEditors(String ConferenceId) {
	// Set<Integer> authors = new HashSet<>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	// Document doc = collection.find(eq("_id", ConferenceId)).first();
	// ArrayList<String> editions = (ArrayList<String>)
	// doc.get(Config.CONFERENCE_EDITION_KEYS);
	// ArrayList<String> proceedingKeys = new ArrayList<>();
	// for (String edition : editions) {
	// // It's ugly, but in comparison to the fancy functions above it
	// // works
	// collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
	// FindIterable<Document> conf_editions = collection.find(eq("_id",
	// edition));
	//
	// for (Document d : conf_editions) {
	// proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
	// }
	//
	// }
	// for (String key : proceedingKeys) {
	// collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// // This would do the job. A lookup would be even better
	// // db.getCollection('proceedings').aggregate( [
	// // {$unwind : "$inproceeding_keys"},
	// // {$group: { _id:"$inproceeding_keys"}}
	// // ])
	// Document procs = collection.find(eq("_id", key)).first();
	// List<String> inProcs = (List<String>)
	// procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
	// for (String inProc : inProcs) {
	// collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// Document inProceedings = collection.find(eq("_id", inProc)).first();
	// List<String> thisauthors = (List<String>)
	// inProceedings.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
	// for (String d : thisauthors) {
	// int authorID = Integer.parseInt(d);
	// if (!authors.contains(authorID))
	// authors.add(authorID);
	// }
	// }
	// }
	//
	// return authors.size();
	// }
	//
	// // task 10
	// public List<Person> allAuthorsOfConference(String ConferenceId) {
	// Set<Integer> authors = new HashSet<>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	// Document doc = collection.find(eq("_id", ConferenceId)).first();
	// if (doc == null) {
	// System.out.println("Well, this donsn't seem to be a valid Conference
	// id");
	// return null;
	// }
	// ArrayList<String> editions = (ArrayList<String>)
	// doc.get(Config.CONFERENCE_EDITION_KEYS);
	// ArrayList<String> proceedingKeys = new ArrayList<>();
	// for (String edition : editions) {
	// // It's ugly, but in comparison to the fancy functions above it
	// // works
	// collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
	// FindIterable<Document> conf_editions = collection.find(eq("_id",
	// edition));
	//
	// for (Document d : conf_editions) {
	// proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
	// }
	//
	// }
	// for (String key : proceedingKeys) {
	// collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	//
	// // This would do the job. A lookup would be even better
	// // db.getCollection('proceedings').aggregate( [
	// // {$unwind : "$inproceeding_keys"},
	// // {$group: { _id:"$inproceeding_keys"}}
	// // ])
	// Document procs = collection.find(eq("_id", key)).first();
	// List<String> editors = (List<String>)
	// procs.get(Config.PROCEEDINGS_EDITOR_KEYS);
	// for (String editor : editors) {
	// authors.add(Integer.parseInt(editor));
	// }
	// List<String> inProcs = (List<String>)
	// procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
	// for (String inProc : inProcs) {
	// collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// Document inProceedings = collection.find(eq("_id", inProc)).first();
	// List<String> thisauthors = (List<String>)
	// inProceedings.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
	// for (String d : thisauthors) {
	// int authorID = Integer.parseInt(d);
	// if (!authors.contains(authorID))
	// authors.add(authorID);
	// }
	// }
	// }
	// List<Person> authorsAndEditors = new ArrayList<>();
	// for (Integer autorID : authors) {
	// authorsAndEditors.add(getPersonById(Integer.toString(autorID)));
	// }
	// return authorsAndEditors;
	//
	// }
	//
	// // task 11
	// public List<InProceedings> allTasksFromPublication(String ConferenceId) {
	// Set<String> inProceedings = new HashSet<>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	// Document doc = collection.find(eq("_id", ConferenceId)).first();
	// if (doc == null) {
	// System.out.println("Well, this donsn't seem to be a valid Conference
	// id");
	// return null;
	// }
	// ArrayList<String> editions = (ArrayList<String>)
	// doc.get(Config.CONFERENCE_EDITION_KEYS);
	// ArrayList<String> proceedingKeys = new ArrayList<>();
	// for (String edition : editions) {
	// // It's ugly, but in comparison to the fancy functions above it
	// // works
	// collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
	// FindIterable<Document> conf_editions = collection.find(eq("_id",
	// edition));
	// for (Document d : conf_editions) {
	// proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
	// }
	//
	// }
	// for (String key : proceedingKeys) {
	// collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	//
	// // This would do the job. A lookup would be even better
	// // db.getCollection('proceedings').aggregate( [
	// // {$unwind : "$inproceeding_keys"},
	// // {$group: { _id:"$inproceeding_keys"}}
	// // ])
	// Document procs = collection.find(eq("_id", key)).first();
	// List<String> inProcs = (List<String>)
	// procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
	// for (String inProc : inProcs) {
	// inProceedings.add(inProc);
	// }
	// }
	// ArrayList<InProceedings> inprocs = new ArrayList<>();
	// for (String InProc : inProceedings) {
	// inprocs.add(getInProceedingsById(InProc));
	// }
	// return inprocs;
	//
	// }
	//
	// // George: How to query and return domain classes
	//
	// public Proceedings getProceedingsById(String id) {
	//
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// Document doc = collection.find(eq("_id", id)).first();
	//
	// if (doc != null) {
	// return makeProceedingsObject(doc);
	//
	// } else {
	//
	// return null;
	//
	// }
	//
	// }
	//
	// // George: Inserts authors/editors
	// // Note: to get authoredPublication, get all inProceedings where this
	// person
	// // is author
	// // Note: to get editedPublications, get all proceedings where this person
	// is
	// // editor/author
	// public void insertPeople(HashMap<Integer, Person> people) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PEOPLE_COLLECTION);
	//
	// for (Integer key : people.keySet()) {
	// Person person = people.get(key);
	// Document doc = new Document("_id",
	// key.toString()).append(Config.PEOPLE_NAME, person.getName());
	//
	// documents.add(doc);
	//
	// }
	//
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts conferences
	// public void insertConferences(HashMap<String, Conference> conferences) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	//
	// for (String key : conferences.keySet()) {
	// Conference conf = conferences.get(key);
	// Document doc = new Document("_id", key).append(Config.CONFERENCE_NAME,
	// conf.getName());
	//
	// // add Conference editions
	// ArrayList<String> edition_keys = new ArrayList<>();
	// for (ConferenceEdition edition : conf.getEditions()) {
	// edition_keys.add(edition.getId());
	// }
	//
	// doc.append(Config.CONFERENCE_EDITION_KEYS, edition_keys);
	// documents.add(doc);
	// }
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts conferenceEditions
	// public void insertConferenceEditions(HashMap<String, ConferenceEdition>
	// conferenceEditions) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
	//
	// for (String key : conferenceEditions.keySet()) {
	// ConferenceEdition confEd = conferenceEditions.get(key);
	// Document doc = new Document("_id",
	// key).append(Config.CONFERENCE_EDITION_YEAR, confEd.getYear())
	// .append(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY,
	// confEd.getProceedings().getId());
	//
	// documents.add(doc);
	// }
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts Publishers
	// // Note: to GetPublications of a publisher:
	// // get all proceedings for a publisher and all inproceedings in a
	// // proceedings
	// public void insertPublishers(HashMap<String, Publisher> publishers) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
	// for (String key : publishers.keySet()) {
	// Publisher publisher = publishers.get(key);
	// Document doc = new Document("_id", key).append(Config.PUBLISHER_NAME,
	// publisher.getName());
	// documents.add(doc);
	//
	// }
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts Series
	// // Note: to GetPublications of a series:
	// // get all proceedings for series and all inproceedings in a proceedings
	// public void insertSeries(HashMap<Integer, Series> seriesList) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.SERIES_COLLECTION);
	// for (Integer key : seriesList.keySet()) {
	// Series series = seriesList.get(key);
	// Document doc = new Document("_id",
	// key.toString()).append(Config.SERIES_NAME, series.getName());
	//
	// documents.add(doc);
	//
	// }
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts Proceedings
	// public void insertProceedings(HashMap<String, Proceedings>
	// proceedingsList) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// for (String key : proceedingsList.keySet()) {
	// Proceedings proceedings = proceedingsList.get(key);
	// Document doc = new Document("_id", key).append(Config.PROCEEDINGS_TITLE,
	// proceedings.getTitle())
	// .append(Config.PROCEEDINGS_YEAR, proceedings.getYear())
	// .append(Config.PROCEEDINGS_ISBN, proceedings.getIsbn())
	// .append(Config.PROCEEDINGS_VOLUME, proceedings.getVolume())
	// .append(Config.PROCEEDINGS_NOTE, proceedings.getNote())
	// .append(Config.PROCEEDINGS_ELECTRONIC_EDITION,
	// proceedings.getElectronicEdition())
	// .append(Config.PROCEEDINGS_NUMBER, proceedings.getNumber());
	//
	// // could be null
	// if (proceedings.getSeries() != null) {
	// doc.append(Config.PROCEEDINGS_SERIES_KEY,
	// proceedings.getSeries().getId());
	// }
	//
	// // could be null
	// if (proceedings.getConferenceEdition() != null) {
	// doc.append(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY,
	// proceedings.getConferenceEdition().getId());
	// }
	//
	// // could be null
	// if (proceedings.getPublisher() != null) {
	// doc.append(Config.PROCEEDINGS_PUBLISHER_KEY,
	// proceedings.getPublisher().getId());
	// }
	//
	// // add editors
	// ArrayList<String> editor_keys = new ArrayList<>();
	// for (Person editor : proceedings.getAuthors()) {
	// editor_keys.add(editor.getId());
	// }
	//
	// // add inproceedings
	// ArrayList<String> inProceeding_keys = new ArrayList<>();
	// for (InProceedings inProceeding : proceedings.getPublications()) {
	// inProceeding_keys.add(inProceeding.getId());
	// }
	//
	// doc.append(Config.PROCEEDINGS_EDITOR_KEYS, editor_keys);
	// doc.append(Config.PROCEEDINGS_INPROCEEDING_KEYS, inProceeding_keys);
	// documents.add(doc);
	//
	// }
	// collection.insertMany(documents);
	// }
	//
	// // George: Inserts inProceedings
	// public void insertInProceedings(HashMap<String, InProceedings>
	// inProceedingsList) {
	// List<Document> documents = new ArrayList<Document>();
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	//
	// for (String key : inProceedingsList.keySet()) {
	// InProceedings inProceedings = inProceedingsList.get(key);
	// Document doc = new Document("_id",
	// key).append(Config.INPROCEEDINGS_TITLE, inProceedings.getTitle())
	// .append(Config.INPROCEEDINGS_YEAR, inProceedings.getYear())
	// .append(Config.INPROCEEDINGS_NOTE, inProceedings.getNote())
	// .append(Config.INPROCEEDINGS_ELECTRONIC_EDITION,
	// inProceedings.getElectronicEdition())
	// .append(Config.INPROCEEDINGS_PAGES, inProceedings.getPages());
	//
	// // could be null
	// if (inProceedings.getProceedings() != null) {
	// doc.append(Config.INPROCEEDINGS_PROCEEDINGS_KEY,
	// inProceedings.getProceedings().getId());
	// }
	//
	// // add authors
	// ArrayList<String> author_keys = new ArrayList<>();
	// for (Person author : inProceedings.getAuthors()) {
	// author_keys.add(author.getId());
	// }
	// doc.append(Config.INPROCEEDINGS_AUTHOR_KEYS, author_keys);
	// documents.add(doc);
	// }
	//
	// collection.insertMany(documents);
	//
	// }
	//
	// public void removeInProceedings(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removeProceedings(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removePerson(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PEOPLE_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removeConference(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removeConferenceEdition(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removePublisher(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public void removeSeries(String id) {
	// MongoCollection<Document> collection =
	// mongoDB.getCollection(Config.SERIES_COLLECTION);
	// collection.deleteMany(eq("_id", id));
	// }
	//
	// public String getName() {
	// return dbName;
	// }
	//
	// // George: commits the imported XML data to ZooDB
	// public void importData(HashMap<String, Proceedings> proceedingsList,
	// HashMap<String, InProceedings> inProceedingsList, HashMap<Integer,
	// Series> seriesList,
	// HashMap<String, Publisher> publishers, HashMap<String, ConferenceEdition>
	// conferenceEditions,
	// HashMap<String, Conference> conferences, HashMap<Integer, Person> people)
	// {
	//
	// insertPeople(people);
	// insertConferences(conferences);
	// insertConferenceEditions(conferenceEditions);
	// insertPublishers(publishers);
	// insertSeries(seriesList);
	// insertProceedings(proceedingsList);
	// insertInProceedings(inProceedingsList);
	// }
	//
	// }
	//
	// class AuthorTree {
	// AuthorTree(int root) {
	// this.root = new AuthorTreeNode(root, 0);
	// }
	//
	// // public add
	// private AuthorTreeNode root;
	//
	// private class AuthorTreeNode {
	// AuthorTreeNode(int id, int dist) {
	// this.id = id;
	// this.distanceFromRoot = dist;
	// }
	//
	// public void setDistanceFromRoot(int dist) {
	// this.distanceFromRoot = dist;
	// }
	//
	// public int id;
	// public int distanceFromRoot;
	// }
}
