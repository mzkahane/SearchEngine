package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * An index to store words and the files those words are found in as well as
 * their position(s) in those files.
 *
 * @param <E> the type of location stored
 *
 * @author Matthew Kahane
 */
public interface InvertedIndex<E> {

	/**
	 * Adds a word and its positions within a specific location to the index
	 *
	 * @param word word to be added.
	 * @param location HashMap of locations/files the word is found in.
	 * @param positions the positions in the location/file the word is found at.
	 */
	public void add(String word, E location, ArrayList<Integer> positions);

	/**
	 * Adds a single position for a word and location. adds the word and/or
	 * location if they do not already exist.
	 *
	 * @param word word found at the given position in the given location
	 * @param location location in which the word is found at the given position
	 * @param position position of the word in the given location.
	 */
	public void add(String word, Path location, Integer position);

	/**
	 * Returns the number of words stored in the index
	 *
	 * @return 0 if empty, otherwise the number of words in the index.
	 */
	public int size();

	/**
	 * Returns the number of locations stored for a given word.
	 *
	 * @param word the word to return the locations of.
	 * @return 0 if the word is not in the index or has no locations, otherwise
	 * 	the number of locations stored for that word.
	 */
	public int size(String word);

	/**
	 * Returns the number of positions stored for a given word at a given location
	 *
	 * @param word the word to return the positions of
	 * @param location the location in which those positions are found
	 * @return 0 if the word is not in the index or if the location is not
	 * 	found for that word, otherwise the number of positions stored for that
	 * 	word.
	 */
	public int size(String word, Path location);

	/**
	 * Determines whether the word is stored in the index.
	 *
	 * @param word the word to lookup
	 * @return {@true} if the word is stored in the index
	 */
	public boolean has(String word);

	/**
	 * Determines whether the word is stored in the index and the location is
	 * stored for that word.
	 *
	 * @param word the word to lookup
	 * @param location the location for that word to lookup
	 * @return {@true} if the word and location is stored in the index
	 */
	public boolean has(String word, E location);

	/**
	 * Determines whether the word is stored in the index, the location stored
	 * for that word, and the position for that location.
	 *
	 * @param word the word to lookup
	 * @param location the location for that word to lookup
	 * @param position the position in that location to lookup
	 * @return {@true} if the word, location, and position is stored in the
	 * 	index
	 */
	public boolean has(String word, E location, Integer position);

	/**
	 * Returns an unmodifiable view of the words stored in the index.
	 *
	 * @return an unmodifiable view of the words stored in the index
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<String> view();

	/**
	 * Returns an unmodifiable view of the locations stored in the index for a
	 * given word.
	 *
	 * @param word the word to view the locations of
	 * @return an unmodifiable view of the locations stored in the index for a
	 * 	given word
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<E> view(String word);

	/**
	 * Returns an unmodifiable view of the positions stored in the index for a
	 * given location of a given word.
	 *
	 * @param word word to view the locations of
	 * @param location location to view the positions of
	 * @return an unmodifiable view of the positions stored in the index for a
	 * 	given location of a given word
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<Integer> view(String word, Path location);

	/**
	 * Returns a copy of the location-position map for a word in the index
	 * @param word the word to get the location-position map for
	 * @return a copy of the location-position map for the word in the index
	 */
	public Map<Path, ? extends Collection<? extends Number>> get(String word);

	/**
	 * Returns a copy of the positions for a word in a given location in the index.
	 * If either the word or the location do not exist in the index, {@code null} is
	 * returned.
	 * @param word the word to get the positions of
	 * @param location the specific location of the word to get the positions of
	 * @return a copy of the positions for a word in a given location in the index
	 */
	public ArrayList<Integer> get(String word, Path location);

	/**
	 * Returns a copy of the keyset of the index
	 *
	 * @return a copy of the keyset of the index
	 */
	Set<String> getKeys();

}
