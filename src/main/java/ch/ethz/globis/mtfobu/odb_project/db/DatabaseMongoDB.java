package ch.ethz.globis.mtfobu.odb_project.db;

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

import org.bson.BSON;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.MongoNamespace;
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

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.ConferenceEdition;
import ch.ethz.globis.mtfobu.domains.DomainObject;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.domains.Series;
import ch.ethz.globis.mtfobu.odb_project.Config;
import ch.ethz.globis.mtfobu.odb_project.QueryParameters;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class DatabaseMongoDB implements Database {
	private static final String dbName = "dblpDB";
	private final MongoClient mongoClient;
	private static MongoDatabase mongoDB;
	public static boolean verbose = false;

	public DatabaseMongoDB() {
		// George: If dbName doesn't exist it will be created automatically
		// once documents are inserted
		mongoClient = new MongoClient();
		if (verbose) {
			System.out.println("Available MongoDB Databases:");
			for (String s : mongoClient.listDatabaseNames()) {
				System.out.println("- " + s);
			}
			System.out.println("Used database: " + dbName);
		}
		mongoDB = mongoClient.getDatabase(dbName);

	}

	@Override
	public boolean openDB() {
		mongoDB = mongoClient.getDatabase(dbName);
		return false;
	}

	@Override
	public void closeDB() {
		mongoClient.close();
		mongoDB = null;
	}

	// George: This will drop the database if already exists,
	// and create a new one automatically when documents are inserted
	public boolean create() {

		try {
			// This throws an exception if the name is not valid. It is used in
			// order to avoid dropping the database before
			MongoNamespace.checkDatabaseNameValidity(dbName);
			mongoDB.drop();
			mongoClient.getDatabase(dbName);
			mongoDB = mongoClient.getDatabase(dbName);
		} catch (IllegalArgumentException iae) {
			return false;
		}
		return true;
	}

	// George: get inProceedings by id
	public InProceedings getInProceedingsById(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();
		if (doc != null) {
			InProceedings inProc = new InProceedings(doc.getString("_id"));
			inProc.setTitle(doc.getString(Config.INPROCEEDINGS_TITLE));
			ArrayList<String> autors = (ArrayList<String>) doc.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
			List<Person> pers = new ArrayList<>(autors.size());
			for (String autor : autors) {
				pers.add(getPersonById(autor, true));
			}
			inProc.setAuthors(pers);
			int year = doc.getInteger(Config.INPROCEEDINGS_YEAR, Integer.MIN_VALUE);
			if (year > Integer.MIN_VALUE)
				inProc.setYear(year);

			return inProc;

		}
		return null;
	}

	@Override
	public Publication getPublicationById(String id) {
		Publication pub;
		pub = getInProceedingsById(id);
		if (pub == null)
			pub = getProceedingsById(id);
		return pub;
	}

	public String getPersonIdFromName(String name) {
		String id;
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);
		MongoCursor<Document> doc = collection.find(eq(Config.PEOPLE_NAME, name)).iterator();
		id = doc.next().getString("_id");
		if (doc.hasNext())
			System.out.println("Warning: multiple people share the same name");
		return id;
	}

	public String getConferenceIdFromName(String name) {
		String id;
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		MongoCursor<Document> doc = collection.find(eq(Config.CONFERENCE_COLLECTION, name)).iterator();
		id = doc.next().getString("_id");
		if (doc.hasNext())
			System.out.println("Warning: multiple conferences share the same name");
		return id;
	}

	// George: get publisher by id
	public Publisher getPublisherById(String series_id) {
		// TODO:implement
		return null;
	}

	// George: get publications for a series by series id
	public Set<Publication> getSeriesPublicationsById(String series_id) {
		// TODO:implement
		return null;
	}

	// George: get ConferenceEdition By ID
	public ConferenceEdition getConferenceEditionById(String id) {
		// TODO:implement
		return null;
	}

	// George: get Conference By ID
	public Conference getConferenceById(String id) {
		// TODO:implement
		return null;
	}

	// George: Get Series by ID
	public Series getSeriesById(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();

		if (doc != null) {
			String name = doc.get(Config.SERIES_NAME).toString();
			Set<Publication> publications = getSeriesPublicationsById(id);
			Series s = new Series(name);
			s.setPublications(publications);
			return s;

		} else {
			return null;
		}

	}

	// Helper class to construct a function for querying or searching with
	// paging or ranged accesses for any domain object
	public class QueryHelper<DO extends DomainObject> {
		private String collectionToUse;
		private String fieldToSearch;
		private Function<Document, DO> parser;

		public QueryHelper(String collectionToUse, String fieldToSearch, Function<Document, DO> parser) {
			this.collectionToUse = collectionToUse;
			this.fieldToSearch = fieldToSearch;
			this.parser = parser;
		}

		public Collection<DO> queryForDomainObject(QueryParameters p) {
			MongoCollection<Document> collection = mongoDB.getCollection(collectionToUse);
			MongoCursor<Document> cursor;

			if (p.isRanged) {
				long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
				long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;

				if (p.isSearch) {
					cursor = collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch)).skip((int) begin)
							.limit((int) (end - begin)).iterator();
				} else {
					cursor = collection.find().sort(ascending(fieldToSearch)).skip((int) begin)
							.limit((int) (end - begin)).iterator();
				}

			} else {

				if (p.isSearch) {
					cursor = collection.find(text(p.searchTerm)).sort(ascending(fieldToSearch))
							.skip((p.pageNumber - 1) * Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
				} else {
					cursor = collection.find().sort(ascending(fieldToSearch))
							.skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
				}

			}

			Collection<DO> coll = new ArrayList<DO>(Config.PAGE_SIZE);

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				coll.add(parser.apply(doc));
			}

			return coll;
		}
	}
	//task 2+3
	// Special case for the Publications Query
	public Collection<Publication> queryForPublications(QueryParameters p) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		MongoCursor<Document> cursor;

		if (p.isRanged) {
			long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
			long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;

			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE))
						.skip((int) begin).limit((int) (end - begin)).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE)).skip((int) begin)
						.limit((int) (end - begin)).iterator();
			}

		} else {

			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.INPROCEEDINGS_TITLE))
						.skip((p.pageNumber - 1) * Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.INPROCEEDINGS_TITLE))
						.skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
			}

		}

		List<Publication> list = new ArrayList<Publication>(2 * Config.PAGE_SIZE);

		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(makeInProceedingsObject(doc));
		}

		collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);

		if (p.isRanged) {
			long begin = p.rangeStart.isPresent() ? p.rangeStart.getAsLong() : 0;
			long end = p.rangeEnd.isPresent() ? p.rangeEnd.getAsLong() : Long.MAX_VALUE;

			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int) begin)
						.limit((int) (end - begin)).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.PROCEEDINGS_TITLE)).skip((int) begin)
						.limit((int) (end - begin)).iterator();
			}

		} else {

			if (p.isSearch) {
				cursor = collection.find(text(p.searchTerm)).sort(ascending(Config.PROCEEDINGS_TITLE))
						.skip((p.pageNumber - 1) * Config.PAGE_SIZE).limit(Config.PAGE_SIZE).iterator();
			} else {
				cursor = collection.find().sort(ascending(Config.PROCEEDINGS_TITLE))
						.skip((p.pageNumber - 1) * Config.PAGE_SIZE).iterator();
			}

		}

		while (cursor.hasNext()) {
			Document doc = cursor.next();
			list.add(makeProceedingsObject(doc));

		}

		// sorting the combined result list again, otherwise all InProceedings
		// are at the top
		list.sort(java.util.Comparator.comparing(publication -> publication.getTitle()));

		return list;
	}

	// Query Helper for Proceedings
	public QueryHelper<ConferenceEdition> conferenceEditionQueryHelper = new QueryHelper<ConferenceEdition>(
			Config.CONFERENCE_EDITION_COLLECTION, Config.CONFERENCE_EDITION_YEAR, this::makeConferenceEditionObject);
	public QueryHelper<Conference> conferenceQueryHelper = new QueryHelper<Conference>(Config.CONFERENCE_COLLECTION,
			Config.CONFERENCE_NAME, this::makeConferenceObject);
	public QueryHelper<InProceedings> inProceedingsQueryHelper = new QueryHelper<InProceedings>(
			Config.INPROCEEDINGS_COLLECTION, Config.INPROCEEDINGS_TITLE, this::makeInProceedingsObject);
	public QueryHelper<Person> personQueryHelper = new QueryHelper<Person>(Config.PEOPLE_COLLECTION, Config.PEOPLE_NAME,
			this::makePersonObject);
	public QueryHelper<Proceedings> proceedingsQueryHelper = new QueryHelper<Proceedings>(Config.PROCEEDINGS_COLLECTION,
			Config.PROCEEDINGS_TITLE, this::makeProceedingsObject);
	// No publication query handler, it's a bit of a special case
	public QueryHelper<Publisher> publisherQueryHelper = new QueryHelper<Publisher>(Config.PUBLISHER_COLLECTION,
			Config.PUBLISHER_NAME, this::makePublisherObject);
	public QueryHelper<Series> seriesQueryHelper = new QueryHelper<Series>(Config.SERIES_COLLECTION, Config.SERIES_NAME,
			this::makeSeriesObject);

	private ConferenceEdition makeConferenceEditionObject(Document doc) {
		// TODO:implement
		return null;
	}

	private Conference makeConferenceObject(Document doc) {
		// TODO:implement
		return null;
	}

	private InProceedings makeInProceedingsObject(Document doc) {
		// TODO:implement
		return null;
	}

	private Person makePersonObject(Document doc) {
		// TODO:implement
		return null;
	}

	private Proceedings makeProceedingsObject(Document doc) {

		Proceedings proceedings = new Proceedings((String) doc.get(Config.MONGODB_PRIMARY_KEY));

		String title = (String) doc.get(Config.PROCEEDINGS_TITLE);
		int year = (int) doc.get(Config.PROCEEDINGS_YEAR);
		String isbn = (String) doc.get(Config.PROCEEDINGS_ISBN);
		String volume = (String) doc.get(Config.PROCEEDINGS_VOLUME);
		String note = (String) doc.get(Config.PROCEEDINGS_NOTE);
		String ee = (String) doc.get(Config.PROCEEDINGS_ELECTRONIC_EDITION);
		int number = (int) doc.get(Config.PROCEEDINGS_NUMBER);

		proceedings.setTitle(title);
		proceedings.setYear(year);
		proceedings.setIsbn(isbn);
		proceedings.setVolume(volume);
		proceedings.setNote(note);
		proceedings.setElectronicEdition(ee);
		proceedings.setNumber(number);

		String series_key = (String) doc.get(Config.PROCEEDINGS_SERIES_KEY);
		Series series = getSeriesById(series_key);
		proceedings.setSeries(series);

		String conf_ed_key = (String) doc.get(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY);
		ConferenceEdition conferenceEdition = getConferenceEditionById(conf_ed_key);
		proceedings.setConferenceEdition(conferenceEdition);

		String publisher_key = (String) doc.get(Config.PROCEEDINGS_PUBLISHER_KEY);
		Publisher publisher = getPublisherById(publisher_key);
		proceedings.setPublisher(publisher);

		ArrayList<String> editor_keys = (ArrayList<String>) doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
		ArrayList<Person> authors = new ArrayList<>();
		for (String key : editor_keys) {
			Person p = getPersonById(key, true);
			authors.add(p);
		}
		proceedings.setAuthors(authors);

		ArrayList<String> inproceedings_keys = (ArrayList<String>) doc.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
		HashSet<InProceedings> inproceedingsList = new HashSet<>();
		for (String key : inproceedings_keys) {
			InProceedings inProceedings = getInProceedingsById(key);
			inproceedingsList.add(inProceedings);
		}
		proceedings.setPublications(inproceedingsList);

		return proceedings;
	}

	private Publisher makePublisherObject(Document doc) {
		// TODO:implement
		return null;
	}

	private Series makeSeriesObject(Document doc) {
		// TODO:implement
		return null;
	}

	// task 5
	public int authorDistance(String authorIdA, String authorIdB) {
		if (authorIdA == authorIdB)
			return 0;
		HashMap<Integer, Integer> doneSet = new HashMap<>();
		HashMap<Integer, Integer> remainingSet = new HashMap<>();

		int currentAuthor = Integer.parseInt(authorIdA);
		doneSet.put(currentAuthor, 0);

		HashSet<Integer> coAuthors = getCoAuthoresIds(Integer.toString(currentAuthor));
		for (Integer i : coAuthors) {
			remainingSet.put(i, 1);
		}

		while (!remainingSet.isEmpty()) {
			// get nearest author
			int currentdist = Integer.MAX_VALUE;
			for (Integer i : remainingSet.keySet()) {
				if (remainingSet.get(i) < currentdist) {
					currentAuthor = i;
					currentdist = remainingSet.get(i);
				}
			}
			if (currentAuthor != Integer.parseInt(authorIdB)) {
				coAuthors = getCoAuthoresIds(Integer.toString(currentAuthor));
				doneSet.put(currentAuthor, currentdist);
				remainingSet.remove(currentAuthor);
				for (Integer id : coAuthors) {
					if (doneSet.containsKey(id))
						continue;
					if (remainingSet.containsKey(id)) {
						// path is shorter than a previously known one
						if (remainingSet.get(id) > doneSet.get(currentAuthor) + 1) {
							remainingSet.replace(id, doneSet.get(currentAuthor) + 1);
						}
						// path is longer
						else {
							// discard
						}
					} else {
						remainingSet.put(id, doneSet.get(currentAuthor) + 1);
					}
				}
			} else
				return currentdist;

		}
		System.out.println("Task 5: author distance couldn't be determined. Author not found!");
		return 0;
	}

	// task 5 helper function
	private HashSet<Integer> getCoAuthoresIds(String authorId) {
		MongoCursor<Document> cursor;
		HashSet<Integer> foundCoAu = new HashSet<>();
		MongoCollection<Document> col;

		col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		cursor = col.find(eq(Config.INPROCEEDINGS_AUTHOR_KEYS, authorId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			@SuppressWarnings("unchecked")
			ArrayList<String> ids = (ArrayList<String>) doc.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
			for (String i : ids) {
				int n = Integer.parseInt(i);
				if (!foundCoAu.contains(n)) {
					foundCoAu.add(n);
				}
			}

		}
		col = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		cursor = col.find(eq(Config.PROCEEDINGS_EDITOR_KEYS, authorId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			@SuppressWarnings("unchecked")
			ArrayList<String> ids = (ArrayList<String>) doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
			for (String i : ids) {
				int n = Integer.parseInt(i);
				if (!foundCoAu.contains(n)) {
					foundCoAu.add(n);
				}
			}

		}
		foundCoAu.remove(Integer.parseInt(authorId));

		return foundCoAu;

	}

	// task 7
	// @retrun: The hashmap takes as key the year and returns the number of
	// publications as value
	// @param: the bounds are not inclusive
	public HashMap<Integer, Integer> noPublicationsPerYear(int yearLowerBound, int yearUpperBound) {
		System.out.println("task 7: please look at the code. There is a bug I have not been able to find. ");
		HashMap<Integer, Integer> result = new HashMap<>();

		Block<Document> block = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				System.out.println(document.toJson());
				int year = document.getInteger("year");
				int pubs = document.getInteger("publications");
				result.put(year, pubs);
			}
		};
		MongoCollection<Document> col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		DBObject matchFields = new BasicDBObject("$gt", yearLowerBound);
		matchFields.put("$lt", yearUpperBound);
		Bson match = new BasicDBObject("$match", new BasicDBObject("year", matchFields));

		DBObject groupFields = new BasicDBObject("_id", "$year");
		groupFields.put("publications", new BasicDBObject("$sum", 1));
		Bson group = new BasicDBObject("$group", groupFields);

		// TODO: I can't find out how the heck the Java driver uses aggregates
		// since seriously it sucks

		// mongodb.getCollection('inproceedings').aggregate( [
		// {$match: { year: {$gt:1980, $lt:2013}} },
		// {$group: { _id: "$year", publications: {$sum: 1}}}
		// ])

		MongoIterable<Document> doc = col.aggregate(Arrays.asList(Aggregates.match(match), Aggregates.project(group)));// .forEach(block);

		for (Document d : doc) {
			System.out.println(d.toJson()); // for test purposes
		}

		// repeat for proceedings
		col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		// match and group fields stay the same
		col.aggregate(Arrays.asList(Aggregates.match(match), Aggregates.project(group))).forEach(block);

		// String inProcMap = "function(){emit(this." +
		// Config.INPROCEEDINGS_YEAR + ", this._id);}";
		// String inProcReduce = "function(year, inProcs){retrun
		// Array.sum(inProcs);}";
		// MapReduceIterable<Document> res = col.mapReduce(inProcMap,
		// inProcReduce);
		// for(Document doc: res){
		// //This is utterly ugly but
		// year = doc.getInteger(Config.INPROCEEDINGS_YEAR);
		// if
		// result+=doc.getDouble("value");
		// nInPro++;
		// }
		return result;

	}

	// George: How to query and return domain classes

	public Proceedings getProceedingsById(String id) {

		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();

		if (doc != null) {
			return makeProceedingsObject(doc);

		} else {

			return null;

		}

	}

	// George: Inserts authors/editors
	// Note: to get authoredPublication, get all inProceedings where this person
	// is author
	// Note: to get editedPublications, get all proceedings where this person is
	// editor/author
	public void insertPeople(HashMap<Integer, Person> people) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);

		for (Integer key : people.keySet()) {
			Person person = people.get(key);
			Document doc = new Document("_id", key.toString()).append(Config.PEOPLE_NAME, person.getName());

			documents.add(doc);

		}

		collection.insertMany(documents);
	}

	// George: Inserts conferences
	public void insertConferences(HashMap<String, Conference> conferences) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);

		for (String key : conferences.keySet()) {
			Conference conf = conferences.get(key);
			Document doc = new Document("_id", key).append(Config.CONFERENCE_NAME, conf.getName());

			// add Conference editions
			ArrayList<String> edition_keys = new ArrayList<>();
			for (ConferenceEdition edition : conf.getEditions()) {
				edition_keys.add(edition.getId());
			}

			doc.append(Config.CONFERENCE_EDITION_KEYS, edition_keys);
			documents.add(doc);
		}
		collection.insertMany(documents);
	}

	// George: Inserts conferenceEditions
	public void insertConferenceEditions(HashMap<String, ConferenceEdition> conferenceEditions) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);

		for (String key : conferenceEditions.keySet()) {
			ConferenceEdition confEd = conferenceEditions.get(key);
			Document doc = new Document("_id", key).append(Config.CONFERENCE_EDITION_YEAR, confEd.getYear())
					.append(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY, confEd.getProceedings().getId());

			documents.add(doc);
		}
		collection.insertMany(documents);
	}

	// George: Inserts Publishers
	// Note: to GetPublications of a publisher:
	// get all proceedings for a publisher and all inproceedings in a
	// proceedings
	public void insertPublishers(HashMap<String, Publisher> publishers) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		for (String key : publishers.keySet()) {
			Publisher publisher = publishers.get(key);
			Document doc = new Document("_id", key).append(Config.PUBLISHER_NAME, publisher.getName());
			documents.add(doc);

		}
		collection.insertMany(documents);
	}

	// George: Inserts Series
	// Note: to GetPublications of a series:
	// get all proceedings for series and all inproceedings in a proceedings
	public void insertSeries(HashMap<Integer, Series> seriesList) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		for (Integer key : seriesList.keySet()) {
			Series series = seriesList.get(key);
			Document doc = new Document("_id", key.toString()).append(Config.SERIES_NAME, series.getName());

			documents.add(doc);

		}
		collection.insertMany(documents);
	}

	// George: Inserts Proceedings
	public void insertProceedings(HashMap<String, Proceedings> proceedingsList) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		for (String key : proceedingsList.keySet()) {
			Proceedings proceedings = proceedingsList.get(key);
			Document doc = new Document("_id", key).append(Config.PROCEEDINGS_TITLE, proceedings.getTitle())
					.append(Config.PROCEEDINGS_YEAR, proceedings.getYear())
					.append(Config.PROCEEDINGS_ISBN, proceedings.getIsbn())
					.append(Config.PROCEEDINGS_VOLUME, proceedings.getVolume())
					.append(Config.PROCEEDINGS_NOTE, proceedings.getNote())
					.append(Config.PROCEEDINGS_ELECTRONIC_EDITION, proceedings.getElectronicEdition())
					.append(Config.PROCEEDINGS_NUMBER, proceedings.getNumber());

			// could be null
			if (proceedings.getSeries() != null) {
				doc.append(Config.PROCEEDINGS_SERIES_KEY, proceedings.getSeries().getId());
			}

			// could be null
			if (proceedings.getConferenceEdition() != null) {
				doc.append(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY, proceedings.getConferenceEdition().getId());
			}

			// could be null
			if (proceedings.getPublisher() != null) {
				doc.append(Config.PROCEEDINGS_PUBLISHER_KEY, proceedings.getPublisher().getId());
			}

			// add editors
			ArrayList<String> editor_keys = new ArrayList<>();
			for (Person editor : proceedings.getAuthors()) {
				editor_keys.add(editor.getId());
			}

			// add inproceedings
			ArrayList<String> inProceeding_keys = new ArrayList<>();
			for (InProceedings inProceeding : proceedings.getPublications()) {
				inProceeding_keys.add(inProceeding.getId());
			}

			doc.append(Config.PROCEEDINGS_EDITOR_KEYS, editor_keys);
			doc.append(Config.PROCEEDINGS_INPROCEEDING_KEYS, inProceeding_keys);
			documents.add(doc);

		}
		collection.insertMany(documents);
	}

	// George: Inserts inProceedings
	public void insertInProceedings(HashMap<String, InProceedings> inProceedingsList) {
		List<Document> documents = new ArrayList<Document>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);

		for (String key : inProceedingsList.keySet()) {
			InProceedings inProceedings = inProceedingsList.get(key);
			Document doc = new Document("_id", key).append(Config.INPROCEEDINGS_TITLE, inProceedings.getTitle())
					.append(Config.INPROCEEDINGS_YEAR, inProceedings.getYear())
					.append(Config.INPROCEEDINGS_NOTE, inProceedings.getNote())
					.append(Config.INPROCEEDINGS_ELECTRONIC_EDITION, inProceedings.getElectronicEdition())
					.append(Config.INPROCEEDINGS_PAGES, inProceedings.getPages());

			// could be null
			if (inProceedings.getProceedings() != null) {
				doc.append(Config.INPROCEEDINGS_PROCEEDINGS_KEY, inProceedings.getProceedings().getId());
			}

			// add authors
			ArrayList<String> author_keys = new ArrayList<>();
			for (Person author : inProceedings.getAuthors()) {
				author_keys.add(author.getId());
			}
			doc.append(Config.INPROCEEDINGS_AUTHOR_KEYS, author_keys);
			documents.add(doc);
		}

		collection.insertMany(documents);

	}

	public void removeProceedings(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public void removePerson(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public void removeConference(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public void removeConferenceEdition(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public void removePublisher(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public void removeSeries(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.SERIES_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	public String getName() {
		return dbName;
	}

	// George: commits the imported XML data to ZooDB
	public void importData(HashMap<String, Proceedings> proceedingsList,
			HashMap<String, InProceedings> inProceedingsList, HashMap<Integer, Series> seriesList,
			HashMap<String, Publisher> publishers, HashMap<String, ConferenceEdition> conferenceEditions,
			HashMap<String, Conference> conferences, HashMap<Integer, Person> people) {

		insertPeople(people);
		insertConferences(conferences);
		insertConferenceEditions(conferenceEditions);
		insertPublishers(publishers);
		insertSeries(seriesList);
		insertProceedings(proceedingsList);
		insertInProceedings(inProceedingsList);
	}

	@Override
	public List<Publisher> getPublishers() {
		List<Publisher> pubs = new ArrayList<>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		MongoCursor<Document> iterator = collection.find().iterator();
		while (iterator.hasNext()) {
			pubs.add(publisherFromDoc(iterator.next()));
		}
		return pubs;
	}

	public Publisher publisherFromDoc(Document doc) {
		Publisher pub = new Publisher(doc.getString(Config.PUBLISHER_NAME));
		return pub;
	}

	@Override
	public List<Publication> getPublications() {
		@SuppressWarnings("unchecked")
		List<Publication> pubs = (List<Publication>) (List<?>) getProceedings();
		pubs.addAll((List<Publication>) (List<?>) getInProceedings());
		return pubs;
	}

	@Override
	public List<Proceedings> getProceedings() {
		List<Proceedings> procs = new ArrayList<>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		MongoCursor<Document> iterator = collection.find().iterator();
		while (iterator.hasNext()) {
			procs.add(proceedingFromDoc(iterator.next()));
		}
		return procs;
	}

	public Proceedings proceedingFromDoc(Document doc) {
		Proceedings proc = new Proceedings(doc.getString("_id"));
		proc.setTitle(doc.getString(Config.PROCEEDINGS_TITLE));
		proc.setYear(doc.getInteger(Config.PROCEEDINGS_YEAR));
		{
			String seriesKey = doc.getString(Config.PROCEEDINGS_SERIES_KEY);
			if (seriesKey != null) {
				// TODO: Verify
				// It is assumed that seriesKey represents the id of the series
				proc.setSeries(getSeriesById(seriesKey));
			}
		}
		proc.setIsbn(doc.getString(Config.PROCEEDINGS_ISBN));
		{
			ArrayList<String> editors = (ArrayList<String>) doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
			List<Person> people = new ArrayList<>();
			int numEdit = editors.size();
			for (int i = 0; i < numEdit; ++i) {
				people.add(getPersonById(editors.get(i), true));
			}
			proc.setAuthors(people);
		}

		{
			// TODO: verify this. It seems that the conference name is set by
			// this tag instead of <booktitle>
			String confName = doc.getString(Config.PROCEEDINGS_CONFERENCE_EDITION_KEY);
			if (confName != null) {
				Conference conf = new Conference(confName);
				proc.setConference(conf);
			}
		}
		proc.setPublisher(getPublisherByName(doc.getString(Config.PROCEEDINGS_PUBLISHER_KEY)));
		{
			ArrayList<String> inProceedingIDs = (ArrayList<String>) doc.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
			Set<InProceedings> inProcs = new HashSet<>();
			int numInProcs = inProceedingIDs.size();
			for (int i = 0; i < numInProcs; ++i) {
				inProcs.add(new InProceedings(inProceedingIDs.get(i)));
			}
			proc.setPublications(inProcs);
		}
		proc.setVolume(doc.getString(Config.PROCEEDINGS_VOLUME));
		proc.setNote(doc.getString(Config.PROCEEDINGS_NOTE));
		proc.setElectronicEdition(doc.getString(Config.PROCEEDINGS_ELECTRONIC_EDITION));
		proc.setNumber(doc.getInteger(Config.PROCEEDINGS_NUMBER));
		return proc;
	}

	public Set<InProceedings> getInProcForProceedingId(String procId) {
		// TODO: implement
		return null;
	}

	@Override
	public List<InProceedings> getInProceedings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Person> getPeople() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Conference> getConferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Series> getSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ConferenceEdition> getConferenceEditions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importData(HashMap<String, Proceedings> proceedingsList,
			HashMap<String, InProceedings> inProceedingsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public Proceedings getProceedingById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Publisher getPublisherByName(String name) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PUBLISHER_COLLECTION);
		Document doc = collection.find(eq(Config.PUBLISHER_NAME, name)).first();
		//It is assumed the id field is set with the name
		Publisher pub = new Publisher(name);
		

		return pub;
	}

	@Override
	public Person getPersonById(String id, boolean lazy) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.PEOPLE_COLLECTION);
		Document doc = collection.find(eq("_id", id)).first();

		if (doc != null) {
			String name = doc.get(Config.PEOPLE_NAME).toString();
			Person p = new Person(name);
			return p;

		} else {
			return null;
		}
	}

	@Override
	public Set<Publication> getAuthoredPublications(String personName, boolean lazy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Publication> getEditedPublications(String personName, boolean lazy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Conference getConferenceByName(String confName, boolean lazy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ConferenceEdition> getConfEditionsForConf(Conference conference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Series getSeriesByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addProceeding(Proceedings proc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteProceedingById(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateProceeding(Proceedings proc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addInProceeding(InProceedings inProc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteInProceedingById(String id) {
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		collection.deleteMany(eq("_id", id));
	}

	@Override
	public List<Publication> getPublicationsByFilter(String title, int begin_offset, int end_offset) {
		// TODO Auto-generated method stub
		return null;
	}

	// task 4
	@Override
	public List<Person> getCoAuthors(String name) {
		// Assuming AuthorID = AuthorName
		String authorId = name;
		MongoCursor<Document> cursor;
		Set<String> foundCoAu = new HashSet<>();
		MongoCollection<Document> col;

		col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		cursor = col.find(eq(Config.INPROCEEDINGS_AUTHOR_KEYS, authorId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			@SuppressWarnings("unchecked")
			ArrayList<String> ids = (ArrayList<String>) doc.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
			for (String i : ids) {
				if (!foundCoAu.contains(i)) {
					foundCoAu.add(i);
				}
			}

		}
		col = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
		cursor = col.find(eq(Config.PROCEEDINGS_EDITOR_KEYS, authorId)).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			@SuppressWarnings("unchecked")
			ArrayList<String> ids = (ArrayList<String>) doc.get(Config.PROCEEDINGS_EDITOR_KEYS);
			for (String i : ids) {
				if (!foundCoAu.contains(i)) {
					foundCoAu.add(i);
				}
			}

		}
		foundCoAu.remove(authorId);
		ArrayList<Person> coAuthors = new ArrayList<>(foundCoAu.size());

		for (String i : foundCoAu) {
			coAuthors.add(getPersonById(i, false));
		}

		return coAuthors;
	}

	// task 6
	// TODO: Seems to work, but verify.
	@Override
	public double getAvgAuthorsInProceedings() {
		double result = 0;
		int nInPro = 0;
		MongoCollection<Document> col = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		String inProcMap = "function(){emit(this._id, " + "this." + Config.INPROCEEDINGS_AUTHOR_KEYS + ".length );}";
		String inProcReduce = "function(key, authors){return Array.sum(authors);}"; // Array.sum(authors)
		MapReduceIterable<Document> res = col.mapReduce(inProcMap, inProcReduce);
		for (Document doc : res) {
			result += doc.getDouble("value");
			nInPro++;
		}
		result /= (double) nInPro;

		return result;
	}

	@Override
	public List<String> getNumberPublicationsPerYearInterval(int yearLowerBound, int yearUpperBound) {
		// TODO Auto-generated method stub
		return null;
	}

	// task 8
	// TODO: This function does not what is asked by the task. Solve the issue
	@Override
	public int getNumberOfPublicationsPerConferenceByName(String conferenceName) {
		// task 8
		// @retrun: the key is the ConferenceID and the value the number of
		// publications
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
		return 0;
	}

	// task 9
	@Override
	public int countEditorsAndAuthorsOfConferenceByName(String conferenceId) {

		Set<Integer> authors = new HashSet<>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		Document doc = collection.find(eq("_id", conferenceId)).first();
		ArrayList<String> editions = (ArrayList<String>) doc.get(Config.CONFERENCE_EDITION_KEYS);
		ArrayList<String> proceedingKeys = new ArrayList<>();
		for (String edition : editions) {
			// It's ugly, but in comparison to the fancy functions above it
			// works
			collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
			FindIterable<Document> conf_editions = collection.find(eq("_id", edition));

			for (Document d : conf_editions) {
				proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
			}

		}
		for (String key : proceedingKeys) {
			collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);
			// This would do the job. A lookup would be even better
			// db.getCollection('proceedings').aggregate( [
			// {$unwind : "$inproceeding_keys"},
			// {$group: { _id:"$inproceeding_keys"}}
			// ])
			Document procs = collection.find(eq("_id", key)).first();
			List<String> inProcs = (List<String>) procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
			for (String inProc : inProcs) {
				collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
				Document inProceedings = collection.find(eq("_id", inProc)).first();
				List<String> thisauthors = (List<String>) inProceedings.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
				for (String d : thisauthors) {
					int authorID = Integer.parseInt(d);
					if (!authors.contains(authorID))
						authors.add(authorID);
				}
			}
		}

		return authors.size();
	}

	// task 10
	@Override
	public List<Person> getAllAuthorsOfConferenceByName(String conferenceId) {

		Set<Integer> authors = new HashSet<>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		Document doc = collection.find(eq("_id", conferenceId)).first();
		if (doc == null) {
			System.out.println("Well, this donsn't seem to be a valid Conference id");
			return null;
		}
		ArrayList<String> editions = (ArrayList<String>) doc.get(Config.CONFERENCE_EDITION_KEYS);
		ArrayList<String> proceedingKeys = new ArrayList<>();
		for (String edition : editions) {
			// It's ugly, but in comparison to the fancy functions above it
			// works
			collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
			FindIterable<Document> conf_editions = collection.find(eq("_id", edition));

			for (Document d : conf_editions) {
				proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
			}

		}
		for (String key : proceedingKeys) {
			collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);

			// This would do the job. A lookup would be even better
			// db.getCollection('proceedings').aggregate( [
			// {$unwind : "$inproceeding_keys"},
			// {$group: { _id:"$inproceeding_keys"}}
			// ])
			Document procs = collection.find(eq("_id", key)).first();
			List<String> editors = (List<String>) procs.get(Config.PROCEEDINGS_EDITOR_KEYS);
			for (String editor : editors) {
				authors.add(Integer.parseInt(editor));
			}
			List<String> inProcs = (List<String>) procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
			for (String inProc : inProcs) {
				collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
				Document inProceedings = collection.find(eq("_id", inProc)).first();
				List<String> thisauthors = (List<String>) inProceedings.get(Config.INPROCEEDINGS_AUTHOR_KEYS);
				for (String d : thisauthors) {
					int authorID = Integer.parseInt(d);
					if (!authors.contains(authorID))
						authors.add(authorID);
				}
			}
		}
		List<Person> authorsAndEditors = new ArrayList<>();
		for (Integer autorID : authors) {
			authorsAndEditors.add(getPersonById(Integer.toString(autorID), false));
		}
		return authorsAndEditors;

	}

	// task 11
	@Override
	public List<Publication> getAllPublicationsOfConferenceByName(String conferenceId) {
		Set<String> inProceedings = new HashSet<>();
		MongoCollection<Document> collection = mongoDB.getCollection(Config.CONFERENCE_COLLECTION);
		Document doc = collection.find(eq("_id", conferenceId)).first();
		if (doc == null) {
			System.out.println("Well, this donsn't seem to be a valid Conference id");
			return null;
		}
		ArrayList<String> editions = (ArrayList<String>) doc.get(Config.CONFERENCE_EDITION_KEYS);
		ArrayList<String> proceedingKeys = new ArrayList<>();
		for (String edition : editions) {
			// It's ugly, but in comparison to the fancy functions above it
			// works
			collection = mongoDB.getCollection(Config.CONFERENCE_EDITION_COLLECTION);
			FindIterable<Document> conf_editions = collection.find(eq("_id", edition));
			for (Document d : conf_editions) {
				proceedingKeys.add(d.getString(Config.CONFERENCE_EDITION_PROCEEDINGS_KEY));
			}

		}
		for (String key : proceedingKeys) {
			collection = mongoDB.getCollection(Config.PROCEEDINGS_COLLECTION);

			// This would do the job. A lookup would be even better
			// db.getCollection('proceedings').aggregate( [
			// {$unwind : "$inproceeding_keys"},
			// {$group: { _id:"$inproceeding_keys"}}
			// ])
			Document procs = collection.find(eq("_id", key)).first();
			List<String> inProcs = (List<String>) procs.get(Config.PROCEEDINGS_INPROCEEDING_KEYS);
			for (String inProc : inProcs) {
				inProceedings.add(inProc);
			}
		}
		ArrayList<Publication> pubs = new ArrayList<>();
		for (String InProc : inProceedings) {
			pubs.add(getInProceedingsById(InProc));
		}
		return pubs;
	}

	@Override
	public List<Person> getPeopleThatAreAuthorsAndEditors() {
		// TODO Auto-generated method stub
		return null;
	}

	// task 13
	@Override
	public List<InProceedings> getPublicationsWhereAuthorIsLast(String authorName) {
		// Assume author id is equal to author name
		String id = authorName;
		/*
		 * db.getCollection('inproceedings').aggregate([ { $project: {
		 * author_keys: { $slice: [ "$author_keys", -1 ] } } }, { $match: {
		 * author_keys: "1785178126" } } ])
		 */
		MongoCollection<Document> collection = mongoDB.getCollection(Config.INPROCEEDINGS_COLLECTION);
		ArrayList<InProceedings> results = new ArrayList<>();

		Block<Document> block = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				System.out.println(document.toJson());
				String id = (String) document.get("_id");
				InProceedings inProceedings = getInProceedingsById(id);
				if (inProceedings != null)
					results.add(inProceedings);
			}
		};
		BasicDBList author_keys = new BasicDBList();
		author_keys.add("$author_keys");
		author_keys.add(-1);

		collection.aggregate(Arrays.asList(
				Aggregates.project(
						Projections.fields(new BasicDBObject("author_keys", new BasicDBObject("$slice", author_keys)))),
				Aggregates.match(eq("author_keys", id)))).forEach(block);

		return results;
	}

	@Override
	public List<Publisher> task14(int yearLowerBound, int yearUpperBound) {
		// TODO Auto-generated method stub
		return null;
	}

}

class AuthorTree {
	AuthorTree(int root) {
		this.root = new AuthorTreeNode(root, 0);
	}

	// public add
	private AuthorTreeNode root;

	private class AuthorTreeNode {
		AuthorTreeNode(int id, int dist) {
			this.id = id;
			this.distanceFromRoot = dist;
		}

		public void setDistanceFromRoot(int dist) {
			this.distanceFromRoot = dist;
		}

		public int id;
		public int distanceFromRoot;
	}
}

