package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds files given a Path and inputs them into an index
 *
 * @author Matthew Kahane
 */
public class FileFinder {

	/**
	 * Gets the file extension for the file at a given path
	 *
	 * @param textPath the path that points to the file
	 * @return a string containing the file extension
	 */
	public static String fileExtension(Path textPath) {
		String extension = "";
		String path = textPath.toString();

		int i = path.lastIndexOf('.');
		// TODO here's another example of a ternary:
		// return i > 0 ? path.substring(i+1) : "";
		if (i > 0) {
			extension = path.substring(i+1);
		}
		return extension;
	}

	/**
	 * Inputs the contents of a file to the word index
	 * @param path the path where the file is found
	 * @param index the index to input the contents into
	 * @throws IOException if an IO error occurs
	 */
	// TODO instead of static method in FileFinder, this might be more appropriate
	// as a non-static method in WordIndex.inputFile(Path path)
	public static void inputFile(Path path, WordIndex index) throws IOException {
		String text = Files.readString(path, UTF_8);
		ArrayList<String> cleanedWords = WordCleaner.listStems(text);

		if (cleanedWords.size() > 0) {
			index.addWordCount(path.toString(), cleanedWords.size());
		}

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
	 * @param isDirectory indicates whether the textPath points to a directory or not
	 * @throws IOException when an IO error occurs
	 */
	public static void findAndInput(Path textPath, Path indexPath, WordIndex index, boolean isDirectory) throws IOException {
		// TODO: instead of isDirectory, it's really about whether to ignore file extension
		if (Files.isDirectory(textPath)) {
			try (Stream<Path> files = Files.walk(textPath)) {
				List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());
				Collections.sort(paths);
				for (int i = 0; i < paths.size(); i++) {
					findAndInput(paths.get(i), indexPath, index, isDirectory);
				}
			}
		} else if (Files.isReadable(textPath)) {
			// TODO abstract this out into isTextFile method
			String extension = fileExtension(textPath);
			extension = extension.toLowerCase();
			if (isDirectory && (extension.equals("txt") || extension.equals("text"))) {
				// TODO call index.inputFile(textPath)
				inputFile(textPath, index);
			}
			if (!isDirectory) {
				// TODO call index.inputFile(textPath)
				inputFile(textPath, index);
			}
		} else {
			System.out.println("invalid path");
			return;
		}
	}

}
