package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	private final TreeMap<String, TreeMap<Path,ArrayList<Integer>>> index;

	/**
	 * Initializes this WordIndex map
	 */
	public WordIndex() {
		this.index = new TreeMap<>();
	}

	@Override
	public void add(String word, Path location, ArrayList<Integer> positions) {
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
		return index.get(word).size();
	}

	@Override
	public int size(String word, Path location) {
		return index.get(word).get(location).size();
	}

	@Override
	public boolean has(String word) {
		return index.containsKey(word);
	}

	@Override
	public boolean has(String word, Path location) {
		if (has(word)) {
			return index.get(word).containsKey(location);
		}
		return false;
	}

	@Override
	public boolean has(String word, Path location, Integer position) {
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
		if (!has(word, location)) {
			ArrayList<Integer> empty = new ArrayList<Integer>();
			return empty;
		}
		return null;
	}

	@Override
	public TreeMap<Path, ? extends Collection<? extends Number>> get(String key) {
		TreeMap<Path, ArrayList<Integer>> out = new TreeMap<Path, ArrayList<Integer>>(index.get(key));
		return out;
	}

	@Override
	public ArrayList<Integer> get(String word, Path location) {
		return (ArrayList<Integer>) List.copyOf(index.get(word).get(location));
	}
}