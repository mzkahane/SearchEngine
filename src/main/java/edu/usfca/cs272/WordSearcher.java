package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
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
	private static TreeMap<String, ArrayList<Path>> findResults(TreeSet<String> query, WordIndex index, boolean exact) {
		// TODO Changed this to a map so that the results can be traced back to the word they were
		// found for. this should make it easier to do the partial and the exact search...
		// avoid at least one of the for loops in search()
		TreeMap<String, ArrayList<Path>> results = new TreeMap<>();
		if (exact) {
			for (String word : query) {
				if (index.has(word)) {
					var temp = index.get(word);

					for (Path location : temp.keySet()) {
						if (results.containsKey(word) && !results.get(word).contains(location)) {
							results.get(word).add(location);
						} else if (!results.containsKey(word)) {
							results.put(word, new ArrayList<Path>());
							results.get(word).add(location);
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

						for (Path location : temp.keySet()) {
							if (results.containsKey(key) && !results.get(key).contains(location)) {
									results.get(key).add(location);
							} else if(!results.containsKey(key)) {
								results.put(key, new ArrayList<Path>());
								results.get(key).add(location);
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
		TreeMap<String, ArrayList<Path>> results = findResults(query, index, exact);
		ArrayList<LinkedHashMap<String, String>> scoredResults = new ArrayList<>();

		for (String word : results.keySet()) {
			int appearances = 0;
			Path location = null; //TODO add a null check?
			for (Path path : results.get(word)) {
				location = path;
				appearances += index.get(word).get(location).size();

				String appearanceCount = String.format("%d", appearances);
				String score = String.format("%.8f", (double) appearances/index.getWordCount(location.toString()));
				String where = ('"' + location.toString() + '"');

				LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();

				temp.put("count", appearanceCount);
				temp.put("score", score);
				temp.put("where", where);

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
		}

//		for (Path location : results) {
//			int appearances = 0;
//
//			if (exact) {
//
//			} else {
//
//			}
//
//			for (String word : query) {
//				var got = index.get(word);
//				if (got != null && got.containsKey(location)) {
//					appearances += index.get(word).get(location).size(); //XXX this is wrong.. its only counting the literal appearances, not the stems
//				}
//			}
//
//			/* ---- BELOW THIS IS CORRECT ---- */
//			String appearanceCount = String.format("%d", appearances);
//			String score = String.format("%.8f", (double) appearances/index.getWordCount(location.toString()));
//			String where = ('"' + location.toString() + '"');
//
//			LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
//
//			temp.put("count", appearanceCount);
//			temp.put("score", score);
//			temp.put("where", where);
//
//			int j = scoredResults.size();
//			for (int i = 0; i < scoredResults.size(); i++) {
//				if (Float.parseFloat(scoredResults.get(i).get("score")) < Float.parseFloat(temp.get("score"))) {
//					j = i;
//					break;
//				} else if (Float.parseFloat(scoredResults.get(i).get("score")) == Float.parseFloat(temp.get("score"))) {
//					if (Float.parseFloat(scoredResults.get(i).get("count")) < Float.parseFloat(temp.get("count"))) {
//						j = i;
//						break;
//					} else if (Float.parseFloat(scoredResults.get(i).get("count")) > Float.parseFloat(temp.get("count"))) {
//						j = i+1;
//						break;
//					} else if (scoredResults.get(i).get("where").compareToIgnoreCase(temp.get("where")) < 0) {
//						j = i+1;
//						break;
//					} else {
//						j = i;
//						break;
//					}
//				}
//			}
//
//			scoredResults.add(j, temp);
//		}

		return scoredResults;
	}
}




