package ch.ethz.globis.mtfobu.odb_project;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlImport {
	
	private Database database;
	private Status status;
	
	public enum Status {
	    XML_INVALID, 
	    XML_NOT_FOUND,
	    XML_IMPORT_SUCCESS
	    
	}
	
	public XmlImport(Database database){
		this.database = database;
	}
	
	public void ImportFromXML(String filename) throws UnexpectedContent{
		Document doc = openXML(filename);
		
		if(doc != null)
			parseXml(doc);
	}
	
	public Document openXML(String filename){
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try{
			doc = builder.build(new File(filename));
		}
		catch(JDOMException ex){
			System.out.printf("Error while parsing file: %s\nThis is not a valid XML file", filename);
			ex.printStackTrace();
			status = Status.XML_INVALID;
			return null;
		}
		catch(IOException ex){
			System.out.printf("I/O Error while parsing the file: %s\n make sure it exists and that this application has read access to it", filename);
			ex.printStackTrace();
			status = Status.XML_NOT_FOUND;
			return null;
		}
		return doc;
		
	}
	
	public void parseXml(Document doc){
		Element root = doc.getRootElement();
		List<Element> proceedings = root.getChildren("proceedings");
		
		importProceedings(proceedings);
		
		
		
		//List<Element> proceedings = root.getChildren("inproceedings");
		status = Status.XML_IMPORT_SUCCESS;
		
	}
	
	@SuppressWarnings("unchecked")
	public void importProceedings(List<Element> proceedings){
		
		
		for (int i = 0; i < proceedings.size(); i++) {
			Element nodeProceeding = proceedings.get(i);
			
			
			Proceedings proceeding = new Proceedings();
			proceeding.setId(nodeProceeding.getAttributeValue("key"));
			
			List<Element> children = nodeProceeding.getChildren();
			
			//for each child of a proceeding node
			for (int j = 0; j < children.size(); j++) {
				Element child = children.get(i);
				String nodeName = child.getName();
				
				switch (nodeName) {
				case "editor":
					
					break;
				case "title":
					proceeding.setTitle(child.getText());
					break;
				case "booktitle":
					//check and add conference
					break;
				case "publisher":
					break;
				case "year":
					//Confercen(booktitle needs to be created first) check, create and set conference edition
					break;
				case "isbn":
					break;
				case "series":
					break;
				case "volume":
					break;
				case "note":
					break;
				case "ee":
					proceeding.setElectronicEdition(child.getText());
					break;

				default:
					
					break;
				}
				
			}
			
			
			
			
		}
		
	}
	
	

}
