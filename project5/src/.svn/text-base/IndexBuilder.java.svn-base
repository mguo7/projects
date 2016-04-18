import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Read all text files from a provided directory or a seed URL(include all sub directories 
 * and all links from the URL and the new links).The program will catch each line in a file
 * and post all necessary information (Include words and filename/path(links))
 * to InvertedIndex.java.
 * @author Miao Guo
 */
public class IndexBuilder {

	private static Logger logger = LogManager.getLogger(IndexBuilder.class);
	private final WorkQueue workers;
	private ArrayList<Path> files;
	private HashSet<String> urls;
	private int pending;

	public IndexBuilder(WorkQueue workers) {

		this.workers = workers;
		files = new ArrayList<Path>();
		pending = 0;
	}

	public IndexBuilder(String url, WorkQueue workers) {

		urls = new HashSet<String>();
		urls.add(url);
		this.workers = workers;
		pending = 0;

	}

	/**
	 * Helper method, that helps a thread wait until all of the current work is
	 * done.
	 */
	public synchronized void finish() {

		try {
			while (pending > 0) {
				logger.debug("Working is not finished.");
				this.wait();
			}
		} catch (InterruptedException e) {
			logger.debug("Interrupted.", e);
			System.out.println("Finishe interrupted.");
		}

	}

	public void parseUrl(String url, InvertedIndex index) {

		workers.execute(new WebWorker(url, index));

		finish();

	}

	/**
	 * Read files from the ArrayList files and catch each line from the file.
	 * The class will normalized the words from .txt files and mark words'
	 * positions by calling getWord method.
	 * 
	 * @param dir of a provided directory
	 */
	public void parseDir(Path dir, InvertedIndex index) {

		parseDir(dir, files);

		for (Path file : files) {

			if (checkExtension(file)) {

				workers.execute(new IndexWorker(file, index));

			}

		}

		finish();
	}

	/**
	 * Find all files from a provided directory and all sub directories from
	 * that directory. Add a new worker to the workqueue when the program read a
	 * different directory.
	 * 
	 * @param dir of a provided directory
	 * @param files of the ArrayList which contains all files from a provided
	 *              directory and all sub directories
	 */
	private void parseDir(Path dir, ArrayList<Path> files) {

		String filename = null; // current file for catching the exception

		try (DirectoryStream<Path> filelist = Files.newDirectoryStream(dir)) {
			for (Path file : filelist) {
				filename = file.toString();
				if (Files.isDirectory(file)) {
					parseDir(file, files);

				} else {
				addFile(file);
			}
		 } //out of file
	  } catch (IOException e) {
			// print a message on which file is failed to read
			System.err.println("Errors on reading file: " + filename);
		}

	}

	/**
	 * Split lines from a file into different words by white spaces. The program
	 * ignores all characters except letters and digits. In addition, it is
	 * case-insensitive.
	 * 
	 * @param path of the file's name/path
	 */
	private static void parseWords(Path file, InvertedIndex index) {

		Charset charset = Charset.forName("UTF-8");

		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {

			// line is one line from a file which will be caught by the program
			String line = "";
			// the position of a word in the file
			int position = 0;

			// Read text file txtFile[i] by lines
			while ((line = reader.readLine()) != null) {

				// split by white-space
				String[] words = line.split("\\s+");

				// Use a for-each loop to iterate through the array
				for (String word : words) {

					// Process word into default format

					word = word.trim(); // Remove leading or trailing spaces
					word = word.toLowerCase(); // Convert to lowercase
					word = word.replaceAll("_", "");
					word = word.replaceAll("\\W", "");

					if (word.isEmpty()) {
						continue;
					} else {

						position++;
						index.addWords(word, position, file.toAbsolutePath()
								.toString());

					}
				} // out of word

			} // out of line
		} catch (IOException e) {

			System.err.println("Error: unexpected IOexception.");

		}

	}

	/**
	 * For each file, the program will create a new worker for parsing words
	 * from that file.
	 */
	private class IndexWorker implements Runnable {

		private Path file;
		private InvertedIndex index;

		/**
		 * Parse all words from a file in the list of files.
		 * 
		 * @param file of files from ArrayList<Path> files
		 * @param index of the inverted index we want to build.
		 */
		public IndexWorker(Path file, InvertedIndex index) {

			this.file = file;
			this.index = index;
			incrementPending(); // pending++ if we create this worker

		}

		@Override
		public void run() {

			InvertedIndex local = new InvertedIndex();
			parseWords(file, local);
			index.addAll(local);

			decrementPending(); // pending-- when the worker's work is done
			logger.debug("One worker's work is finished");
			logger.debug("Now Pending is: " + pending);
		}

	}

	/**
	 * For each URL, the program will create a new worker for parsing words from
	 * that URL.
	 */
	private class WebWorker implements Runnable {

		private String url;
		private InvertedIndex index;

		/**
		 * Parse all words from a web page in the list of URLs.
		 * 
		 * @param url of a given seed URL or links from a parsed web page
		 * @param index of the inverted index we want to build.
		 */
		public WebWorker(String url, InvertedIndex index) {

			this.url = url;
			this.index = index;
			incrementPending(); // pending++ if we create this worker

		}

		@Override
		public void run() {

			InvertedIndex local = new InvertedIndex();	
			// get all links from web page
			try {
				
			URL addr = new URL(url);
			String html = HTMLFetcher.fetchHTML(addr);
			ArrayList<String> links = HTMLLinkParser.listLinks(html, url);
 					
			for (String link : links) {
				
				if(!urls.contains(link) && urls.size() < 50) {
					
					urls.add(link);
					workers.execute(new WebWorker(link, index));
					
				} else if (urls.size() >= 50) {
					
					break;
				}
				
			}
	 		//add words to index
			int location = 0;
			
			for (String word : HTMLFetcher.cleanHTML(html).split("\\s+")) {
				word = word.trim().replaceAll("[\\W_]+", "");

				if (!word.isEmpty()) {
					location++;	
					local.addWords(word, location, url);	
				}
			}
			
			index.addAll(local);
			
			decrementPending(); // pending-- when the worker's work is done
			logger.debug("One worker's work is finished");
			logger.debug("Now Pending is: " + pending);
		} catch (MalformedURLException e) {
 
			System.err.println("Invaild URL form.");
		 }	
	   }							
	}

	/**
	 * Add Files into the ArrayList.
	 * 
	 * @param file of which contains all index files.
	 */
	private void addFile(Path file) {
		files.add(file);
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
	 * Check if a file from the ArrayList files is a text file.The program will
	 * only read text files from the directories.
	 * 
	 * @param txtfile a file which is need to be checked whether it is a text file
	 *                or not.
	 * @return <code>true</code> if the file is a text file
	 */
	private static boolean checkExtension(Path file) {

		// get the File's suffix
		String fileName = file.toString();

		return fileName.toLowerCase().endsWith(".txt");

	}

}