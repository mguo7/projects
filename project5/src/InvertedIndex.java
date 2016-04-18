import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * The program will find all words' locations and the file name/path which
 * contains these words This program uses two TreeMaps and one ArrayList to
 * store all information of these words.
 * 
 * @author miaoguo
 */
public class InvertedIndex {

	private final MultiReaderLock lock;

	/**
	 * This TreeMap is used to store the mapping from words to the documents
	 * (and position within those documents) where those words were found.
	 */
	 private final TreeMap<String, TreeMap<String, ArrayList<Integer>>> wordmap;

	public InvertedIndex() {
		lock = new MultiReaderLock();
		wordmap = new TreeMap<String, TreeMap<String, ArrayList<Integer>>>();
	}

	/**
	 * Find each word positions and the file which the word is found.The inner
	 * TreeMap is used to store the file name/path and locations of a word.
	 * 
	 * @param word
	 *            of a word from one file
	 * @param position
	 *            of the word's position in the file
	 * @param path
	 *            of the file's name/path
	 */
	public void addWords(String word, int position, String path) {

		lock.lockWrite();

		// if the word is a new word, the program will create a new inner
		// TreeMap
		// the inner TreeMap will be put into outer TreeMap
		if (!wordmap.containsKey(word)) {

			ArrayList<Integer> location = new ArrayList<Integer>();
			location.add(position);

			TreeMap<String, ArrayList<Integer>> pathloc = new TreeMap<String, ArrayList<Integer>>();

			pathloc.put(path, location);
			wordmap.put(word, pathloc);

			// if the word has been in the map and it is from the same file
			// the program will just add a new location into the ArrayList.

		} else if (wordmap.get(word).containsKey(path)) {

			// wordmap.get(word).get(path) is the ArrayList-location.
			wordmap.get(word).get(path).add(position);

			// if the word has been in the Map but it is from a different file
			// the program will create a new ArrayList for storing its location
			// the file name/path and the location will be put into inner
			// TreeMap of the word
		} else if (!wordmap.get(word).containsKey(path)) {

			ArrayList<Integer> location = new ArrayList<Integer>();
			location.add(position);

			// wordmap.get(word) is the inner TreeMap.
			wordmap.get(word).put(path, location);
		}

		lock.unlockWrite();

	}

	/**
	 * Find all words start with the queries. The program will get the frequency
	 * of the query(queries) that appear in each file. The program will also
	 * find the first location of a word that start with a query in the file.
	 * 
	 * @param queryList of a ArrayList that contains queries from one line in search
	 *        file.
	 * @return an ArrayList contains one line of queries in a file.
	 */
	public ArrayList<SearchResults> searchIndex(ArrayList<String> queryList) {

		lock.lockRead();

		HashMap<String, SearchResults> results = new HashMap<String, SearchResults>();

		for (String query : queryList) {

			for (String word = wordmap.ceilingKey(query); word != null; word = wordmap
					.higherKey(word)) {

				if (!word.startsWith(query)) {

					break;
				}

				// for every word that starts with the query
				else {

					for (String path : wordmap.get(word).keySet()) {

						// if the word comes from a different file
						// build a new ArrayList and add frequency & location
						// into this ArrayList
						// location is the first location of the word(s) in a
						// file which contains the query.
						if (!results.containsKey(path)) {
							int frequency = wordmap.get(word).get(path).size();
							int loc = wordmap.get(word).get(path).get(0);

							SearchResults result = new SearchResults(path,
									frequency, loc);
							results.put(path, result);

							// if the word comes from the same file
							// get the new frequency and new location
						} else if (results.containsKey(path)) {

							int frequency = wordmap.get(word).get(path).size();
							int loc = wordmap.get(word).get(path).get(0);

							results.get(path).update(frequency, loc);

						}
					}// out of path
				}
			} // out of word;
		} // out of query

		lock.unlockRead();

		ArrayList<SearchResults> resultList = new ArrayList<SearchResults>();
		resultList.addAll(results.values());
		Collections.sort(resultList);

		return resultList;

	}

	/**
	 * Print out all information into a new text file. The output format should
	 * meet the requirement of the Project.
	 * 
	 * @param filename of a output file name which the user want to create.
	 * @throws IOException
	 */
	public void printFile(String filename) {

		lock.lockRead();

		Path file = Paths.get(filename);
		Charset charset = Charset.forName("UTF-8");

		try (BufferedWriter output = Files.newBufferedWriter(file, charset)) {

			// Print out the words
			for (String word : wordmap.keySet()) {

				output.write(word);
				output.newLine();
				// Print out the dirs of the files which contain the word
				for (String path : wordmap.get(word).keySet()) {

					output.write("\"" + path + "\"");
					// Print out the positions in each file of the word
					for (int i = 0; i < wordmap.get(word).get(path).size(); i++) {

						output.write(", " + wordmap.get(word).get(path).get(i));

					}// out of i
					output.newLine();
				} // out of path
				output.newLine();
			}// out of word
		} catch (Exception e) {
			// "Error printing to file " + filename
			System.err.println("Errors on printing file: " + filename);

		}

		lock.unlockRead();
	}
/**
 * Get the final index for all local index.
 * 
 * @param other of local index contains the results of InvertedIndex
 */
	public void addAll(InvertedIndex other) {

		lock.lockWrite();
		for (String key : other.wordmap.keySet()) {
			if (!this.wordmap.containsKey(key)) {
				this.wordmap.put(key, other.wordmap.get(key));
			} else {
				// fill in
				for (String path : other.wordmap.get(key).keySet()) {

					if (!this.wordmap.get(key).containsKey(path)) {

						this.wordmap.get(key).put(path,
								other.wordmap.get(key).get(path));
					} else {

						for (int location : other.wordmap.get(key).get(path)) {

							if (!this.wordmap.get(key).get(path)
									.contains(location)) {

								this.wordmap.get(key).get(path).add(location);
							}

						} // out of location

					}
				} // out of path
			}

		} // out of key

		lock.unlockWrite();
	}

}