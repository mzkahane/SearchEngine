package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A type of {@link InvertedIndex} that indexes the {@link Path} to the files, as well
 * as the positions within each file, that the word can be found.
 *
 * @author Matthew Kahane
 *
 */
public class WordIndex implements InvertedIndex<Path> {

	/**
	 * Index to store the given data in.
	 *
	 * Outer HashMap maps the word to the inner HashMap. Inner HashMap maps
	 * each path to the positions of that word found in the file at that path.
	 */
	private final TreeMap<String, TreeMap<Path, ArrayList<Integer>>> index;

	/**
	 * Map to store the word counts of the files in the index
	 *
	 * The key is the path of the file.
	 * The value is the word count of that file
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * Initializes this WordIndex map
	 */
	public WordIndex() {
		this.index = new TreeMap<>();
		this.counts = new TreeMap<>(Comparator.naturalOrder());
	}

	@Override
	public void add(String word, Path location, ArrayList<Integer> positions) {
		// @TODO take a look at putIfAbsent
		// https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/util/Map.html#putIfAbsent(K,V)
		//
		// index.putIfAbsent(word, new TreeMap());
		// index.get(word).putIfAbsent(location, new ArrayList());
		// index.get(word).get(location).addAll(positions);
		if (!index.containsKey(word)) {
			var temp = new TreeMap<Path, ArrayList<Integer>>();
			temp.put(location, positions);
			index.put(word, temp);
		} else if (!index.get(word).containsKey(location)) {
			index.get(word).put(location, positions);
		} else {
			index.get(word).get(location).addAll(positions);
		}

	}

	@Override
	public void add(String word, Path location, Integer position) {
		// @TODO take a look at putIfAbsent
		if (!index.containsKey(word)) {
			var temp = new TreeMap<Path, ArrayList<Integer>>();
			temp.put(location, new ArrayList<Integer>());

			ArrayList<Integer> array = temp.get(location);
			array.add(position);

			index.putIfAbsent(word, temp);
		} else if (!index.get(word).containsKey(location)) {
			ArrayList<Integer> array = new ArrayList<Integer>();
			array.add(position);
			index.get(word).putIfAbsent(location, array);
		} else {
			index.get(word).get(location).add(position);
		}
	}

	@Override
	public int size() {
		return index.size();
	}

	@Override
	public int size(String word) {
		// @TODO possible NullPointerException, fix using has()
		return index.get(word).size();
	}

	@Override
	public int size(String word, Path location) {
		// @TODO possible NullPointerException, fix using has()
		return index.get(word).get(location).size();
	}

	@Override
	public boolean has(String word) {
		return index.containsKey(word);
	}

	@Override
	public boolean has(String word, Path location) {
		// @TODO try ternary
		if (has(word)) {
			return index.get(word).containsKey(location);
		}
		return false;
	}

	@Override
	public boolean has(String word, Path location, Integer position) {
		// @TODO try ternary
		if (has(word, location)) {
			return index.get(word).get(location).contains(position);
		}
		return false;
	}

	@Override
	public Collection<String> view() {
		return List.copyOf(index.keySet());
	}

	@Override
	public Collection<Path> view(String word) {
		if (index.get(word) == null) {
			ArrayList<Path> empty = new ArrayList<Path>();
			return empty;
		}
		return List.copyOf(index.get(word).keySet());
	}

	@Override
	public Collection<Integer> view(String word, Path location) {
		// @TODO does this always return either empty list or null
		if (!has(word, location)) {
			ArrayList<Integer> empty = new ArrayList<Integer>();
			return empty;
		}
		return null;
	}

	@Override
	public TreeMap<Path, ? extends Collection<? extends Number>> get(String word) {
		if (index.get(word) == null) {
			return null;
		}
		// @TODO (optional) - technically this is a shallow copy
		// to properly deep copy, you have to iterate over all your values and clone those lists as well
		TreeMap<Path, ArrayList<Integer>> out = new TreeMap<Path, ArrayList<Integer>>(index.get(word));
		return out;
	}

	@Override
	public ArrayList<Integer> get(String word, Path location) {
		// @TODO NullPointerException possibility - we should specify in the Javadoc comments what's the intended behavior if index.get(word) returns null. Should it throw or just return null?
		return (ArrayList<Integer>) List.copyOf(index.get(word).get(location));
	}

	@Override
	public Set<String> getKeys() {
		return Set.copyOf(index.keySet());
	}

	/**
	 * Adds the location and the number of words it contains to the count Map
	 *
	 * @param location the location of the file being counted
	 * @param numWords the number of words in the file
	 */
	public void addWordCount(String location, int numWords) {
		counts.put(location, numWords);
	}

	/**
	 * Gets the word count of a specific file
	 *
	 * @param location the location of the file to get the word count of
	 * @return the word count of the file at the location given
	 */
	public int getWordCount(String location) {
		int out = counts.get(location);
		return out;
	}

	/**
	 * Returns an unmodifiable view of the counts map
	 *
	 * @return An unmodifiable view of the counts map
	 */
	public Map<String, Integer> getWordCounts() {
		return Collections.unmodifiableSortedMap(counts);
	}
}
