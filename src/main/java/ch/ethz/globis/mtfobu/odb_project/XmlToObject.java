package ch.ethz.globis.mtfobu.odb_project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOExceptionWithCause;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XmlToObject {
	/**
	 * 
	 * @param xml
	 *            a string in XML format
	 * @param pub
	 *            publisher if known. This is used to set the publisher
	 *            attribute for a proceeding. Can be null
	 * @param db
	 *            the database context. This is needed in order to correctly set
	 *            the attributes "Publisher" and "Series" in proceedings. Can be
	 *            null for InProceedings
	 * @return a publication object
	 * @throws IOException
	 */
	public static Publication XmlToPublication(String xml, Publisher pub, Database db, boolean lazy)
			throws IOException {
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
				if (lazy == false) {
					List<Element> authors = rootNode.getChildren("author");
					List<Person> authorList = new ArrayList<>();
					for (Element author : authors)
						authorList.add(new Person(author.getText()));
					publication.setAuthors(authorList);
				}
				publication.setNote(rootNode.getChildText("note"));
				publication.setPages(rootNode.getChildText("pages"));

				return publication;
			}
			// The id referred to a proceeding
			else {
				Proceedings publication = new Proceedings(rootNode.getAttributeValue("key"));
				publication.setTitle(rootNode.getChildText("title"));
				publication.setYear(Integer.parseInt(rootNode.getChildText("year")));
				if (lazy == false) {
					List<Element> authors = rootNode.getChildren("editor");
					List<Person> authorList = new ArrayList<>();
					for (Element author : authors)
						authorList.add(new Person(author.getText()));
					publication.setAuthors(authorList);
				}
				publication.setNote(rootNode.getChildText("note"));
				if (db != null) {
					String series = rootNode.getChildText("series");
					if (series != null)
						publication.setSeries(db.getSeriesByName(series));
					if (pub != null) {
						publication.setPublisher(pub);
					} else {
						String publisher = rootNode.getChildText("publisher");
						// Publisher is not fully initialized to avoid cycles
						if (publisher != null)
							publication.setPublisher(new Publisher(publisher));
					}
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

	public static Proceedings XmlToProceeding(String xml, Publisher pub, Database db, boolean lazy) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();
			return proceedingFromElement(rootNode, pub, db, lazy);
		} catch (JDOMException e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	public static InProceedings XmlToInProceeding(String xml, Database db, boolean lazy) throws IOException {
		InProceedings inProc;
		// The result is in XML format therefore JDOM is used in order
		// to parse it
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();
			inProc = new InProceedings(rootNode.getAttributeValue("key"));
			inProc.setTitle(rootNode.getChildText("title"));
			inProc.setYear(Integer.parseInt(rootNode.getChildText("year")));
			if (lazy == false) {
				List<Element> authors = rootNode.getChildren("author");
				List<Person> authorList = new ArrayList<>();
				for (Element author : authors)
					authorList.add(new Person(author.getText()));
				inProc.setAuthors(authorList);
			}
			inProc.setNote(rootNode.getChildText("note"));
			inProc.setPages(rootNode.getChildText("pages"));
		} catch (JDOMException e) {
			System.out.println("Error: The query result was not in the expected XML format");
			e.printStackTrace();
			return null;
		}
		// System.out.println(queryResult);
		return inProc;
	}

	private static Proceedings proceedingFromElement(Element proceedingNode, Publisher pub, Database db, boolean lazy) {
		Proceedings proceeding = new Proceedings(proceedingNode.getAttributeValue("key"));
		proceeding.setTitle(proceedingNode.getChildText("title"));
		proceeding.setYear(Integer.parseInt(proceedingNode.getChildText("year")));
		if (lazy == false) {
			List<Element> authors = proceedingNode.getChildren("editor");
			List<Person> authorList = new ArrayList<>();
			for (Element author : authors)
				authorList.add(new Person(author.getText()));
			proceeding.setAuthors(authorList);
		}
		proceeding.setNote(proceedingNode.getChildText("note"));
		if (db != null) {
			String series = proceedingNode.getChildText("series");
			if (series != null)
				proceeding.setSeries(db.getSeriesByName(series));
			if (pub != null) {
				proceeding.setPublisher(pub);
			} else {
				String publisher = proceedingNode.getChildText("publisher");
				// Publisher is not fully initialized to avoid cycles
				if (publisher != null)
					proceeding.setPublisher(new Publisher(publisher));
			}
		} else
			System.out.println("Proceeding with id: " + proceedingNode.getAttributeValue("key")
					+ " has not been fully initialized because of a missing context");
		proceeding.setConferenceEdition(new ConferenceEdition(proceedingNode.getChildText("year"),
				new Conference(proceedingNode.getChildText("booktitle")),
				Integer.parseInt(proceedingNode.getChildText("year")), proceeding)); // The
																						// conference
																						// edition
																						// is
																						// given
																						// by
																						// the
																						// year
																						// tag
		proceeding.setIsbn(proceedingNode.getChildText("isbn"));
		return proceeding;
	}

	// Asserted XML format:
	// <ConfEdit>
	// <year>1989</year>
	// <proceedings mdate="date" key="key">
	// ...
	// </proceedings>
	// <Conference>Conference Name</Conference>
	// </ConfEdit>
	public static ConferenceEdition XmlToConferenceEdition(String xml, Conference conf, Database db)
			throws IOException {
		ConferenceEdition cE;

		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();
			String year = rootNode.getChildText("year");
			Proceedings proc = proceedingFromElement(rootNode.getChild("proceedings"), null, db, true);
			// Here I assume that the conferencEdition id is the same as the
			// year. TODO: Verify that this assertion is valid
			cE = new ConferenceEdition(year, conf, Integer.parseInt(year), proc);
		} catch (JDOMException e) {
			System.out.println("Error: The conference edition XML was not in the expected format");
			e.printStackTrace();
			return null;
		}
		return cE;
	}
}
