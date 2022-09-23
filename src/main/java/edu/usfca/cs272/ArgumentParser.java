package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.(test push)
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class ArgumentParser {
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final HashMap<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentParser() {
		this.map = new HashMap<>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into
	 * flag/value pairs where possible. Some flags may not have associated values.
	 * If a flag is repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentParser(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a
	 * flag if it is a dash "-" character followed by any character that is not a
	 * digit or whitespace. For example, "-hello" and "-@world" are considered
	 * flags, but "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 *
	 * @see String#startsWith(String)
	 * @see String#length()
	 * @see String#codePointAt(int)
	 * @see Character#isDigit(int)
	 * @see Character#isWhitespace(int)
	 */
	public static boolean isFlag(String arg) {
		if(arg != null && arg.startsWith("-") && arg.length() > 1) {
			Character first = (char) arg.codePointAt(1);
			// Make sure what follows '-' is not a digit or whitespace
			if(Character.isDigit(first) || Character.isWhitespace(first)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may
	 * not have associated values. If a flag is repeated, its value will be
	 * overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args) {
		// if there is only one argument that is a flag, put it with a null value
		if(args.length == 1) {
			if(isFlag(args[0])) {
				map.put(args[0], null);
			}
			return; // if the one argument is not a flag, do nothing
		}
		int j = 1; // current value pointer
		for(int i = 0; i <= args.length-1; i++) { // current key pointer
			String key = args[i];
			String value = args[j];
			if(isFlag(key)) {
				if(!map.containsKey(key)) {
					if(isValue(value)) {
						map.put(key, value); // if the key is new and the value is valid, insert
						i++;				 // ... and move the pointers an extra index
						j++;
					} else {
						map.put(key, null); // if not pointing at a valid value, insert null instead.
					}
				} else {
					if(isValue(value)) {
						map.replace(key, value);
						i++;
						j++;
					} else {
						map.replace(key, null);
					}
				}
			}
			if(j < args.length-1) { //increments the current value pointer to stay one ahead of i
				j++;
			}
		}
		return;
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		int numFlags = 0;

		// All of the keys are flags, so the number of that set is the number of flags
		Set<String> flags = map.keySet();
		numFlags += flags.size();

		return numFlags;
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		if (!isFlag(flag)) {
			throw new IllegalArgumentException("Argument is not a flag");
		}

		if(flag != null && map.containsKey(flag)) {
			return true;
		}
		return false;
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		if (!isFlag(flag)) {
			throw new IllegalArgumentException("Argument is not a flag");
		}

		if(map.containsKey(flag)) {
			if(map.get(flag) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the backup
	 *   value if there is no mapping
	 */
	public String getString(String flag, String backup) {
		if (!isFlag(flag)) {
			throw new IllegalArgumentException("Argument is not a flag");
		}

		if(map.containsKey(flag)) { // check to see if the flag exists in the map
			if(map.get(flag) != null) { // check that the value is not empty/null
				return map.get(flag);
			}
		}
		return backup;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag) {
		if (!isFlag(flag)) {
			throw new IllegalArgumentException("Argument is not a flag");
		}

		return getString(flag, null);
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   backup value if there is no valid mapping
	 *
	 * @see Path#of(String, String...)
	 */
	public Path getPath(String flag, Path backup) {
		if(map.get(flag) == null) {
			return backup;
		}
		String file = map.get(flag);
		return Path.of(file);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path},
	 * or {@code null} if unable to retrieve this mapping (including being unable
	 * to convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 *
	 * @see #getPath(String, Path)
	 */
	public Path getPath(String flag) {
		return getPath(flag, null);
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup
	 *   value if there is no valid mapping
	 *
	 * @see Integer#parseInt(String)
	 */
	public int getInteger(String flag, int backup) {
		if (!isFlag(flag)) {
			throw new IllegalArgumentException("Argument is not a flag");
		}

		if(map.get(flag) != null) {
			int val;

			try { // try to convert the value from string to int, if it fails, then its not a number
				val = Integer.parseInt(map.get(flag));
			} catch (NumberFormatException e) {
				val = 0; // .. in that case, return the default 0
			}

			return val;
		}
		return backup;
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the
	 * value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is
	 *   no valid mapping
	 *
	 * @see #getInteger(String, int)
	 */
	public int getInteger(String flag) {
		return getInteger(flag, 0);
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * Returns an unmodifiable view of the flags(keys) stored in the argument parser
	 *
	 * @return an unmodifiable view of the flags(keys) stored in the argument parser
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public List<String> viewFlags() {
		return List.copyOf(map.keySet());
	}

	/**
	 * Returns an unmodifiable view of the values stored in the argument parser
	 *
	 * @return an unmodified view of the values stored in the argument parser
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public List<String> viewValues() {
		return List.copyOf(map.values());
	}

}
