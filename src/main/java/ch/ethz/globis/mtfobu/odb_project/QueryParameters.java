package ch.ethz.globis.mtfobu.odb_project;

import java.util.OptionalLong;

public class QueryParameters {
	public boolean isRanged;
	public boolean isSearch;
	public int pageNumber;
	public OptionalLong rangeEnd;
	public OptionalLong rangeStart;
	public String searchTerm;
}
