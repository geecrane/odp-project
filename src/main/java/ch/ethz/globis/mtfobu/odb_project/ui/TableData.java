package ch.ethz.globis.mtfobu.odb_project.ui;

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.ConferenceEdition;
import ch.ethz.globis.mtfobu.domains.InProceedings;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Proceedings;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.domains.Publisher;
import ch.ethz.globis.mtfobu.domains.Series;
import ch.ethz.globis.mtfobu.odb_project.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TableData {
    public ObservableList<Person> peopleMasterData = FXCollections.observableArrayList();
    public ObservableList<InProceedings> inProceedingsMasterData = FXCollections.observableArrayList();
    public ObservableList<Proceedings> proceedingsMasterData = FXCollections.observableArrayList();
    public ObservableList<Publication> publicationsMasterData = FXCollections.observableArrayList();
    public ObservableList<Publisher> publishersMasterData = FXCollections.observableArrayList();
    public ObservableList<Conference> confMasterData = FXCollections.observableArrayList();
    public ObservableList<ConferenceEdition> confEdMasterData = FXCollections.observableArrayList();
    public ObservableList<Series> seriesMasterData = FXCollections.observableArrayList();
}
