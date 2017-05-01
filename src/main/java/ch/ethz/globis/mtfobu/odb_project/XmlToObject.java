package ch.ethz.globis.mtfobu.odb_project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOExceptionWithCause;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XmlToObject {
	private static XMLOutputter xmOut = new XMLOutputter();

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
				publication
						.setConferenceEdition(new ConferenceEdition(new Conference(rootNode.getChildText("booktitle")),
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
				inProc.setProceedings(db.getProceedingById(rootNode.getChildText("crossref")));
			}
			inProc.setNote(rootNode.getChildText("note"));
			inProc.setPages(rootNode.getChildText("pages"));
			String proceedingId = rootNode.getChildText("crossref");
			if (proceedingId != null)
				inProc.setProceedings(db.getProceedingById(proceedingId));
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
		proceeding.setConferenceEdition(new ConferenceEdition(new Conference(proceedingNode.getChildText("booktitle")),
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
			cE = new ConferenceEdition(conf, Integer.parseInt(year), proc);
		} catch (JDOMException e) {
			System.out.println("Error: The conference edition XML was not in the expected format");
			e.printStackTrace();
			return null;
		}
		return cE;
	}

	public static ConferenceEdition XmlProceedingToConferenceEdition(String proceedingXml, Database db)
			throws IOException {
		ConferenceEdition cE;
		SAXBuilder builder = new SAXBuilder();
		InputStream stream = new ByteArrayInputStream(proceedingXml.getBytes("UTF-8"));
		try {
			Document inprocXML = builder.build(stream);
			Element rootNode = inprocXML.getRootElement();
			String ConferenceName = rootNode.getChildText("booktitle");
			Conference conf;
			if (ConferenceName != null)
				conf = db.getConferenceByName(rootNode.getChildText("booktitle"), true);
			else {
				System.out.println(
						"(function: XmlProceedingToConferenceEdition) Warning the given proceeding doesn't have a conference!");
				return null;
			}
			Proceedings proc = XmlToObject.XmlToProceeding(proceedingXml, null, db, true);
			String year = rootNode.getChildText("year");
			cE = new ConferenceEdition(conf, Integer.parseInt(year), proc);
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		}
		return cE;
	}

	public static String inProcToXml(InProceedings inProc) {
		String resultingXml;
		Element inproceeding = new Element("inproceedings");
		Document doc = new Document(inproceeding);
		doc.setRootElement(inproceeding);
		Element root = doc.getRootElement();
		root.setAttribute(new Attribute("key", inProc.getId()));
		root.addContent(new Element("title").setText(inProc.getTitle()));
		List<Person> authors = inProc.getAuthors();
		for (Person author : authors) {
			root.addContent(new Element("author").setText(author.getName()));
		}
		root.addContent(new Element("year").setText(Integer.toString(inProc.getYear())));
		if (inProc.getPages() != null)
			root.addContent(new Element("pages").setText(inProc.getPages()));
		if (inProc.getProceedings() != null)
			root.addContent(new Element("crossref").setText(inProc.getProceedings().getId()));
		if (inProc.getNote() != null)
			root.addContent(new Element("note").setText(inProc.getNote()));
		// root.addContent(new Element("booktitle").setText(inProc.))
		resultingXml = xmOut.outputString(doc.getRootElement());

		System.out.println(resultingXml);
		return resultingXml;
	}

	public static String procToXml(Proceedings proc) {
		String resultingXml;
		Element proceeding = new Element("proceedings");
		Document doc = new Document(proceeding);
		doc.setRootElement(proceeding);
		Element root = doc.getRootElement();
		root.setAttribute(new Attribute("key", proc.getId()));
		List<Person> editors = proc.getAuthors();
		for (Person per : editors) {
			root.addContent(new Element("editor").setText(per.getName()));
		}
		root.addContent(new Element("title").setText(proc.getTitle()));
		Conference conf = proc.getConference();
		if (conf != null) {
			root.addContent(new Element("booktitle").setText(conf.getName()));
		}
		if (proc.getSeries() != null)
			root.addContent(new Element("series").setText(proc.getSeries().getName()));
		Publisher pub = proc.getPublisher();
		if (pub != null) {
			root.addContent(new Element("publisher").setText(pub.getName()));
		}
		root.addContent(new Element("year").setText(Integer.toString(proc.getYear())));
		String isbn = proc.getIsbn();
		if (isbn != null)
			root.addContent(new Element("isbn").setText(isbn));
		resultingXml = xmOut.outputString(root);
		return resultingXml;
	}
}
