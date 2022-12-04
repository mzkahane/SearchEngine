package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Matthew Kahane
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver {

	/** Logger used throughout the Driver */
	private static final Logger log = LogManager.getLogger("Driver");

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		ArgumentParser flags = new ArgumentParser();
		flags.parse(args);
		if (flags.viewFlags().size() == 0) {
			System.out.println("No flags found. please use flag -text [Path] to"
					+ "add a file to the index, and flag -index [Path] to output"
					+ "the index to a specific location");
			return;
		}

		InvertedIndex<Path> index;

		int threadCount = 5;
		boolean multithreaded = false;
		if (flags.hasFlag("-threads")) {
			multithreaded = true;
			threadCount = flags.getInteger("-threads", 5);

			if (threadCount < 1) {
				System.out.println("Thread count must be 1 or greater");
				log.error("Thread count is less than 1");
				return;
			}

			log.debug("Threads flag found, Creating ThreadSafeIndex...");
			index = new ThreadSafeIndex(threadCount);
		} else {
			index = new WordIndex();
		}

		int maxCrawls = 1;
		if (flags.hasFlag("-html")) {
			multithreaded = true;
			index = new ThreadSafeIndex(threadCount);

			//TODO use sockets to download HTML, following up to 3 redirects
			// each worker gets 1 URL, the redirected content is attatched to the original URL
			if (flags.hasFlag("-max")) {
				maxCrawls = flags.getInteger("-max", 1);
				// The max number of links to be crawled.
				// if the number is 3, then the seed link as well as the first 2 links found on that page are to be crawled
			}
			String seed = flags.getString("-html");
			if (seed != null) {
				try {
					// maxCrawls-1 because the seed counts as one of the crawls? (might need to revert depending on how the inner loop works
					WebCrawler.findHTML(seed, maxCrawls-1, (ThreadSafeIndex) index, threadCount);
				} catch (MalformedURLException e) {
					System.out.println("The URL :" + seed + "is malformed");
					log.catching(e);
				}
			} else {
				System.out.println("-html flag must be followed by a URL");
				log.debug("No URL following -html flag");
			}
		}

		Path textPath = null;
		boolean isDirectory  = false;
		Path indexPath = Path.of("index.json"); // default path
		if (flags.hasFlag("-text") && (textPath = flags.getPath("-text")) != null) {
			log.debug("Text flag found");
			if (multithreaded) {
				log.debug("Passing textPath to MultithreadedFileFinder...");
				MultithreadedFileFinder.findAndInput(textPath, (ThreadSafeIndex)index, isDirectory, threadCount);
			} else {
				if (Files.isDirectory(textPath)) {
					log.debug("textPath points to a directory");
					isDirectory = true;
				}

				try {
					log.debug("Passing textPath to single threaded FileFinder...");
					FileFinder.findAndInput(textPath, indexPath, (WordIndex) index, isDirectory);
				} catch (IOException e) {
					System.out.println("Could not walk file path!");
					log.catching(e);
				}
			}
		}

		if (flags.hasFlag("-index")) {
			indexPath = flags.getPath("-index", indexPath);
			log.debug("Index flag found");

			try {
				log.debug("Passing index to PrettyJsonWriter...");
				PrettyJsonWriter.writeIndex(index, indexPath, 0);
			} catch (IOException e) {
				System.out.println("Could not write index to path: " + indexPath.toString());
				log.catching(e);
			}
		}

		Path countsPath = Path.of("counts.json"); // default path
		if (flags.hasFlag("-counts")) {
			countsPath = flags.getPath("-counts", countsPath);
			log.debug("Counts flag found");
			try {
				log.debug("Passing countsPath to PrettyJson Writer");
				PrettyJsonWriter.writeObject(((WordIndex) index).getWordCounts(), countsPath);
			} catch (IOException e) {
				System.out.println("Could not write counts to path: " + countsPath.toString());
				log.catching(e);
			}
		}

		Path queryPath = null;
		Path resultsPath = Path.of("results.json");
		TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults = new TreeMap<>();
		if (flags.hasFlag("-query") && (queryPath = flags.getPath("-query")) != null) {
			log.debug("Query flag found");
			boolean exact = false;
			if (flags.hasFlag("-exact")) {
				exact = true;
				log.debug("Exact flag found");
			}

			if(multithreaded) {
				log.debug("Passing queryPath to MultithreadedWordSearcher...");
				MultithreadedWordSearcher.search(queryPath, (ThreadSafeIndex) index, searchResults, exact, threadCount);
			} else {
				log.debug("Cleaning query, Searching index, and putting searchResults...");
				try (BufferedReader reader = Files.newBufferedReader(queryPath, UTF_8)) {
					while (reader.ready()) {
						TreeSet<String> cleanedQuery = WordCleaner.uniqueStems(reader.readLine());
						if (cleanedQuery.size() > 0) {
							var result = WordSearcher.search(cleanedQuery, (WordIndex) index, exact);
							String joinedQuery = String.join(" ", cleanedQuery);
							searchResults.put(joinedQuery, result);
						}
					}
				} catch (IOException e) {
					System.out.println("Something went wrong processing -query");
					log.catching(e);
				}
			}
		} else if (flags.hasFlag("-query")&& queryPath == null) {
			System.out.println("please specify a path to go along with the -query flag");
			log.error("No path was found after -query flag");
		}

		if (flags.hasFlag("-results")) {
			resultsPath = flags.getPath("-results", resultsPath);
			log.debug("Results flag found");
			try {
				log.debug("Passing results to PrettyJsonWriter...");
				PrettyJsonWriter.writeResults(searchResults, resultsPath, 0);
			} catch (IOException e) {
				System.out.println("Error writing results to path: " + resultsPath);
				log.catching(e);
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}
