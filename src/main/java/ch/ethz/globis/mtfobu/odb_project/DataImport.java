package ch.ethz.globis.mtfobu.odb_project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ch.ethz.globis.mtfobu.odb_project.Database;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class DataImport {
	DataImport(Database database){
		db = database;
	}
	@SuppressWarnings("unchecked")
	public int ImoprtFormXML(String filename) throws UnexpectedContent{
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try{
			doc = builder.build(new File(filename));
		}
		catch(JDOMException ex){
			System.out.printf("Error while parsing file: %s\nThis is not a valid XML file", filename);
			ex.printStackTrace();
			return 2;
		}
		catch(IOException ex){
			System.out.printf("I/O Error while parsing the file: %s\n make sure it exists and that this application has read access to it", filename);
			ex.printStackTrace();
			return 1;
		}
		Element root = doc.getRootElement();
		classifyProceedings(root.getChildren("proceedings"));
		return 0;
	}
	@SuppressWarnings("unchecked")
	private int classifyProceedings(List<Element> proceedings) throws UnexpectedContent{
		for( Element prcd: proceedings){
			Proceeding proceeding = new Proceeding();

			//Set editor
			{
				List<Person> editors = new Vector<Person>();

				//Convert Elements representing editors to authors
				List<Element> editorElements = prcd.getChildren("editor");
				for (Element editorElem: editorElements){
					Person editor = null;
					Collection<Person> foundEditors = db.getPeopleByName(editorElem.getTextTrim());
					// author previously known
					if(foundEditors.size() == 1) editor = foundEditors.iterator().next(); 
					// author was not previously included in the database
					else if (foundEditors.size() == 0){
						editor = new Person();
						editor.setName(editorElem.getTextTrim());
						Set<Publication> pbls = new HashSet<Publication>();
						pbls.add(proceeding);
						editor.setEditedPublications(pbls);
					}
					// 2 or more authors are listed in the database, can't determine author of this proceeding
					else{
						throw new UnexpectedContent("Multiple authors with the same name are listed in the database and there is no way to know wich author edited this proceeding");
					}
					editors.add(editor);
				}
				proceeding.setAuthors(editors);
			}
			
			//Set Title
			{
				List<Element> titles = prcd.getChildren("tile");
				if (titles.size() > 1){
					throw new UnexpectedContent("Proceeding has multiple titles");
				}
				else if (titles.size() == 0){
					throw new UnexpectedContent("Proceeding does not have a title! This is a required field! See: http://nwalsh.com/tex/texhelp/bibtx-19.html");
				}
				else{
					proceeding.setTitle(titles.get(0).getTextTrim());
				}
			}
			//set corresponding Conference
			{
				/**Page 3 of the exercise sheet dictates the use of the tag:"<booktitle>" in order to determine the conference name*/
				List<Element> booktitleElements = prcd.getChildren("booktitle");
				if (booktitleElements.size() > 1){
					throw new UnexpectedContent("Proceeding has multiple booktitle tags and therefore multiple conferece names. This was not expected by the developper.");
				}
				else if (booktitleElements.size() == 0){
					System.out.print("Proceeding with title: " + proceeding.getTitle() + " Does not have a conferece name");
				}
				else{
					Publisher publisher;
					Collection<Publisher> foundPublishers = db.getPublisherByName(publisherElements.get(0).getTextTrim());
					// publisher previously known
					if(foundPublishers.size() == 1){
						publisher = foundPublishers.iterator().next(); 
						publisher.addPublication(proceeding);
					}
					// publisher was not previously included in the database
					else if (foundPublishers.size() == 0){
						publisher = new Publisher();
						publisher.setName(publisherElements.get(0).getTextTrim());
						Set<Publication> publs = new HashSet<Publication>();
						publs.add(proceeding);
						publisher.setPublications(publs);
					}
					// 2 or more publishers are listed in the database, can't determine publisher of this proceeding
					else{
						throw new UnexpectedContent("Multiple publishers with the same name are listed in the database and there is no way to know wich publisher published this proceeding");
					}
					
					proceeding.setPublisher(publisher);
				}
			}
			
			//Set Publisher
			{
				List<Element> publisherElements = prcd.getChildren("publisher");
				if (publisherElements.size() > 1){
					throw new UnexpectedContent("Proceeding has multiple publishers");
				}
				else if (publisherElements.size() == 0){
					//throw new UnexpectedContent("Proceeding does not have a publisher");
					System.out.print("Proceeding with title: " + proceeding.getTitle() + " Does not have a publisher");
				}
				else{
					Publisher publisher;
					Collection<Publisher> foundPublishers = db.getPublisherByName(publisherElements.get(0).getTextTrim());
					// publisher previously known
					if(foundPublishers.size() == 1){
						publisher = foundPublishers.iterator().next(); 
						publisher.addPublication(proceeding);
					}
					// publisher was not previously included in the database
					else if (foundPublishers.size() == 0){
						publisher = new Publisher();
						publisher.setName(publisherElements.get(0).getTextTrim());
						Set<Publication> publs = new HashSet<Publication>();
						publs.add(proceeding);
						publisher.setPublications(publs);
					}
					// 2 or more publishers are listed in the database, can't determine publisher of this proceeding
					else{
						throw new UnexpectedContent("Multiple publishers with the same name are listed in the database and there is no way to know wich publisher published this proceeding");
					}
					
					proceeding.setPublisher(publisher);
				}
			}
			//Set year
			{
				List<Element> yearElements = prcd.getChildren("year");
				if (yearElements.size() > 1){
					throw new UnexpectedContent("Proceeding has multiple year tags");
				}
				else if (yearElements.size() == 0){
					throw new UnexpectedContent("Proceeding does not have a year tag! This is a required field! See: http://nwalsh.com/tex/texhelp/bibtx-19.html");
				}
				else{
					proceeding.setYear(Integer.parseInt(yearElements.get(0).getTextTrim()));
				}
			}
			//Set ISBN
			{
				List<Element> isbnElements = prcd.getChildren("isbn");
				if (isbnElements.size() > 1){
					throw new UnexpectedContent("Proceeding has multiple isbn tags");
				}
				else if (isbnElements.size() == 0){
					//throw new UnexpectedContent("Proceeding does not have a isbn tag");
					System.out.print("Proceeding with title: " + proceeding.getTitle() + " Does not have a isbn");
				}
				else{
					proceeding.setIsbn(isbnElements.get(0).getTextTrim());
				}
			}
		






		}


		return 0;
	}
	private Database db;
}
class UnexpectedContent extends Exception{

	private static final long serialVersionUID = 3525539332323217584L;

	//Parameterless Constructor
    public UnexpectedContent() {}

    //Constructor that accepts a message
    public UnexpectedContent(String message)
    {
       super(message);
    }
}
