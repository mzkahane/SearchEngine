package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Finds files given a Path and inputs them into an index in a thread safe way.
 *
 * See {@link FileFinder}
 *
 * @author Matthew Kahane
 */
public class ThreadSafeFileFinder extends FileFinder {

	/*  XXX QUESTIONS
	 * - CR todos affect the implementation of things in PR3, which first?
	 *
	 * - less copy-paste, more only task and queue, calling super for the rest?
	 *
	 * - ThreadSafeIndex lock fields not used?
	 *
	 * - Logger set up? turn off for individual classes?
	 *
	 * separate loggers per class in the xml, level set to off
	 */


	/**
	 * Finds the file specified by the path, or walks through all the files in the
	 * directory if the path points to one, and creates a task for the work queue
	 * to input those files into the index.
	 *
	 * @param textPath a path pointing to the file/directory to be indexed
	 * @param index the index to parse the files into
	 * @param isDirectory indicates whether or not textPath points to a directory
	 * @param threadCount the number of threads to start the work queue with
	 */
	public static void findAndInput(Path textPath, ThreadSafeIndex index, boolean isDirectory, int threadCount) {
		WorkQueue queue = new WorkQueue(threadCount);

		if (isDirectory) {
			try (Stream<Path> files = Files.walk(textPath)) {
				List<Path> paths = files.filter(Files::isRegularFile).collect(Collectors.toList());
				Collections.sort(paths);
				for (int i = 0; i < paths.size(); i++) {
					queue.execute(new Task(paths.get(i), index));
				}
			} catch (IOException e) {
				System.out.println("Could not walk text path : " + textPath);
				// TODO log.catching
			}
		} else if (Files.isReadable(textPath)) {
			queue.execute(new Task(textPath, index));
		}

		queue.join();
	}

	/**
	 * Creates tasks to be run by workers in the queue
	 *
	 * @author Matthew Kahane
	 */
	private static class Task implements Runnable {

		/** the text path to input into the index */
		private Path textPath;

		/** the index to input the files into */
		private ThreadSafeIndex index;

		/**
		 * Initializes the task
		 *
		 * @param input the path to input to the index
		 * @param index the index to input the file to
		 */
		public Task(Path input, ThreadSafeIndex index) {
			this.textPath = input;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				FileFinder.inputFile(textPath, index);
			} catch (IOException e) {
				System.out.println("Could not read text file at : " + textPath);
				// TODO log.catching
			}
		}

	}
}
