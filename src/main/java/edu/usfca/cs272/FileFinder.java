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
	 * @param isDirectory indicates whether the textPath points to a directory or not
	 * @throws IOException when an IO error occurs
	 */
	public static void findAndInput(Path textPath, Path indexPath, WordIndex index, boolean isDirectory) throws IOException {
		if (Files.isDirectory(textPath)) {
			try (Stream<Path> files = Files.walk(textPath)) {
				List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());
				Collections.sort(paths);
				for (int i = 0; i < paths.size(); i++) {
					findAndInput(paths.get(i), indexPath, index, isDirectory);
				}
			}
		} else if (Files.isReadable(textPath)) {
			String extension = fileExtension(textPath);
			extension = extension.toLowerCase();
			if (isDirectory && (extension.equals("txt") || extension.equals("text"))) {
				inputFile(textPath, index);
			}
			if (!isDirectory) {
				inputFile(textPath, index);
			}
		} else {
			System.out.println("invalid path");
			return;
		}
	}

}
