package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Processes search queries and calculates the search results and their scores
 *
 * @author Matthew Kahane
 */
public class WordSearcher {

	/**
	 * Finds all of the paths in the index that the words in the query appear in.
	 *
	 * @param query the set of words to search for in the index
	 * @param index the index to search through
	 * @param exact marks whether the search should be exact or partial
	 * @return an ArrayList of the paths that have at least one occurrence of
	 * one of the query words.
	 */
	private static LinkedHashMap<String, Integer> findResults(TreeSet<String> query, WordIndex index, boolean exact) {
		LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
		if (exact) {
			for (String word : query) {
				if (index.has(word)) {
					var temp = index.get(word);

					for (String location : temp.keySet()) {
						if (results.containsKey(location)) {
							int newCount = results.get(location) + index.size(word, location);
							results.put(location, newCount);
						} else if (!results.containsKey(location)) {
							results.put(location, index.size(word, location));
						}
					}
				}
			}
		} else {
			Set<String> indexKeys = index.getKeys();
			for (String word : query) {
				for (String key : indexKeys) {
					if (key.startsWith(word)) {
						var temp = index.get(key);

						for (String location : temp.keySet()) {
							if (results.containsKey(location)) {
								int newCount = results.get(location) + index.size(key, location);
								results.put(location, newCount);
							} else if(!results.containsKey(location)) {
								results.put(location, index.size(key, location));
							}
						}
					}
				}
			}
		}

		return results;
	}

	/**
	 * Calculates the number of appearances of each query word and the score of the
	 * result. sorts them, and stores them in a data structure to be returned.
	 *
	 * Outer LinkedHashMap maps the query to the inner LinkedHashMap.
	 * Inner LinkedHashMap contains the count, score, and path of each result.
	 *
	 * @param query the set of unique stems to search on
	 * @param index the index for reference words, locations, and positions
	 * @param exact flag to mark if an exact search should be performed or not
	 *
	 * @return A LinkedHashMap containing the results of the search
	 */
	public static ArrayList<LinkedHashMap<String, String>> search
	(TreeSet<String> query, WordIndex index, boolean exact) {
		LinkedHashMap<String, Integer> results = findResults(query, index, exact);
		ArrayList<LinkedHashMap<String, String>> scoredResults = new ArrayList<>();

		String location;
		for (String path : results.keySet()) {
			location = path;
			var temp = new LinkedHashMap<String, String>();
			int appearances = results.get(location);
			temp.put("count", String.format("%d", appearances));
			temp.put("score", String.format("%.8f", (double) appearances/index.getWordCount(location.toString())));
			temp.put("where", ('"' + location.toString() + '"'));

			int j = scoredResults.size();
			for (int i = 0; i < scoredResults.size(); i++) {
				if (Float.parseFloat(scoredResults.get(i).get("score")) < Float.parseFloat(temp.get("score"))) {
					j = i;
					break;
				} else if (Float.parseFloat(scoredResults.get(i).get("score")) == Float.parseFloat(temp.get("score"))) {
					if (Float.parseFloat(scoredResults.get(i).get("count")) < Float.parseFloat(temp.get("count"))) {
						j = i;
						break;
					} else if (Float.parseFloat(scoredResults.get(i).get("count")) > Float.parseFloat(temp.get("count"))) {
						j = i+1;
						break;
					} else if (scoredResults.get(i).get("where").compareToIgnoreCase(temp.get("where")) < 0) {
						j = i+1;
						break;
					} else {
						j = i;
						break;
					}
				}
			}
			scoredResults.add(j, temp);
		}
		return scoredResults;
	}
}




