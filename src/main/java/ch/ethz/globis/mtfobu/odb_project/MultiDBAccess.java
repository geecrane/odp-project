package ch.ethz.globis.mtfobu.odb_project;

public class MultiDBAccess {
	// field gives information about if the different databases should be
	// synchronized
	private boolean independent;
	// turns error messages on or off
	private static boolean verbose;

	private final DatabaseBaseX dbBaseX;
	private final DatabaseMongoDB dbMongoDB;
	private final DatabaseZooDB dbZooDB;

	public static enum Databases {
		BaseX, MongoDB, ZooDB
	}

	public MultiDBAccess() {
		independent = true;
		dbBaseX = DatabaseBaseX.getDatabase();
		dbMongoDB = new DatabaseMongoDB(Config.DATABASE_NAME);
		dbZooDB = new DatabaseZooDB(Config.DATABASE_NAME);
	}

	// ADD functions
	/**
	 * Add proceeding to a specific database.
	 * 
	 * @param proc
	 *            Proceeding to add. If <b>null</b> nothing changes
	 * @param db
	 *            Database to use. Has to be one of the databases given by <i>
	 *            MultiDBAccess.Databases </i>. This parameter is assumed to
	 *            unequal to <b>null</b>
	 */
	public void addProceeding(Proceedings proc, MultiDBAccess.Databases db) {
		assert db != null : "Database had not been specified";
		if (proc != null) {
			switch (db) {
			case BaseX:
				// TODO
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:addProceeding() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:addProceeding() ] Ignored add request since @param proc was null.");
	}

	/**
	 * Add inproceeding to a specific database.
	 * 
	 * @param inproc
	 *            inproceeding to add. If <b>null</b> nothing changes
	 * @param db
	 *            Database to use. Has to be one of the databases given by <i>
	 *            MultiDBAccess.Databases </i>. This parameter is assumed to
	 *            unequal to <b>null</b>
	 */
	public void addInProceeding(InProceedings inproc, Databases db) {
		assert db != null : "Database had not been specified";
		if (inproc != null) {
			switch (db) {
			case BaseX:
				// TODO
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:addInProceeding() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:addInProceeding() ] Ignored add request since @param inproc was null.");
	}

	// DELETE functions
	/**
	 * Deletes proceeding given by its id
	 * 
	 * @param procID Id of the proceeding to delete
	 * @param db
	 */
	public void deleteProceedingByID(String procID, Databases db) {
		assert db != null : "Database had not been specified";
		if (procID != null) {
			switch (db) {
			case BaseX:
				dbBaseX.deleteProceedingById(procID);
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:deleteProceedingByID() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:deleteProceedingByID() ] Ignored add request since @param procID was null.");

	}

	public void deleteInProceedingByID(String inProcID, Databases db) {
		assert db != null : "Database had not been specified";
		if (inProcID != null) {
			switch (db) {
			case BaseX:
				dbBaseX.deleteInProceedingById(inProcID);
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:deleteInProceedingByID() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:deleteInProceedingByID() ] Ignored add request since @param inProcID was null.");

	}

	// UPDATE functions
	/**
	 * Updates the proceeding in a given database.
	 * 
	 * @param proc
	 *            proceeding to update. If <b>null</b> nothing changes
	 * @param db
	 *            Database to use. Has to be one of the databases given by <i>
	 *            MultiDBAccess.Databases </i>. This parameter is assumed to
	 *            unequal to <b>null</b>
	 */
	public void updateProceeding(Proceedings proc, Databases db) {
		assert db != null : "Database had not been specified";
		if (proc != null) {
			switch (db) {
			case BaseX:
				// TODO
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:updateProceeding() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:updateProceeding() ] Ignored add request since @param proc was null.");
	}

	/**
	 * Updates the inproceeding in a given database.
	 * 
	 * @param inproc
	 *            inproceeding to update. If <b>null</b> nothing changes
	 * @param db
	 *            Database to use. Has to be one of the databases given by <i>
	 *            MultiDBAccess.Databases </i>. This parameter is assumed to
	 *            unequal to <b>null</b>
	 */
	public void updateInProceeding(InProceedings inproc, Databases db) {
		assert db != null : "Database had not been specified";
		if (inproc != null) {
			switch (db) {
			case BaseX:
				// TODO
				if (independent)
					break;
			case MongoDB:
				// TODO
				if (independent)
					break;
			case ZooDB:
				// TODO
				if (independent)
					break;

			default:
				if (verbose)
					System.err.println(String.format(
							"[ function:updateInProceeding() ] Error: the given database name: %s has no candidate",
							db.name()));
				break;
			}
		} else if (verbose)
			System.out.println("[ function:updateInProceeding() ] Ignored add request since @param inproc was null.");
	}
}
