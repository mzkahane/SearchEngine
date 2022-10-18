package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Matthew Kahane
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver {

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
		Path indexPath = Path.of("index.json"); // default path
		Path textPath = null;
		boolean isDirectory  = false;

		if (flags.hasFlag("-text") && (textPath = flags.getPath("-text")) != null) {
			if (Files.isDirectory(textPath)) {
				isDirectory = true;
			}

			try {
				FileFinder.findAndInput(textPath, indexPath, index, isDirectory);
			} catch (IOException e) {
				System.out.println("Could not walk file path!");
			}
		} else if (textPath == null) {
			try(BufferedWriter writer = Files.newBufferedWriter(indexPath, UTF_8)) {
				writer.write("[]");
				return;
			} catch (IOException e) {
				System.out.println("Could not create writer!");
			}
		}

		if (flags.hasFlag("-index")) {
			indexPath = flags.getPath("-index", indexPath);

			try {
				PrettyJsonWriter.writeIndex(index, indexPath, 0);
			} catch (IOException e) {
				System.out.println("Could not write to this path.");
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}
