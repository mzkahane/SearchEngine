package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A thread-safe version of {@link WordIndex}
 *
 * @author Matthew Kahane
 *
 */
public class ThreadSafeIndex extends WordIndex{

	/** Lock used for reading and writing to the index */
	private final ReadWriteLock lock;

	/** Lock used for reading and writing to the counts map in the index */
	private final ReadWriteLock countsLock;

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
	 * Initializes this ThreadSafeIndex map
	 * @param threadCount the number of threads to start the WorkQueue with
	 */
	public ThreadSafeIndex(int threadCount) {
		this.index = new TreeMap<>();
		this.counts = new TreeMap<>(Comparator.naturalOrder());
		this.lock = new ReadWriteLock();
		this.countsLock = new ReadWriteLock();
	}

	@Override
	public void add (String word, Path location, ArrayList<Integer> positions) {
		lock.write().lock();
		super.add(word, location, positions);
		lock.write().unlock();
	}

	@Override
	public void add (String word, Path location, Integer position) {
		lock.write().lock();
		super.add(word, location, position);
		lock.write().unlock();
	}

	@Override
	public int size() {
		lock.read().lock();
		int size = super.size();
		lock.read().unlock();
		return size;
	}

	@Override
	public int size(String word) {
		lock.read().lock();
		int size = super.size(word);
		lock.read().unlock();
		return size;
	}

	@Override
	public int size(String word, Path location) {
		lock.read().lock();
		int size = super.size(word, location);
		lock.read().unlock();
		return size;
	}

	@Override
	public boolean has(String word) {
		lock.read().lock();
		boolean found = super.has(word);
		lock.read().unlock();
		return found;
	}

	@Override
	public boolean has(String word, Path location) {
		lock.read().lock();
		boolean has = super.has(word, location);
		lock.read().unlock();
		return has;
	}

	@Override
	public boolean has(String word, Path location, Integer position) {
		lock.read().lock();
		boolean has = super.has(word, location, position);
		lock.read().unlock();
		return has;
	}

	@Override
	public Collection<String> view() {
		lock.read().lock();
		var view = super.view();
		lock.read().unlock();
		return view;
	}

	@Override
	public Collection<Path> view(String word) {
		lock.read().lock();
		var view = super.view(word);
		lock.read().unlock();
		return view;
	}

	@Override
	public Collection<Integer> view(String word, Path location) {
		lock.read().lock();
		var view = super.view(word, location);
		lock.read().unlock();
		return view;
	}

	@Override
	public TreeMap<Path, ? extends Collection<? extends Number>> get(String word) {
		lock.read().lock();
		var get = super.get(word);
		lock.read().unlock();
		return get;
	}

	@Override
	public ArrayList<Integer> get(String word, Path location) {
		lock.read().lock();
		var get = super.get(word, location);
		lock.read().unlock();
		return get;
	}

	@Override
	public Set<String> getKeys() {
		lock.read().lock();
		var keys = super.getKeys();
		lock.read().unlock();
		return keys;
	}

	@Override
	public void addWordCount(String location, int numWords) {
		countsLock.write().lock();
		super.addWordCount(location, numWords);
		countsLock.write().unlock();
	}

	@Override
	public int getWordCount(String location) {
		countsLock.read().lock();
		int count = super.getWordCount(location);
		countsLock.read().unlock();
		return count;

	}

	@Override
	public Map<String, Integer> getWordCounts() {
		countsLock.read().lock();
		var out = super.getWordCounts();
		countsLock.read().unlock();
		return out;
	}

}
