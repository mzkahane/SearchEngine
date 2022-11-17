package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Processes search queries and calculates the search results and their scores
 * in a thread safe way.
 *
 * See {@link WordSearcher}
 *
 * @author Matthew Kahane
 */
public class ThreadSafeWordSearcher extends WordSearcher {

	/**
	 * Takes a path input to parse one or more search queries from. For each query,
	 * a Task is created to find the locations, and the appearance count in each
	 * location, with which it creates a search result that is added to the
	 * searchResults map.
	 *
	 * @param queryPath the path to a file containing queries to search on
	 * @param index the index to search through
	 * @param searchResults the map to add search results to
	 * @param exact marks whether the search should be exact or partial
	 * @param threadCount the number of threads to start the work queue with
	 */
	public static void search(Path queryPath, ThreadSafeIndex index,
			TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults,
			boolean exact, int threadCount) {
		WorkQueue queue = new WorkQueue(threadCount);

		try (BufferedReader reader = Files.newBufferedReader(queryPath, UTF_8)) {
			while (reader.ready()) {
				TreeSet<String> cleanedQuery = WordCleaner.uniqueStems(reader.readLine());
				if (cleanedQuery.size() > 0) {
					queue.execute(new Task(cleanedQuery, index, exact, searchResults));
				}
			}
		} catch (IOException e) {
			System.out.println("Could not read file at  query path: " + queryPath);
			// TODO log.catching
			return;
		}
	}

	/**
	 * Creates tasks to be run by workers in the queue. See {@link ThreadSafeWordSearcher#search}
	 *
	 * @author Matthew Kahane
	 */
	private static class Task implements Runnable {

		/** The cleaned and split query to search on */
		private TreeSet<String> query;

		/** The index to search through */
		private ThreadSafeIndex index;

		/** Marks whether or not the search is exact */
		private boolean exact;

		/** The map to add the search results to */
		private TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults;

		/**
		 * Initializes the Task
		 *
		 * @param cleanedQuery the stemmed and split query to search on
		 * @param index the index to search through
		 * @param exact marks whether the search should be exact or partial
		 * @param searchResults the TreeMap to collect the results in
		 */
		public Task(TreeSet<String> cleanedQuery, ThreadSafeIndex index, boolean exact,
				TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults) {
			this.query = cleanedQuery;
			this.index = index;
			this.exact = exact;
			this.searchResults = searchResults;

		}

		@Override
		public void run() {
			LinkedHashMap<Path, Integer> results = new LinkedHashMap<>();
			if (exact) {
				for (String word : query) {
					if (index.has(word)) {
						var temp = index.get(word);

						for (Path location : temp.keySet()) {
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

							for (Path location : temp.keySet()) {
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
			ArrayList<LinkedHashMap<String, String>> scoredResults = new ArrayList<>();

			Path location;
			for (Path path : results.keySet()) {
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
			String joinedQuery = String.join(" ", query);
			synchronized (searchResults) {
				searchResults.put(joinedQuery, scoredResults);
			}
		}
	}
}
