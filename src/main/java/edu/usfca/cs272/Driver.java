package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Matthew Kahane
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver {

	/** Default path to output the index */
	private static Path DEFAULT_INDEX_PATH = Path.of("index.json");

	/** Default path to output the counts file */
	private static Path DEFAULT_COUNTS_PATH = Path.of("counts.json");

	/** Default path to output the results file */
	private static Path DEFAULT_RESULTS_PATH = Path.of("results.json");

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

		WordIndex index = new WordIndex();

		Path textPath = null;
		// TODO: should be able to remove this
		// XXX ^ removing this causes problems with .md files, sometimes they should be included, others not
		boolean isDirectory  = false;
		if (flags.hasFlag("-text") && (textPath = flags.getPath("-text")) != null) {
			// TODO: should be able to remove this
			if (Files.isDirectory(textPath)) {
				isDirectory = true;
			}

			try {
				FileFinder.findAndInput(textPath, index, isDirectory);
			} catch (IOException e) {
				System.out.println("Could not walk file path!");
			}
		}

		Path indexPath = DEFAULT_INDEX_PATH;
		if (flags.hasFlag("-index")) {
			indexPath = flags.getPath("-index", indexPath);

			try {
				PrettyJsonWriter.writeIndex(index, indexPath, 0);
			} catch (IOException e) {
				System.out.println("Could not write index to path: " + indexPath.toString());
			}
		}

		Path countsPath = DEFAULT_COUNTS_PATH;
		if (flags.hasFlag("-counts")) {
			countsPath = flags.getPath("-counts", countsPath);
			try {
				PrettyJsonWriter.writeObject(index.getWordCounts(), countsPath);
			} catch (IOException e) {
				System.out.println("Could not write counts to path: " + countsPath.toString());
			}
		}

		Path queryPath = null;
		Path resultsPath = DEFAULT_RESULTS_PATH;
		TreeMap<String, ArrayList<LinkedHashMap<String, String>>> searchResults = new TreeMap<>();
		if (flags.hasFlag("-query") && (queryPath = flags.getPath("-query")) != null) {
			boolean exact = flags.hasFlag("-exact") ? true : false;

			try (BufferedReader reader = Files.newBufferedReader(queryPath, UTF_8);){
				// TODO try `while((line = reader.readLine()) != null) {`
				while (reader.ready()) {
					// TODO: let's move any search functionality (e.g. parsing/cleaning/looping over lines etc) into a method in WordSearcher
					// so that the Driver can just focus on the minimal argument parsing + calling methods
					TreeSet<String> cleanedQuery = WordCleaner.uniqueStems(reader.readLine());
					if (cleanedQuery.size() > 0) {
						var result = WordSearcher.search(cleanedQuery, index, exact);
						String joinedQuery = String.join(" ", cleanedQuery);
						searchResults.put(joinedQuery, result);
					}
				}
			} catch (IOException e) {
				System.out.println("Something went wrong processing -query");
			}
		} else if (queryPath == null) {
			System.out.println("please specify a path to go along with the -query flag");
		}

		if (flags.hasFlag("-results")) {
			resultsPath = flags.getPath("-results", resultsPath);
			try {
				PrettyJsonWriter.writeNestedMap(searchResults, resultsPath, 0);
			} catch (IOException e) {
				System.out.println("Error writing results to path: " + resultsPath);
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}
