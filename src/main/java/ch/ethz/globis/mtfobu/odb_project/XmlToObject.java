package ch.ethz.globis.mtfobu.odb_project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlToObject {
	/**
	 * 
	 * @param xml
	 *            a string in XML format
	 * @param db
	 *            the database context. This is needed in order to correctly set
	 *            the attributes "Publisher" and "Series" in proceedings. Can be
	 *            null for InProceedings
	 * @return a publication object
	 * @throws IOException
	 */
	public static Publication XmlToPublication(String xml, Database db) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();
			// The id refered to an inproceeding
			if (rootNode.getName().equals("inproceedings")) {
				InProceedings publication = new InProceedings(rootNode.getAttributeValue("key"));
				publication.setTitle(rootNode.getChildText("title"));
				publication.setYear(Integer.parseInt(rootNode.getChildText("year")));
				List<Element> authors = rootNode.getChildren("author");
				List<Person> authorList = new ArrayList<>();
				for (Element author : authors)
					authorList.add(new Person(author.getText()));
				publication.setAuthors(authorList);
				publication.setNote(rootNode.getChildText("note"));
				publication.setPages(rootNode.getChildText("pages"));

				return publication;
			}
			// The id referred to a proceeding
			else {
				Proceedings publication = new Proceedings(rootNode.getAttributeValue("key"));
				publication.setTitle(rootNode.getChildText("title"));
				publication.setYear(Integer.parseInt(rootNode.getChildText("year")));
				List<Element> authors = rootNode.getChildren("editor");
				List<Person> authorList = new ArrayList<>();
				for (Element author : authors)
					authorList.add(new Person(author.getText()));
				publication.setAuthors(authorList);
				publication.setNote(rootNode.getChildText("note"));
				if (db != null) {
					publication.setSeries(db.getSeriesByName(rootNode.getChildText("series")));
					publication.setPublisher(db.getPublisherByName(rootNode.getChildText("publisher")));
				} else
					System.out.println("Proceeding with id: " + rootNode.getAttributeValue("key")
							+ " has not been fully initialized because of a missing context");
				publication.setConferenceEdition(new ConferenceEdition(rootNode.getChildText("year"),
						new Conference(rootNode.getChildText("booktitle")),
						Integer.parseInt(rootNode.getChildText("year")), publication)); // The
																						// conference
																						// edition
																						// is
																						// given
																						// by
																						// the
																						// year
																						// tag
				publication.setIsbn(rootNode.getChildText("isbn"));
				return publication;
			}

		} catch (JDOMException e) {
			System.out.println("Error: The query result was not in the expected XML format");
			e.printStackTrace();
			return null;
		}
	}

	public static Proceedings XmlToProceeding(String xml, Database db) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();

			Proceedings proceeding = new Proceedings(rootNode.getAttributeValue("key"));
			proceeding.setTitle(rootNode.getChildText("title"));
			proceeding.setYear(Integer.parseInt(rootNode.getChildText("year")));
			List<Element> authors = rootNode.getChildren("editor");
			List<Person> authorList = new ArrayList<>();
			for (Element author : authors)
				authorList.add(new Person(author.getText()));
			proceeding.setAuthors(authorList);
			proceeding.setNote(rootNode.getChildText("note"));
			if (db != null) {
				proceeding.setSeries(db.getSeriesByName(rootNode.getChildText("series")));
				proceeding.setPublisher(db.getPublisherByName(rootNode.getChildText("publisher")));
			} else
				System.out.println("Proceeding with id: " + rootNode.getAttributeValue("key")
						+ " has not been fully initialized because of a missing context");
			proceeding.setConferenceEdition(new ConferenceEdition(rootNode.getChildText("year"),
					new Conference(rootNode.getChildText("booktitle")), Integer.parseInt(rootNode.getChildText("year")),
					proceeding)); // The
									// conference
									// edition
									// is
									// given
									// by
									// the
									// year
									// tag
			proceeding.setIsbn(rootNode.getChildText("isbn"));
			return proceeding;
		} catch (JDOMException e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
}
