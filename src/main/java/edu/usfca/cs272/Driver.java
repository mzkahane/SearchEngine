package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * Inputs the contents of a file to the word index
	 * @param path the path where the file is found
	 * @param index the index to input the contents into
	 * @throws IOException if an IO error occurs
	 */
	public static void inputFile(Path path, WordIndex index) throws IOException {
		String text = Files.readString(path, UTF_8);
		ArrayList<String> cleanedWords = WordCleaner.listStems(text);

		for (int i = 0; i < cleanedWords.size(); i++) {
			index.add(cleanedWords.get(i), path, i+1);
		}
	}

	/**
	 * Finds the file specified by the path, or walks through all of the files in the
	 * directory if the path points to one
	 *
	 * @param textPath the path to find the files
	 * @param indexPath the path to output the index
	 * @param index the index to parse the files into
	 * @throws IOException when an IO error occurs
	 */
	public static void findAndInput(Path textPath, Path indexPath, WordIndex index) throws IOException {
		if (Files.isDirectory(textPath)) {
			try (Stream<Path> files = Files.walk(textPath)) {
				List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());
				Collections.sort(paths);
				for (int i = 0; i < paths.size(); i++) {
					findAndInput(paths.get(i), indexPath, index);
				}
			}
		} else if (Files.isReadable(textPath)) {
			inputFile(textPath, index);
			//PrettyJsonWriter.writeIndex(index, indexPath, 0);
		} else {
			System.out.println("invalid path");
			return;
		}
	}

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 * @throws IOException if an IO error occurs
	 */
	public static void main(String[] args) throws IOException {
		// store initial start time
		Instant start = Instant.now();

		System.out.println(Arrays.toString(args));

		ArgumentParser flags = new ArgumentParser();
		flags.parse(args);
		if (flags.viewFlags().size() == 0) {
			return;
		}

		WordIndex index = new WordIndex();
		Path indexPath = Path.of("index.json");
		Path textPath = null;
		boolean indexFound = false;

		for (String flag : flags.viewFlags()) {
			if (Objects.equals(flag, "-index")) {
				indexPath = flags.getPath(flag, indexPath);
				indexFound = true;
			}
			if (Objects.equals(flag, "-text")) {
				textPath = flags.getPath(flag);
			}
			if (!Objects.equals(flag, "-index") && !Objects.equals(flag, "-text")) {
				throw new UnsupportedOperationException("Flag not supported!");
			}
		}

		if (textPath == null) {
			BufferedWriter writer = Files.newBufferedWriter(indexPath, UTF_8);
			writer.write("[]");
			return;
		}

		if (!indexFound) {
			return;
		}

		findAndInput(textPath, indexPath, index);

		//System.out.println(indexPath.toAbsolutePath().toString());
		PrettyJsonWriter.writeIndex(index, indexPath, 0);

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

}
