import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ThreadTester.BadArgumentTester.class,
	ThreadTester.OutputTester.class})
public class ThreadTester {

	/** Configure this on your system if you want to have a longer timeout. */
	public static final int TIMEOUT = 60000;

	/**
	 * Tests the output of {@link Driver}. If the path arguments are provided,
	 * will make sure the actual output matches the expected output. If the
	 * test fails, will include the name of the test in the assertion output.
	 *
	 * @param testName 	name of the test being run
	 * @param args		arguments to pass to {@link Driver}
	 * @param actual		path to actual output
	 * @param expected	path to expected output
	 */
	private static void testProject(String testName, String[] args, Path actual, Path expected) {
		try {
			if (actual != null) {
				Files.deleteIfExists(actual);
			}

			Driver.main(args);

			if ((actual != null) && (expected != null)) {
				Assert.assertTrue(testName, BaseTester.testFiles(actual, expected));
			}
		}
		catch (Exception e) {
			Assert.fail(String.format(
					"Test Case: %s%nException: %s",
					testName, e.toString()));
		}
	}

	/**
	 * This class tests whether {@link Driver} runs without throwing any
	 * exceptions, despite being given bad arguments.
	 */
	@RunWith(Parameterized.class)
	public static class BadArgumentTester {
		@Parameters
        public static Iterable<Object[]> data() {
        		return Arrays.asList(new Object[][]{
        				{"-1"},		// numThreads should not be negative
        				{"0"},		// numThreads should not be zero
        				{"1.2"},		// numThreads should not be a float
        				{"fox"}		// numThreads should not be a word
        		});
        }

        private String numThreads;

        public BadArgumentTester(String numThreads) {
        		this.numThreads = numThreads;
        }

	    	@Test(timeout = TIMEOUT)
	    	public void testBadThreadValues() {
	    		String base = BaseTester.getBaseDirectory();
	    		String name = "invertedindex-badargs.txt";

	    		Path input = Paths.get(base, "input", "index", "simple");

	    		String[] args = new String[] {
	    				"-d", input.toAbsolutePath().toString(),
	    				"-i", name,
	    				"-t", this.numThreads};

	    		testProject(name, args, null, null);
	    	}
	}

	/**
	 * This class tests whether {@link Driver} produces the correct
	 * inverted index and search result output for different numbers
	 * of threads.
	 */
	@RunWith(Parameterized.class)
	public static class OutputTester {
		@Parameters
        public static Iterable<Object[]> data() {
        		return Arrays.asList(new Object[][]{
        				{"1"},	// test output with 1 worker thread
        				{"2"},	// test output with 2 worker threads
        				{"3"}	// test output with 3 worker threads
        		});
        }

        private String numThreads;

        public OutputTester(String numThreads) {
        		this.numThreads = numThreads;
        }

	    	@Test(timeout = TIMEOUT)
	    	public void testThreadIndexOutput() {
	    		String base = BaseTester.getBaseDirectory();
	    		String name = String.format("invertedindex-%sthreads.txt", this.numThreads);

	    		Path input = Paths.get(base, "input", "index");

	    		Path actual = Paths.get(name);
	    		Path expected = Paths.get(base, "output", "invertedindex-index.txt");

	    		String[] args = new String[] {
	    				"-d", input.toAbsolutePath().toString(),
	    				"-i", name,
	    				"-t", this.numThreads};

	    		testProject(name, args, actual, expected);
	    	}

	    	@Test(timeout = TIMEOUT)
	    	public void testThreadSearchOutput() {
	    		String base = BaseTester.getBaseDirectory();
	    		String name = String.format("searchresults-%sthreads.txt", this.numThreads);

	    		Path input = Paths.get(base, "input", "index");
	    		Path query = Paths.get(base, "input", "search", "queries-multi.txt");

	    		Path actual = Paths.get(name);
	    		Path expected = Paths.get(base, "output", "searchresults-index-multi.txt");

	    		String[] args = new String[] {
	    				"-d", input.toAbsolutePath().toString(),
	    				"-q", query.toAbsolutePath().toString(),
	    				"-r", name,
	    				"-t", this.numThreads};

	    		testProject(name, args, actual, expected);
	    	}
	}
}
