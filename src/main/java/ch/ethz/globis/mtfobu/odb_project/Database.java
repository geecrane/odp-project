package ch.ethz.globis.mtfobu.odb_project;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.OptionalLong;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;


public class Database {
	public final int PAGE_SIZE = 20;
	private final String dbName;
	
	public Database(String dbName){
		this.dbName = dbName;
		
		//George: only creates a database automatically if it doesn't already exist.
		//Does not automatically delete existing database!
		//create()
	}
	
	//George: Enables creation/deletion of database on demand.
	public void create() {
		//George: Remove the database if it exists
		

		//George: create new db

	}
	
	
	public String getName(){
		return dbName;
	}
	
	
	//George: commits the imported XML data to ZooDB
	public void importData(HashMap<String, Proceedings> proceedingsList, HashMap<String, InProceedings> inProceedingsList){
		// TODO: importxml
	}
	
	
	
	/**
	 * Close the database connection.
	 * 
	 * @param pm The current PersistenceManager.
	 */
	private static void closeDB(PersistenceManager pm) {
		//TODO: closedb
	}	
	

	
}
