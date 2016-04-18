import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchBuilder {

	/**
	 * This class is query helper class, that is able to parse the query file
	 * into lines and lines into either an array or list of cleaned words.
	 * 
	 * @author miaoguo 
	 */
	private static Logger logger = LogManager.getLogger(SearchBuilder.class);
	private final WorkQueue workers;
	private final MultiReaderLock lock;
	LinkedHashMap<String, ArrayList<SearchResults>> results;
	private int pending;

	/**
	 * Build Partial Search and Use a work queue to search  the inverted index 
	 * from a file of multiple word queries
	 * 
	 * @param workers of a WorkQueue which contains a specific number of threads
	 */
	public SearchBuilder(WorkQueue workers) {
		this.workers = workers;
		lock = new MultiReaderLock();
		results = new LinkedHashMap<String, ArrayList<SearchResults>>();
		pending = 0;

	}

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done.
	 */
	public synchronized void finish() {

		try {
			while (pending > 0) {
				logger.debug("We are still working on Searching the words!");
				this.wait();
			}
		} catch (InterruptedException e) {

			System.out.println("Finishe interrupted.");
		}

	}

	/**
	 * Read a file that contains a list of queries.
	 * 
	 * @param queryfile
	 *            of a file that contains queries.
	 * @param queryname
	 *            of output file name for printing out results
	 */
	public void parseFile(Path qyfile, InvertedIndex index) {

		parseQuery(qyfile, index);

	}

	/**
	 * Get the queries in the file. The program will catch one line at one time
	 * and do search function. A new worker will begin to work when the program read
	 * a new line of query(s);
	 * @param queryfile
	 *            of a file that contains queries
	 */
	private void parseQuery(Path qyfile, InvertedIndex index) {

		Charset charset = Charset.forName("UTF-8");

		try (BufferedReader reader = Files.newBufferedReader(qyfile, charset)) {

			// line is one line from a file which will be caught by the program
			String line = "";

			while ((line = reader.readLine()) != null) {
				 
				lock.lockWrite();
				results.put(line, null); 
				lock.unlockWrite();
				workers.execute(new SearchWorker(line, index));

			}// out of line

			finish();
		} catch (NullPointerException ne) {
			System.err
					.println("Failed. Can not find the directory or no files in the directory");

		} catch (FileNotFoundException fn) {
			System.err.println("Failed. Can not find the directory!");

		} catch (IOException e) {
			System.err
					.println("Unexpected IO Exception on reading QueryFile : "
							+ qyfile);
		}
	}

	/**
	 * Use a work queue to search your inverted index from a file of multiple word queries. 
	 * Each worker thread should handle an individual query.
	 */
	private class SearchWorker implements Runnable {

		private String line;
		private InvertedIndex index;

		public SearchWorker(String line, InvertedIndex index) {

			this.line = line;
			this.index = index;
			incrementPending(); // pending++ if we create this worker
			logger.debug("A new worker has began to work.");
			logger.debug("Now pending is "+ pending);

		}

		/**
		 * Find all files from a provided directory and all sub directories from
		 * that directory.
		 * 
		 * @param dir
		 *            of a provided directory
		 * @param files
		 *            of the ArrayList which contains all files from a provided
		 *            directory and all sub directories
		 */
		@Override
		public void run() {

			
			// do split and do search
			buildSearch(line, index); // split one line into different
										// words(query)

			decrementPending(); // pending-- when the worker's work is done
			logger.debug("A worker's work is done");
			logger.debug("Now Pending is "+ pending);
			
		}

	}

	/**
	 * Split queries into different words by white space and add them into an
	 * ArrayList. The program will search the InvertedIndex by the queries
	 * 
	 * @param line
	 *            of one line in a file that contains queries
	 * @return 
	 */
	public ArrayList<SearchResults> buildSearch(String line, InvertedIndex index) {

		// queryList contains queries from a single line.
		ArrayList<String> queryList = new ArrayList<String>();

		// split by white-space
		String[] querys = line.split("\\s+");

		// Use a for-each loop to iterate through the array
		for (String query : querys) {

			// Process word into default format

			query = query.trim(); // Remove leading or trailing spaces
			query = query.toLowerCase(); // Convert to lowercase
			query = query.replaceAll("_", "");
			query = query.replaceAll("\\W", "");
			// Remove queries that were just spaces
			if (query.isEmpty()) {
				continue;
			}

			// Add word to wordList
			queryList.add(query);
		}// out of query
		 
			ArrayList<SearchResults> result = index.searchIndex(queryList);
			 
			return result;
	 
	}

	/**
	 * Indicates that we now have additional "pending" work to wait for.
	 */
	private synchronized void incrementPending() {
		pending++;

	}

	/**
	 * Indicates that we now have one less "pending" work, and will notify any
	 * waiting threads if we no longer have any more pending work left.
	 */
	private synchronized void decrementPending() {
		pending--;

		if (pending <= 0) {
			this.notifyAll();
		}
	}

	/**
	 * Print out the output file of the Partial Search
	 * 
	 * @param qyresult the output file's path/name for PartialSearch
	 */

	public void printFile(String qyresult) {
		lock.lockRead(); 
		Path file = Paths.get(qyresult);
		Charset charset = Charset.forName("UTF-8");

		try (BufferedWriter output = Files.newBufferedWriter(file, charset)) {

			for (String query : results.keySet()) {
				output.write(query);
				output.newLine();
				for (SearchResults result : results.get(query)) {
					output.write(result.toString());
					output.newLine();
				}

				output.newLine();
			} // out query

		} catch (Exception e) {

			System.err
					.println("Unexpected Erros on printing PatyialSearch Files to"
							+ qyresult);
			return;

		}

		System.out.println("The out put file for Partial Search is " + "\""
				+ qyresult + "\"" + "\n");

		lock.unlockRead();
	}

}