	package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	public static boolean isTextFile(Path textPath) {
		String path = textPath.toString();

		int i = path.lastIndexOf('.');
		String extension =  i > 0 ? path.substring(i+1) : "";
		extension = extension.toLowerCase();

		return (extension.equals("txt") || extension.equals("text"));

	}

	/**
	 * Finds the file specified by the path, or walks through all of the files in the
	 * directory if the path points to one
	 *
	 * @param textPath the path to find the files
	 * @param index the index to parse the files into
	 * @param strictTextTest indicates whether the textPath points to a directory or not
	 * @throws IOException when an IO error occurs
	 */
	public static void findAndInput(Path textPath, WordIndex index, boolean strictTextTest) throws IOException {
		if (Files.isDirectory(textPath)) {
			try (Stream<Path> files = Files.walk(textPath)) {
				List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());
				Collections.sort(paths);
				for (int i = 0; i < paths.size(); i++) {
					findAndInput(paths.get(i), index, strictTextTest);
				}
			}
		} else if (Files.isReadable(textPath)) {
			if (strictTextTest && isTextFile(textPath)) {
				index.inputFile(textPath);
			}
			if (!strictTextTest) {
				index.inputFile(textPath);
			}
		} else {
			System.out.println("invalid path");
			return;
		}
	}

}
