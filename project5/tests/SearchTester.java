import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;


public class SearchTester {

	/**
	 * This helper method passes the necessary arguments to the Driver class,
	 * and then tests the output. The expected output file should always be
	 * in the "output" subdirectory of the base directory. The actual output
	 * file should be in the current working directory.
	 *
	 * @param args arugments to pass to the Driver class
	 * @param expected name of the expected output file
	 * @param actual name of the actual output file
	 * @throws Exception
	 */
	public static void testOutput(String[] args, Path expected, Path actual) {
		String message = "Check file " + actual.getFileName() + ".";

		try {
			Files.deleteIfExists(actual);
			Driver.main(args);
			Assert.assertTrue(message, BaseTester.testFiles(actual, expected));
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Tests to make sure an exception is not thrown if a key argument is not
	 * provided.
	 */
	@Test
	public void testMissingArguments(){
		String base = BaseTester.getBaseDirectory();

		Path input = Paths.get(base, "input", "index", "simple");
		Path search = Paths.get("searchresults.txt");

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-r", search.toAbsolutePath().toString()};

		try {
			Driver.main(args);
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Tests to make sure an exception is not thrown if a bad query file is
	 * provided.
	 */
	@Test
	public void testBadQueryFile() {
		String base = BaseTester.getBaseDirectory();

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-nowhere.txt");

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString()};

		try {
			Files.deleteIfExists(query);
			Driver.main(args);
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Tests to make sure the correct output files are created when the -i and
	 * -r flags are not provided with values.
	 */
	@Test
	public void testDefaultNames() {
		String base = BaseTester.getBaseDirectory();

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-simple.txt");

		Path index = Paths.get("invertedindex.txt");
		Path search = Paths.get("searchresults.txt");

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-i", "-r"};

		try {
			Files.deleteIfExists(index);
			Files.deleteIfExists(search);

			Driver.main(args);

			Assert.assertTrue(Files.exists(index));
			Assert.assertTrue(Files.exists(search));
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Tests to make sure no output files are created when the -i and -r flags
	 * are not provided.
	 */
	@Test
	public void testNoOutput() {
		String base = BaseTester.getBaseDirectory();

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-simple.txt");

		Path index = Paths.get("invertedindex.txt");
		Path search = Paths.get("searchresults.txt");

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString()};

		try {
			Files.deleteIfExists(index);
			Files.deleteIfExists(search);

			Driver.main(args);

			Assert.assertFalse(Files.exists(index));
			Assert.assertFalse(Files.exists(search));
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Tests to make sure that if unable to write the output file, no
	 * exception occurs.
	 */
	@Test
	public void testNoWriteFile() {
		String base = BaseTester.getBaseDirectory();

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-simple.txt");

		try {
			Path path = Files.createTempDirectory(Paths.get(".").normalize(), "temp");
			path.toFile().deleteOnExit();

			String[] args = new String[] {
					"-d", input.toAbsolutePath().toString(),
					"-q", query.toAbsolutePath().toString(),
					"-r", path.toAbsolutePath().toString()};

			Driver.main(args);
		}
		catch (Exception e) {
			Assert.fail(e.toString());
		}
	}

	/**
	 * Searches the inverted index built from the "simple" subdirectory for
	 * queries in the file "queries-simple.txt".
	 */
	@Test
	public void testQuerySimple() {
		String base = BaseTester.getBaseDirectory();
		String name = "searchresults-simple-simple.txt";

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-simple.txt");
		Path results = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-r", name};

		testOutput(args, results, Paths.get(name));
	}

	/**
	 * Searches the inverted index built from the "simple" subdirectory for
	 * queries in the file "queries-alphabet.txt".
	 */
	@Test
	public void testQuerySimpleAlphabet() {
		String base = BaseTester.getBaseDirectory();
		String name = "searchresults-simple-alphabet.txt";

		Path input = Paths.get(base, "input", "index", "simple");
		Path query = Paths.get(base, "input", "search", "queries-alphabet.txt");
		Path results = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-r", name};

		testOutput(args, results, Paths.get(name));
	}

	/**
	 * Searches the inverted index built from the "index" subdirectory for
	 * queries in the file "queries-simple.txt". This test may take awhile
	 * to complete.
	 */
	@Test
	public void testQueryIndexSimple() {
		String base = BaseTester.getBaseDirectory();
		String name = "searchresults-index-simple.txt";

		Path input = Paths.get(base, "input", "index");
		Path query = Paths.get(base, "input", "search", "queries-simple.txt");
		Path results = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-r", name};

		testOutput(args, results, Paths.get(name));
	}

	/**
	 * Searches the inverted index built from the "index" subdirectory for
	 * queries in the file "queries-alphabet.txt". This test may take awhile
	 * to complete.
	 */
	@Test
	public void testQueryIndexAlphabet() {
		String base = BaseTester.getBaseDirectory();
		String name = "searchresults-index-alphabet.txt";

		Path input = Paths.get(base, "input", "index");
		Path query = Paths.get(base, "input", "search", "queries-alphabet.txt");
		Path results = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-r", name};

		testOutput(args, results, Paths.get(name));
	}

	/**
	 * Searches the inverted index built from the "index" subdirectory for
	 * queries in the file "queries-multi.txt". This test may take awhile
	 * to complete.
	 */
	@Test
	public void testQueryIndexMulti() {
		String base = BaseTester.getBaseDirectory();
		String name = "searchresults-index-multi.txt";

		Path input = Paths.get(base, "input", "index");
		Path query = Paths.get(base, "input", "search", "queries-multi.txt");
		Path results = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", input.toAbsolutePath().toString(),
				"-q", query.toAbsolutePath().toString(),
				"-r", name};

		testOutput(args, results, Paths.get(name));
	}
}
