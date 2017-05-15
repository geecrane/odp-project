package ch.ethz.globis.mtfobu.odb_project.db;

import ch.ethz.globis.mtfobu.odb_project.Config;

public class DatabaseManager {

    private final DatabaseBaseX dbBaseX;
    private final DatabaseMongoDB dbMongoDB;
    private final DatabaseZooDB dbZooDB;

    public static enum DBType {
	BaseX, MongoDB, ZooDB
    }

    public DatabaseManager() {
	dbBaseX = DatabaseBaseX.getDatabase();
	dbMongoDB = DatabaseMongoDB.getDatabase();
	dbZooDB = DatabaseZooDB.getDatabase();
    }

    public Database getDB(DBType type) {

	switch (type) {
	case BaseX:
	    return dbBaseX;
	case MongoDB:
	    return dbMongoDB;
	case ZooDB:
	    return dbZooDB;
	default:
	    System.err.println("DB type not recognized!");
	    break;
	}
	return null;
    }

}
