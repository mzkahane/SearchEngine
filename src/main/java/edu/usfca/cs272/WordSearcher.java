package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
	private static LinkedHashMap<Path, Integer> findResults(TreeSet<String> query, WordIndex index, boolean exact) {
		LinkedHashMap<Path, Integer> results = new LinkedHashMap<>();
		Set<String> indexKeys = index.getKeys();
		for (String word : query) {
			for (String key : indexKeys) {
				if (exact ? index.has(word) : key.startsWith(word)) {
					String current = exact ? word : key;
					var temp = index.get(current);

					for (Path location : temp.keySet()) {
						if (results.containsKey(location)) {
							int newCount = results.get(location) + index.size(current, location);
							results.put(location, newCount);
						} else if (!results.containsKey(location)) {
							results.put(location, index.size(current, location));
						}
					}
				}
				if (exact) {
					break;
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
	 * @param queryPath the path at which the file containing the queries can be found
	 * @param index the index for reference words, locations, and positions
	 * @param searchResults the map to add the search results to
	 * @param exact flag to mark if an exact search should be performed or not
	 */
	public static void search
	(Path queryPath, WordIndex index, TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults, boolean exact) {
		LinkedHashMap<Path, Integer> results = null;
		TreeSet<String> cleanedQuery = null;
		try (BufferedReader reader = Files.newBufferedReader(queryPath, UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				cleanedQuery = WordCleaner.uniqueStems(line);
				if (cleanedQuery.size() > 0) {
					results = findResults(cleanedQuery, index, exact);

					ArrayList<LinkedHashMap<String, String>> scoredResults = new ArrayList<>();
					assert results != null;
					ArrayList<SearchResult> temp = new ArrayList<>();
					Path location;
					for (Path path : results.keySet()) {
						location = path;

						int appearances = results.get(location);
						SearchResult result = new SearchResult(appearances, ((double)appearances/index.getWordCount(location.toString())), location.toString());
						temp.add(result);
					}

					Collections.sort(temp);
					for (SearchResult result : temp) {
						scoredResults.add(result.format());
					}

					assert cleanedQuery != null;
					String joinedQuery = String.join(" ", cleanedQuery);
					searchResults.put(joinedQuery, scoredResults);
				}
			}
		} catch (IOException e) {
			System.out.println("Something went wrong processing -query");
		}
	}
}




