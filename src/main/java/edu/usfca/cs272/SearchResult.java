package edu.usfca.cs272;

import java.util.LinkedHashMap;

/**
 * Stores the count, score, and location for each result of a search on an Inverted
 * index. Implements comparabe to allow the results to be sorted.
 *
 * @author Matthew Kahane
 */
public class SearchResult implements Comparable<SearchResult> {

	/** The number of appearances of this word in the corresponding location */
	int count;

	/** The score of this result */
	double score;

	/** The path location of the file this result is found in */
	String location;

	/**
	 * Initializes a SeaerchResult object
	 *
	 * @param count the word count to initialize it with
	 * @param score the score for the word to initialize it with
	 * @param location the location the word is found to initialize it with
	 */
	public SearchResult(int count, double score, String location) {
		this.count = count;
		this.score = score;
		this.location = location;
	}

	@Override
	public int compareTo(SearchResult o) {
		if (this.score < o.score) {
			return 1;
		} else if (this.score == o.score) {
			if (this.count < o.count) {
				return 1;
			} else if (this.count > o.count) {
				return -1;
			} else if (this.location.compareToIgnoreCase(o.location) < 0) {
				return -1;
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	/**
	 * formats the search result to be ready for printing. See {@link PrettyJsonWriter}, see {@link Driver}
	 *
	 * @return A map of the contents of this search result formatted properly as strings
	 */
	public LinkedHashMap<String, String> format() {
		var out = new LinkedHashMap<String, String>();

		out.put("count", String.format("%d", count));
		out.put("score", String.format("%.8f", score));
		out.put("where", '"' + location + '"');

		return out;
	}
}
