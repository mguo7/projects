import java.nio.file.Paths;

/**
 * The Driver class will accept the following command-line arguments: -d
 * <directory> where -d indicates the next argument is a directory, and
 * <directory> is the directory of text files that must be processed.-u
 *  <seed_url> where -u indicates the next argument <seed_url> is a seed 
 * URL that must be processed and added to an inverted index data structure.
 * -i <filename> where -i is an optional flag that indicates the next argument
 * is a filename, and <filename> is the file name to use for the inverted
 * index output file. If this argument is not provided, the program will use
 * invertedindex.txt as the default output filename.-q <queries> where -q is
 * an optional flag that indicates the next argument <queries> is a path to
 * a text file of queries. If this flag is not provided, then no search
 * should be performed. -r <searchname> where -r is an optional flag that
 * indicates the search results should be written to a file, and is only
 * valid if the -q is also provided. The value <searchname> is optional, and
 * if provided indicates the path and filename to use for the output file.
 * If the -r flag is provided without a <searchname> value, then
 * searchresults.txt should be used as the default output file.
 *-t <threads> where -t is an optional flag that indicates the next argument 
 *<threads> indicates how many worker threads should be used by the work queue. 
 *-t flag/value pair is optional. 
 *If not provided, the program will use 5 worker threads by default.
 * 
 * @author miaoguo
 * 
 */

public class Driver {

/**
 * Provide specific flags for executing the program. Any valid flag should have a 
 * value after it or may use by a default value if no values are provided.
 * 
 * @param args of flag/values.
 */
	public static void main(String[] args) {
		// 1. Parse Arguments
		// 2. Create WorkQueue
		// 3. Build Index (directory or URL or BOTH)
		// 4. Do PartialSearch if there is a -q in args

		ArgumentParser parse = new ArgumentParser(args);
		

		String dir = parse.getValue("-d");
		String filename = parse.getValue("-i");
		String qyfile = parse.getValue("-q");
		String qyresult = parse.getValue("-r");
		String url = parse.getValue("-u");
		Integer threadnum;
		
		
		//Create a WorkQueue
		
		WorkQueue workers;
		if(parse.hasFlag("-t")){
		
		try{
		threadnum = Integer.parseInt(parse.getValue("-t"));
		  
		if(threadnum == null||threadnum <= 0){
			threadnum = 5;
		}
		
		}catch (NumberFormatException e) {
	    threadnum = 5;
		  }
		 
		workers = new WorkQueue(threadnum);
		} else {
			
		workers = new WorkQueue(); // by default
		
		}
		

		//Build Index
		
		InvertedIndex index = new InvertedIndex();
		SearchBuilder search = new SearchBuilder(workers);
		
		if(parse.hasFlag("-d")){
		if (dir == null) {

			System.out
					.println("No directory...Please enter -d before the directory");
			return;
		} else {
			IndexBuilder indexbuilder = new IndexBuilder(workers);
			indexbuilder.parseDir(Paths.get(dir), index);
		}
	}
		
		// do Web Crawler if there is -u flag
		if(parse.hasFlag("-u")){
			
			if(url != null && !url.isEmpty()) {
			IndexBuilder indexbuilder = new IndexBuilder(url, workers);
			System.out.println("Read pages from " + url);
			indexbuilder.parseUrl(url, index);
			
		   } else {
			
			System.out.println("Invailed url inputment.");
		}
		  } else {
			  
			  System.out.println("No -u flag, Program will skip Web Crawler step...");  
		  }
		
		
		// print index file if there is -i flag

		if (parse.hasFlag("-i") || parse.hasFlag("-d") && parse.numFlags() == 1) {

			try {

				if (filename == null) {

					filename = "invertedindex.txt";
					System.out
							.println("No InvertedIndex output filename or there is only ax -d flag ...");

					System.out
							.println("The program will use 'invertedindex.txt' as the default file name.");

					System.out
							.println("InvertedIndex output filename is followed by the flag -i, you can modify the filename ");
				}

				index.printFile(filename);

			} catch (Exception e) {
				System.err.println("Unexpected errors...");
			}
		} else {

			System.out
					.println("No -i flag, the program will not print out the reuslt of InvertedIndex.");

		}
				 
		// build Partial Search if there is "-q" flag
		
		if (parse.hasFlag("-q")) { // do build a partial search for the
									// InvertedIndex if there is a flag "-q"

			try {
				search.parseFile(Paths.get(qyfile), index);

				// print search's results if has a "-r" flag.

				if (parse.hasFlag("-r")) {

					if (qyresult == null) {
						qyresult = "searchresults.txt";
						System.out
								.println("No Partial Search output filename...The program will use 'searchresults.txt' as the default file name");
						System.out
								.println("Partial Search output filename is followed by the flag -q, you can modify the filename ");
					}

					search.printFile(qyresult);

				} else {

					System.out
							.println("No -r flag, the program will not print out the result of PartialSearch");
				}

			} catch (Exception e) {
				System.err.println("Unexpected errors...");

			}

		} else {

			System.out
					.println("No -q flag, the program will skip Partial Search step.");

		}
		
		workers.shutdown();
		
	}
}