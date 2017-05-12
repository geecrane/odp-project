package ch.ethz.globis.mtfobu.odb_project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

//This is our version
//import ch.ethz.globis.mtfobu.odb_project.BaseXClient.Query;
//This version is used if the BaseX Server is also executed by the program. 
// Since I was not able to get a proper description and because the previous 
// version was linked on the task sheet, I used the previous one.

//import org.basex.api.client.Query;

//George: Instatiate db  only once!
//Otherwise, creates a new server every time!
public class DatabaseBaseX {
    private final String dbName;
    private ClientSession session = null;
    final String defaultHost = "localhost";
    final int defaultPort = 1984;
    final String defaultUsername = "admin";
    final String defaultPassword = "admin";
    final String rootDocumentName = "dblp_filtered.xml";

    private static class Singleton {
	private static final DatabaseBaseX instance = new DatabaseBaseX(Config.DATABASE_NAME);
    }

    /**
     * Used to retrieve the database instance. DO NOT try to instantiate the
     * Database class manually!
     * 
     * @return The Database instance
     */
    public static DatabaseBaseX getDatabase() {
	return Singleton.instance;
    }

    private DatabaseBaseX(String dbName) {

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
     * Close DB
     */
    public void closeDB() {
	try {
	    session.close();
	} catch (IOException e) {
	    System.err.println("Could not close DB");
	    e.printStackTrace();
	}
    }

    // George: get inProceedings by id
    public InProceedings getInProceedingsById(String id) {

	String InProcByIDQuery = String.format("//inproceedings[@key='%s']", id);
	try {
	    ClientQuery query = session.query(InProcByIDQuery);
	    if (query.more()) {
		String queryResult = query.next();
		return getInProceedingsObject(queryResult);
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	return null;
    }

    private InProceedings getInProceedingsObject(String queryResult) throws UnsupportedEncodingException, IOException {
	// The result is in XML format therefore JDOM is used in order
	// to parse it

	SAXBuilder builder = new SAXBuilder();
	InputStream stream = new ByteArrayInputStream(queryResult.getBytes("UTF-8"));
	try {
	    Document inprocXML = builder.build(stream);
	    Element rootNode = inprocXML.getRootElement();
	    InProceedings inProc = new InProceedings(rootNode.getAttributeValue("key"));
	    inProc.setTitle(rootNode.getChildText("title"));
	    inProc.setYear(Integer.parseInt(rootNode.getChildText("year")));
	    List<Element> authors = rootNode.getChildren("author");
	    List<Person> authorList = new ArrayList<>();
	    for (Element author : authors)
		authorList.add(new Person(author.getText()));

	    inProc.setAuthors(authorList);
	    inProc.setNote(rootNode.getChildText("note"));
	    inProc.setPages(rootNode.getChildText("pages"));
	    inProc.setProceedings(getProceedingById(rootNode.getChildText("crossref")));
	    return inProc;
	} catch (JDOMException e) {
	    System.out.println("Error: The query result was not in the expected XML format");
	    e.printStackTrace();
	    return null;
	}
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
		return XmlToObject.XmlToProceeding(queryResult, null, this, false);

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
		return XmlToObject.XmlToPublication(queryResult, null, this, false);

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
    /**
     * 
     * @param name
     *            Publisher name
     * @return a partially initialized publisher Object.
     */
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
		pubs.add(XmlToObject.XmlToPublication(publication, pub, this, true));
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
		pubs.add(XmlToObject.XmlToPublication(queryResult, null, this, false));
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
		coAuthors.add(getPersonByName(coAutherName, true));
		System.out.println("Found co-auther: " + coAutherName);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return coAuthors;
    }

    // Helper function task 4
    public Person getPersonByName(String name, boolean lazy) {
	// TODO: Verify this assumption
	return getPersonById(name, lazy);
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
    public double getAvgAuthorsInProceedings() {
	String avgAuthorsInProceedingsQuery = "avg(for $inProc in //inproceedings return count($inProc/author))";
	ClientQuery query;
	try {
	    query = session.query(avgAuthorsInProceedingsQuery);
	    assert (query.more());
	    return Double.parseDouble(query.next());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return Double.NaN;

    }

    // TASK 7:
    public List<String> getNumberPublicationsPerYearInterval(int yearLowerBound, int yearUpperBound) {
	List<String> result = new ArrayList<>();
	String numberOfPublicationsPerYearIntervalQuery = String.format(
		"declare function local:buildInterval($lowB as xs:integer, $upperB as xs:integer){ "
			+ "for $n in $lowB to $upperB return $n };"
			+ " let $interval := local:buildInterval(%d, %d) for $year in $interval"
			+ " return concat(string($year),\": \","
			+ " count(for $pub in //proceedings | //inproceedings where $pub/year=$year return $pub))",
		yearLowerBound, yearUpperBound);
	ClientQuery query;
	try {
	    query = session.query(numberOfPublicationsPerYearIntervalQuery);
	    while (query.more()) {
		result.add(query.next());
	    }
	} catch (IOException e) {
	    System.out.println(numberOfPublicationsPerYearIntervalQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return result;
    }

    // TASK 8:
    // TODO: This query returns the number of InProceedings associated with a
    // conference.
    // Verify that this is the right interpretation: "[...] featured in all
    // editions of a given conference [...]"
    public int getNumberOfPublicationsPerConferenceByName(String conferenceName) {
	String numberOfPublicationsPerConferenceQuery = String.format("count(let $conference := \"%s\""
		+ " let $proc := //proceedings[booktitle=$conference]" + " for $inproc in //inproceedings"
		+ " where $inproc/crossref=$proc/@key" + " order by $inproc/title" + " return $inproc)",
		conferenceName);
	ClientQuery query;
	try {
	    query = session.query(numberOfPublicationsPerConferenceQuery);
	    if (query.more())
		return Integer.parseInt(query.next());
	    else
		System.out.println("Error in task 8: Query did not return a result");
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return Integer.MIN_VALUE;
    }

    // TASK 9:
    // TODO: Verify that this returns the correct result
    public int countEditorsAndAuthorsOfConferenceByName(String conferenceName) {
	String countEditorsAndAuthorsOfConferenceQuery = String.format("count(distinct-values(let $conference := \"%s\""
		+ " let $procs := (for $proc in //proceedings" + " where $proc/booktitle=$conference" + " return $proc)"
		+ " for $inproc in //inproceedings" + " where $inproc/crossref= $procs/@key"
		+ " return $inproc/author/text() | $procs/editor/text()))", conferenceName);
	ClientQuery query;
	try {
	    query = session.query(countEditorsAndAuthorsOfConferenceQuery);
	    assert (query.more());
	    return Integer.parseInt(query.next());
	} catch (IOException e) {
	    System.out.println(countEditorsAndAuthorsOfConferenceQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return Integer.MIN_VALUE;
	}
    }

    // TASK 10:
    public List<Person> getAllAuthorsOfConferenceByName(String conferenceName) {
	List<Person> authorsOrEditors = new ArrayList<>();
	String allAuthorsOfConferenceQuery = String.format(
		"for $author in distinct-values(let $conference := \"%s\""
			+ " let $procs := (for $proc in //proceedings" + " where $proc/booktitle=$conference"
			+ " return $proc)" + " for $inproc in //inproceedings" + " where $inproc/crossref= $procs/@key"
			+ " return $inproc/author/text() | $procs/editor/text()) order by $author return $author",
		conferenceName);
	ClientQuery query;
	try {
	    query = session.query(allAuthorsOfConferenceQuery);
	    while (query.more()) {
		authorsOrEditors.add(getPersonByName(query.next(), true));
	    }
	} catch (IOException e) {
	    System.out.println(allAuthorsOfConferenceQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return authorsOrEditors;
    }

    // TASK 11:
    public List<Publication> getAllPublicationsOfConferenceByName(String conferenceName) {
	List<Publication> pubs = new ArrayList<>();
	String allInProcsOfConferenceByName = String.format("let $conference := \"%s\""
		+ " let $procs := (for $proc in //proceedings" + " where $proc/booktitle=$conference" + " return $proc)"
		+ " for $inproc in //inproceedings" + " where $inproc/crossref= $procs/@key" + " return $inproc",
		conferenceName);
	ClientQuery query;
	try {
	    query = session.query(allInProcsOfConferenceByName);
	    while (query.more()) {
		pubs.add(XmlToObject.XmlToPublication(query.next(), null, this, true));
	    }
	} catch (IOException e) {
	    System.out.println(allInProcsOfConferenceByName);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return pubs;
    }

    // TASK 12:
    public List<Person> getPeopleThatAreAuthorsAndEditors() {
	List<Person> people = new ArrayList<>();
	String peopleThatAreAuthorsAndEditorsQuery = "distinct-values(for $inProc in //inproceedings"
		+ " let $proc := //proceedings[@key=$inProc/crossref/text()]"
		+ " for $author in $inProc/author where index-of(($proc/editor/text()),$author/text())!=0"
		+ " order by $author/text() return $author)";
	ClientQuery query;
	try {
	    query = session.query(peopleThatAreAuthorsAndEditorsQuery);
	    while (query.more()) {
		people.add(getPersonByName(query.next(), true));
	    }
	} catch (IOException e) {
	    System.out.println(peopleThatAreAuthorsAndEditorsQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return people;
    }

    // TASK 13:
    public List<InProceedings> getPublicationsWhereAuthorIsLast(String authorName) {
	List<InProceedings> inProcs = new ArrayList<>();
	String allInProceedinsWhereAuthorLastQuery = String.format("let $author := \"%s\""
		+ " for $inProc in //inproceedings where index-of(($inProc/author),$author)=count($inProc/author)"
		+ " return $inProc", authorName);
	ClientQuery query;
	try {
	    query = session.query(allInProceedinsWhereAuthorLastQuery);
	    while (query.more()) {
		inProcs.add(XmlToObject.XmlToInProceeding(query.next(), this, false));
	    }
	} catch (IOException e) {
	    System.out.println(allInProceedinsWhereAuthorLastQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return inProcs;
    }

    // TASK 14:
    // let $lowerBound := 1960
    // let $upperBound := 1962
    // let $inProcs := (for $inProc in //inproceedings
    // where $inProc/year>=$lowerBound and $inProc/year<=$upperBound
    // return $inProc)
    // for $proc in //proceedings
    // where $proc/editor/text()=$inProcs/author/text()
    //
    // return $proc
    public List<Publisher> task14(int yearLowerBound, int yearUpperBound) {
	List<Publisher> pubs = new ArrayList<>();
	String task14Query = String.format("distinct-values(let $lowerBound := %d" + " let $upperBound := %d"
		+ " let $inProcs := (for $inProc in //inproceedings"
		+ " where $inProc/year>=$lowerBound and $inProc/year<=$upperBound return $inProc)"
		+ " for $proc in //proceedings where $proc/editor/text()=$inProcs/author/text()"
		+ " order by $proc/publisher return $proc/publisher)", yearLowerBound, yearUpperBound);
	ClientQuery query;
	try {
	    query = session.query(task14Query);
	    while (query.more()) {
		pubs.add(getPublisherByName(query.next().replace("&", "&amp;")));
	    }
	} catch (IOException e) {
	    System.out.println(task14Query);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return pubs;
    }

    // Seba: get person by id
    // id is expected to be the name of the person
    public Person getPersonById(String id, boolean lazy) {
	Person per = new Person(id);
	per.setId(id);
	if (lazy == false) {
	    per.setAuthoredPublications(getAuthoredPublications(per.getName(), true));
	    per.setEditedPublications(getEditedPublications(per.getName(), true));
	}
	return per;
    }

    public Set<Publication> getAuthoredPublications(String personName, boolean lazy) {
	Set<Publication> authoredPubs = new HashSet<>();
	String AuthoredInProceedings = "let $PersonName := \"" + personName
		+ "\" for $inproc in root/inproceedings where index-of(($inproc/author), $PersonName)!=0 return $inproc";
	ClientQuery query;
	try {
	    query = session.query(AuthoredInProceedings);
	    while (query.more()) {
		String authoredInProceeding = query.next();
		authoredPubs.add(XmlToObject.XmlToInProceeding(authoredInProceeding, this, lazy));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return authoredPubs;
    }

    public Set<Publication> getEditedPublications(String personName, boolean lazy) {
	Set<Publication> authoredPubs = new HashSet<>();
	String editedProceedings = "let $PersonName := \"" + personName
		+ "\" for $proc in root/proceedings where index-of(($proc/editor), $PersonName)!=0 return $proc";
	ClientQuery query;
	try {
	    query = session.query(editedProceedings);
	    while (query.more()) {
		String editedProceeding = query.next();
		authoredPubs.add(XmlToObject.XmlToProceeding(editedProceeding, null, this, lazy));
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return authoredPubs;
    }

    public Conference getConferenceByName(String confName, boolean lazy) {
	Conference conf = new Conference(confName);
	conf.setId(confName);
	if (lazy == false)
	    conf.setEditions(getConfEditionsForConf(conf));
	return conf;
    }

    public Set<ConferenceEdition> getConfEditionsForConf(Conference conference) {
	assert (conference.getName() != null);
	Set<ConferenceEdition> confEdits = new HashSet<>();
	// TODO: Conference with name: <<Informatik und "Dritte Welt">> will
	// cause an error in the query. I counldn't find out how to escape it
	// properly
	String confEditionsGivenConfQuery = String.format(
		"let $confName := \"%s\" for $proc in root/proceedings where $proc/booktitle=$confName"
			+ " return <ConfEdit>{$proc/year,$proc}</ConfEdit>",
		conference.getName().replace("\"", "&quot"));
	ClientQuery query;

	try {
	    query = session.query(confEditionsGivenConfQuery);
	    while (query.more()) {
		String confedit = query.next();
		confEdits.add(XmlToObject.XmlToConferenceEdition(confedit, conference, this));
	    }
	} catch (IOException e) {
	    System.out.println(confEditionsGivenConfQuery);
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return confEdits;
    }

    public Series getSeriesByName(String name) {
	Series serie = new Series(name);
	Set<Publication> pubs = new HashSet<>();
	String getPublicationsFormSeriesQuery = String.format("//proceedings[series=\"%s\"]", name);
	ClientQuery query;
	try {
	    query = session.query(getPublicationsFormSeriesQuery);
	    while (query.more()) {
		String xml = query.next();
		Publication pub = XmlToObject.XmlToPublication(xml, null, this, true);
		if (pub != null)
		    pubs.add(pub);
		else
		    System.out.println("Error is function getSeriesByName. Publication");
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	serie.setPublications(pubs);
	return serie;

    }

    // GUI functions
    public List<Publisher> getPublishers() {
	List<Publisher> pubs = new ArrayList<>();
	String allPublishersQuery = "distinct-values(for $pub in //proceedings/publisher/text() | //inproceedings/publisher/text() order by $pub return $pub)";
	ClientQuery query;
	try {
	    query = session.query(allPublishersQuery);
	    while (query.more()) {
		pubs.add(getPublisherByName(query.next()));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return pubs;
    }

    public List<Publication> getPublications() {
	List<Publication> pubs = new ArrayList<>();
	String publicationsQuery;
	publicationsQuery = "for $pub in root/proceedings | root/inproceedings " + "order by $pub/title return $pub";
	ClientQuery query;
	try {
	    query = session.query(publicationsQuery);
	    while (query.more()) {
		String queryResult = query.next();
		System.out.println(queryResult);
		pubs.add(XmlToObject.XmlToPublication(queryResult, null, this, false));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return pubs;
    }

    public List<Proceedings> getProceedings() {
	List<Proceedings> procs = new ArrayList<>();

	try {
	    ClientQuery query = session.query("data(//proceedings/@key)");
	    while (query.more()) {
		Proceedings proc = getProceedingById(query.next());
		if (proc != null) {
		    procs.add(proc);
		} else
		    System.err.println("Query \"getProceedings\": proceeding could not be found given the correct id");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return procs;
    }

    /**
     * @return A list of all InProceedings
     */
    public List<InProceedings> getInProceedings() {
	ArrayList<InProceedings> inProcs = new ArrayList<>();

	try {
	    ClientQuery query = session.query("//inproceedings");
	    while (query.more()) {
		InProceedings inProc = getInProceedingsObject(query.next());
		if (inProc != null) {
		    inProcs.add(inProc);
		}
	    }

	} catch (IOException e) {
	    System.err.println("Could not query for inProceedings in getInProceedings()");
	}

	return inProcs;
    }

    public List<Person> getPeople() {
	List<Person> people = new ArrayList<>();
	String allPeopleQuery = "distinct-values(//proceedings/editor/text() | //inproceedings/author/text())";
	ClientQuery query;
	try {
	    query = session.query(allPeopleQuery);
	    while (query.more()) {
		people.add(getPersonByName(query.next(), true));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return people;
    }

    public List<Conference> getConferences() {
	List<Conference> confs = new ArrayList<>();
	String allConferencesQuery = "distinct-values(for $conf in //proceedings/booktitle/text() | //inproceedings/booktitle/text() order by $conf return $conf)";
	ClientQuery query;
	try {
	    query = session.query(allConferencesQuery);
	    while (query.more()) {
		confs.add(getConferenceByName(query.next(), false));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return confs;
    }

    public List<Series> getSeries() {
	List<Series> series = new ArrayList<>();
	String allSeriesQuery = "distinct-values(//proceedings/series)";
	ClientQuery query;
	try {
	    query = session.query(allSeriesQuery);
	    while (query.more()) {
		series.add(getSeriesByName(query.next()));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return series;
    }

    public List<ConferenceEdition> getConferenceEditions() {
	List<ConferenceEdition> confEditions = new ArrayList<>();
	String allConferencEditionsQuery = "//proceedings[exists(booktitle)]";
	ClientQuery query;
	try {
	    query = session.query(allConferencEditionsQuery);
	    while (query.more()) {
		confEditions.add(XmlToObject.XmlProceedingToConferenceEdition(query.next(), this));
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return confEditions;
    }

    // Learn about insert, update, delete :
    // https://www.w3.org/TR/xquery-update-10/#id-delete
    // ADD Functions
    public void addInProceeding(InProceedings inProc) {
	if (getInProceedingsById(inProc.getId()) == null) {
	    String xml = XmlToObject.inProcToXml(inProc);
	    String query = String.format("insert node %s into /root", xml);
	    try {
		System.out.println(query);
		session.query(query);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    System.out.println("Added Inproceeding: " + inProc.getId() + " to database");
	} else
	    System.out.println("(function: \"addInProceeding\") Refuse to add Inproceeding with id: + " + inProc.getId()
		    + " id already exists");
    }

    public void addProceeding(Proceedings proc) {
	if (getProceedingById(proc.getId()) == null) {
	    String xml = XmlToObject.procToXml(proc);
	    String query = String.format("insert node %s into /root", xml);
	    try {
		System.out.println(query);
		session.query(query);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	    }
	    System.out.println("Added proceeding: " + proc.getId() + " to database");
	} else
	    System.out.println("(function: \"addProceeding\") Refuse to add proceeding with id: + " + proc.getId()
		    + " id already exists");

    }

    // DELETE Functions
    public void deleteProceedingById(String id) {
	assert getProceedingById(id) != null : "It is assumend the proceeding to be deleted also exists. Faulty id: "
		+ id;
	String deleteProceedingQuery = String.format("delete node //proceedins[@key=\"%s\"]", id);
	try {
	    System.out.println(deleteProceedingQuery);
	    session.query(deleteProceedingQuery);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return;
	}
	System.out.println("Deleted proceeding with id: " + id + " in database");

    }

    public void deleteInProceedingById(String id) {
	assert getInProceedingsById(
		id) != null : "It is assumend the InProceeding to be deleted also exists. Faulty id: " + id;
	String deleteInProceedingQuery = String.format("delete node //inproceedins[@key=\"%s\"]", id);
	try {
	    System.out.println(deleteInProceedingQuery);
	    session.query(deleteInProceedingQuery);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return;
	}
	System.out.println("Deleted inproceeding with id: " + id + " in database");

    }

    // UPDATE Functions
    // This identifies a proceeding by its id. So if you want to modify the id
    // you have to use the delete and add functions in order to create a new
    // object
    // TODO: This function is not complete
    public void updateProceeding(Proceedings proc) {
	Proceedings oldProc = getProceedingById(proc.getId());
	if (oldProc == null) {
	    System.err.println("(function \"updateProceeding()\") Warning: Proceeding with id: " + proc.getId()
		    + " could not be updated, since it does not exist in the databse.");
	    return;
	}
	String queryFormat = "replace value of node //proceedings[@key=\"" + proc.getId() + "\"]/%s with %s";
	String deleteQueryFormat = "delete node //proceedings[@key=\"" + proc.getId() + "\"]/%s";
	// Caution the last parameter needs to have a '/' as a prefix
	String insertQueryFormat = "insert node %s as last into //proceedings[@key=\"" + proc.getId() + "\"]%s";
	try {
	    // Update Title
	    if (!oldProc.getTitle().equals(proc.getTitle())) {
		session.query(String.format(queryFormat, "title", proc.getTitle()));
	    }
	    // Update Editors
	    List<Person> oldAuthors = oldProc.getAuthors();
	    List<Person> newAuthors = proc.getAuthors();
	    int nNewAuthors = newAuthors.size();
	    int bound = Integer.min(nNewAuthors, oldAuthors.size());
	    int index;
	    for (index = 0; index < bound; ++index) {
		if (!newAuthors.get(index).getId().equals(oldAuthors.get(index).getId())) {
		    session.query(
			    String.format(queryFormat, "editor[" + index + 1 + "]", newAuthors.get(index).getName()));
		}
	    }
	    if (nNewAuthors < oldAuthors.size()) {
		// The size() function already encounters the fact that xQuery
		// indices start with 1
		String subsequence = String.format("subsequence(%s,%d,%d)", "editor", nNewAuthors, oldAuthors.size());
		session.query(String.format(deleteQueryFormat, subsequence));
	    } else if (nNewAuthors > bound) {
		for (int i = index; i < nNewAuthors; ++i) {
		    session.query(String.format(insertQueryFormat,
			    String.format("<editor>%s</editor>", newAuthors.get(i).getName()), "/editor"));
		}
	    }
	    // Update Publisher
	    if (!oldProc.getPublisher().getId().equals(proc.getPublisher().getId())) {
		session.query(String.format(queryFormat, "publisher", proc.getPublisher().getName()));
	    }
	    // Update Year
	    if (oldProc.getYear() != proc.getYear()) {
		session.query(String.format(queryFormat, "year", proc.getYear()));
	    }
	    // Update ISBN
	    if (!oldProc.getIsbn().equals(proc.getIsbn())) {
		session.query(String.format(queryFormat, "isbn", proc.getIsbn()));
	    }
	    // Update Conference
	    // TODO: throws nullPointer exception and I'm too exhausted to fix
	    // it
	    if (!oldProc.getConference().equals(proc.getConference())) {
		if (oldProc.getConference() != null) {
		    // TODO: Verify that we actually want that
		    assert proc.getConference().getName() != null : "Proceeding with id: " + proc.getId()
			    + " has a Conference attribute that does not have a name";
		    session.query(String.format(queryFormat, "booktitle", proc.getConference().getName()));
		} else if (oldProc.getConference() == null) {
		    session.query(String.format(insertQueryFormat,
			    String.format("<booktitle>%s</booktitle>", proc.getConference().getName()), ""));
		}
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
