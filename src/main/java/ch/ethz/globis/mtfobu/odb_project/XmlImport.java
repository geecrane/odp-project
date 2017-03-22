package ch.ethz.globis.mtfobu.odb_project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlImport {
	
	//George: I implemented this class from scratch to replace DataImport class.
	//Unlike DataImport, I took a different approach. I avoided using database queries and only committed once for several reasons:
	//1)The code looks kind of messy 
	//2)DataImport was not working/complete yet
	//2)It's complicated and hard to ensure correctness when the database is involved. 
	//3)Difficult to debug the database (easier to reason with classes/objects only)
	//4)Avoiding database improves performance of the import(avoids creating,closing database connections and using queries many times)
	//5)We will have to extend this project to work with other databases in the next tasks. 
	//  So it's important to keep the import independent of the DBMS as much as possible.
	private Database database;
	private HashMap<String, Proceedings> proceedingsList = new HashMap<>();
	private HashMap<String, InProceedings> inProceedingsList = new HashMap<>();
	private HashMap<String, Conference> conferences = new HashMap<>();
	private HashMap<String, ConferenceEdition> conferenceEditions = new HashMap<>();
	private HashMap<String, Publisher> publishers = new HashMap<>();
	private HashMap<Integer, Person> people = new HashMap<>();
	private HashMap<Integer, Series> seriesList = new HashMap<>();
	
	
	public XmlImport(Database database){
		this.database = database;	
	}
	
	public void ImportFromXML(String filename) throws UnexpectedContent{
		Document doc = openXML(filename);
		
		if(doc != null){
			//parser
			parseXml(doc);
			
			System.out.printf("\nImported succesfully:\n%s proceedings \n%s inProceedings \n%s editors \n%s publishers \n"
					+ "%s conferences \n%s conference editions \n%s series!", proceedingsList.size(), inProceedingsList.size(), people.size(),
					publishers.size(), conferences.size(), conferenceEditions.size(), seriesList.size());
			
			//commit to database
			database.importData(proceedingsList, inProceedingsList);
			System.out.printf("\nSuccessfully commited!");
			
		}
	}
	
	public Document openXML(String filename){
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try{
			doc = builder.build(new File(filename));
		}
		catch(JDOMException ex){
			ex.printStackTrace();
			throw new RuntimeException("Error while parsing file: "+filename+"\nThis is not a valid XML file");
		}
		catch(IOException ex){
			ex.printStackTrace();
			throw new RuntimeException("I/O Error while parsing the file: "+filename+"\n make sure it exists and that this application has read access to it");
		}
		return doc;
		
	}
	
	@SuppressWarnings("unchecked")
	private void parseXml(Document doc){
		Element root = doc.getRootElement();
		//importing proceedings
		List<Element> proceedingsNodes = root.getChildren("proceedings");	
		importProceedings(proceedingsNodes);
		
		//importing inProceedings
		List<Element> inProceedingsNodes = root.getChildren("inproceedings");
		importInProceedings(inProceedingsNodes);
		
	}
	
	@SuppressWarnings("unchecked")
	private void importInProceedings(List<Element> inProceedingsNodes) {
		
		for (int i = 0; i < inProceedingsNodes.size(); i++) {
			Element nodeInProceeding = inProceedingsNodes.get(i);
			
			//instantiate inProceeding with Id
			InProceedings inProceedings = new InProceedings(nodeInProceeding.getAttributeValue("key"));
			
			//year
			String year = nodeInProceeding.getChild("year").getText();
			inProceedings.setYear(Integer.parseInt(year));
			
			//title
			String title = nodeInProceeding.getChild("title").getText();
			inProceedings.setTitle(title);
			
			//Authors (May not Exist)
			List<Element> authorNodes = nodeInProceeding.getChildren("author");
			for(int j = 0; j < authorNodes.size(); j++){
				String authorName = authorNodes.get(j).getText();
				Person author = getAuthor(authorName);
				
				//update authors authoredPublications
				Set<Publication> authoredPublications = author.getAuthoredPublications();
				authoredPublications.add(inProceedings);
				author.setAuthoredPublications(authoredPublications);
				
				//add to inProceedings
				List<Person> authors = inProceedings.getAuthors();
				authors.add(author);
				inProceedings.setAuthors(authors);			
				
			}
			
			//Electronic Edition (May not exist).
			Element eeNode = nodeInProceeding.getChild("ee");
			if(eeNode != null){
				inProceedings.setElectronicEdition(eeNode.getText());
			}
			
			//Note (May not exist).
			Element noteNode = nodeInProceeding.getChild("note");
			if(noteNode != null){
				inProceedings.setNote(noteNode.getText());
			}
			
			//Pages (May not exist).
			Element pagesNode = nodeInProceeding.getChild("pages");
			if(pagesNode != null){
				inProceedings.setNote(pagesNode.getText());
			}
			
			//Proceedings (Proceeding corresponding to foreign-key may not exist)
			Element crossRefNode = nodeInProceeding.getChild("crossref");
			if(crossRefNode != null){
				Proceedings proceedings = getProceedings(crossRefNode.getText());
				if(proceedings != null){
					//update Proceedings
					Set<InProceedings> inProcs = proceedings.getPublications();
					inProcs.add(inProceedings);
					proceedings.setPublications(inProcs);
					//add to inProceedings
					inProceedings.setProceedings(proceedings);
				}
			}
			
			
			inProceedingsList.put(inProceedings.getId(), inProceedings);
		}
		
		
	}

	

	//import all proceedings
	@SuppressWarnings("unchecked")
	private void importProceedings(List<Element> proceedingsNodes){
		
		
		for (int i = 0; i < proceedingsNodes.size(); i++) {
			Element nodeProceeding = proceedingsNodes.get(i);
			
			//instantiate proceeding with Id
			Proceedings proceedings = new Proceedings(nodeProceeding.getAttributeValue("key"));
			
			//year
			String year = nodeProceeding.getChild("year").getText();
			proceedings.setYear(Integer.parseInt(year));
			
			//title
			String title = nodeProceeding.getChild("title").getText();
			proceedings.setTitle(title);
			
			//Conference (May not exist)
			Element conferenceNode = nodeProceeding.getChild("booktitle");
			if(conferenceNode != null){
				String conferenceName = conferenceNode.getText();
				Conference conference = getConference(conferenceName);
				
				//Conference Edition
				ConferenceEdition conferenceEdition = getConferenceEdition(conference, proceedings);
				proceedings.setConferenceEdition(conferenceEdition);
			}
			
			//publisher (May not exist)
			Element publisherNode = nodeProceeding.getChild("publisher");
			if(publisherNode != null){
				String publisherName =  publisherNode.getText();
				Publisher publisher = getPublisher(publisherName);
				//update publishers publications
				Set<Publication> publications = publisher.getPublications();
				publications.add(proceedings);
				publisher.setPublications(publications);
				//add to proceedings
				proceedings.setPublisher(publisher);
			}
			
			//Editors(May not Exist)
			List<Element> editorNodes = nodeProceeding.getChildren("editor");
			for(int j = 0; j < editorNodes.size(); j++){
				String editorName = editorNodes.get(j).getText();
				Person editor = getAuthor(editorName);
				
				//update editors editedPublications
				Set<Publication> editedPublications = editor.getEditedPublications();
				editedPublications.add(proceedings);
				editor.setEditedPublications(editedPublications);
				
				//add to proceedings
				List<Person> editors = proceedings.getAuthors();
				editors.add(editor);
				proceedings.setAuthors(editors);			
				
			}
			
			//ISBN(May not exist).
			Element isbnNode = nodeProceeding.getChild("isbn");
			if(isbnNode != null){
				proceedings.setIsbn(isbnNode.getText());
			}
			
			//Series (May not exist)
			Element seriesNode = nodeProceeding.getChild("series");
			if(seriesNode != null){
				Series series = getSeries(seriesNode.getText());
				//update publications
				Set<Publication> pubs = series.getPublications();
				pubs.add(proceedings);
				series.setPublications(pubs);
				//add to proceedings
				proceedings.setSeries(series);
			}
			
			//Note (May not exist).
			Element noteNode = nodeProceeding.getChild("note");
			if(noteNode != null){
				proceedings.setNote(noteNode.getText());
			}
			
			//Volume (May not exist).
			Element volumeNode = nodeProceeding.getChild("volume");
			if(volumeNode != null){
				proceedings.setVolume(volumeNode.getText());
			}
			
			//Electronic Edition (May not exist).
			Element eeNode = nodeProceeding.getChild("ee");
			if(eeNode != null){
				proceedings.setElectronicEdition(eeNode.getText());
			}
			
			//Number (May not exist).
			Element numberNode = nodeProceeding.getChild("number");
			if(numberNode != null){
				proceedings.setNumber(Integer.parseInt(numberNode.getText()));
			}
				
			proceedingsList.put(proceedings.getId(), proceedings);
		}
		
	}
	
	//May return null.
	private Proceedings getProceedings(String key) {
		Proceedings proceedings = proceedingsList.get(key);
		return proceedings;
	}
	
	private Series getSeries(String seriesName) {
		Integer id = seriesName.hashCode();
		Series series = seriesList.get(id);
		
		if(series == null){
			series = new Series(seriesName);
			seriesList.put(id, series);
		}
		return series;
	}

	private Person getAuthor(String editorName) {
		Integer id = editorName.hashCode();
		Person editor = people.get(id);
		
		if(editor == null){
			editor = new Person(editorName);
			people.put(id, editor);			
		}
		
		return editor;
	}
	
	private Publisher getPublisher(String publisherName) {
		Publisher publisher = publishers.get(publisherName);
		
		if(publisher == null){
			publisher = new Publisher(publisherName);
			publishers.put(publisherName, publisher);					
		}
		
		return publisher;
	}

	private ConferenceEdition getConferenceEdition(Conference conference, Proceedings proceedings) {
		
		String id = conference.getId() + String.valueOf(proceedings.getYear());
		
		ConferenceEdition conferenceEdition = conferenceEditions.get(id);
		if(conferenceEdition == null){
			conferenceEdition = new ConferenceEdition(id,conference, proceedings.getYear(), proceedings);	
			
			//update Conference
			Set<ConferenceEdition> editions = conference.getEditions();
			editions.add(conferenceEdition);
			conference.setEditions(editions);
			
			conferenceEditions.put(id, conferenceEdition);	
			
		}
		return conferenceEdition;
	}

	private Conference getConference(String name){
		Conference conference = conferences.get(name);
		if(conference == null){
			conference = new Conference(name);
			conferences.put(name, conference);
			
		}
		return conference;
	}

}
