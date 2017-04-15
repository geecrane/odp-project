package ch.ethz.globis.mtfobu.odb_project;

//George: All configuration parameters go here
final public class Config {

	public final static String DATABASE_NAME = "dblpDB";
	
	public final static String PROCEEDINGS_COLLECTION = "proceedings";
	public final static String PROCEEDINGS_TITLE = "title";
	public final static String PROCEEDINGS_YEAR = "year";
	public final static String PROCEEDINGS_ISBN = "isbn";
	public final static String PROCEEDINGS_VOLUME = "volume";	
	public final static String PROCEEDINGS_NOTE = "note";
	public final static String PROCEEDINGS_ELECTRONIC_EDITION = "electronicEdition";
	public final static String PROCEEDINGS_NUMBER = "number";
	public final static String PROCEEDINGS_SERIES_KEY = "series_key";
	public final static String PROCEEDINGS_CONFERENCE_EDITION_KEY = "conference_edition_key";
	public final static String PROCEEDINGS_PUBLISHER_KEY = "publisher_key";
	public final static String PROCEEDINGS_EDITOR_KEYS = "editor_keys";
	public final static String PROCEEDINGS_INPROCEEDING_KEYS = "inproceeding_keys";
	
	public final static String INPROCEEDINGS_COLLECTION = "inproceedings";
	public final static String INPROCEEDINGS_TITLE = "title";
	public final static String INPROCEEDINGS_YEAR = "year";
	public final static String INPROCEEDINGS_NOTE = "note";
	public final static String INPROCEEDINGS_ELECTRONIC_EDITION = "electronicEdition";
	public final static String INPROCEEDINGS_PAGES = "pages";
	public final static String INPROCEEDINGS_PROCEEDINGS_KEY = "proceedings_key";
	public final static String INPROCEEDINGS_AUTHOR_KEYS = "author_keys";
	
	public final static String SERIES_COLLECTION = "series";
	public final static String SERIES_NAME = "name";
	
	
	public final static String PUBLISHER_COLLECTION = "publishers";
	public final static String PUBLISHER_NAME = "name";
	
	
	public final static String CONFERENCE_EDITION_COLLECTION = "conference_editions";
	public final static String CONFERENCE_EDITION_YEAR = "year";
	public final static String CONFERENCE_EDITION_PROCEEDINGS_KEY= "proceedings_key";
	
	public final static String CONFERENCE_COLLECTION = "conferences";
	public final static String CONFERENCE_NAME = "name";
	public final static String CONFERENCE_EDITION_KEYS= "edition_keys";
	
	public final static String PEOPLE_COLLECTION = "people";
	public final static String PEOPLE_NAME = "name";
	
	public final static String MONGODB_PRIMARY_KEY = "_key";
	
	public final static int PAGE_SIZE = 20;
	
	private Config(){
		//disable instantiation
	}
}
