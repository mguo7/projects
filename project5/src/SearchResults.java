/**
 * This class will build <SearchResults> and update/sort the results.
 * 
 * @author Miao Guo.
 */
public class SearchResults implements Comparable<SearchResults> {

	private final String file;
	private int frequency;
	private int location;

	/**
	 * There are three elements in <SearchResults> : file, frequency and
	 * location. The class will initial these elements by the results from Partial
	 * Search.
	 * 
	 * @param file of files in a given directory.
	 * @param frequency of times that word(s) appear in a file
	 * @param locatoin of word which appear in a file
	 */
	public SearchResults(String file, int frequency, int location) {

		this.file = file;
		this.frequency = frequency;
		this.location = location;

	}

	/**
	 * Update the SearchResults with new a frequency and a new location.
	 * 
	 * @param newFrequency
	 *            will be added into total frequency in order to combine the
	 *            results of different queries.
	 * @param newLocation
	 *            will replace the old location if it is smaller than the old
	 *            one.
	 */
	public void update(int newFrequency, int newLocation) {
		this.frequency += newFrequency;

		if (newLocation < this.location) {
			this.location = newLocation;
		}
	}

	@Override
	public String toString() {
		// return the search result in the format expected
		// "path, frequency, position"

		String result = "<a href=" + this.file + ">" + this.file + "</a>" + ", " + this.frequency + ", "
				+ this.location;
		return result;
	}

	/**
	 * Sorted by: Frequency: Files where the query word(s) are more frequent
	 * should be ranked above others. Position : For files that have the same
	 * frequency: files where the words appear in earlier positions should be
	 * ranked above others.
	 */
	@Override
	public int compareTo(SearchResults o) {

		// compare by frequency, if equal
		// compare by position, if equal
		// compare by path

		if (this.frequency != o.frequency) { 

			return o.frequency - this.frequency;

		} else if (this.location != o.location) {

			return this.location - o.location;
		} else {

			return String.CASE_INSENSITIVE_ORDER.compare(this.file, o.file);
		}

	}

}