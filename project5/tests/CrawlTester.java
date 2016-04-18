import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CrawlTester.LocalTester.class,
	CrawlTester.ExceptionTester.class
})
public class CrawlTester {

	private static final String NUM_THREADS = "5";

	/**
	 * Tests the output of {@link Driver}. If the path arguments are provided,
	 * will make sure the actual output matches the expected output. If the
	 * test fails, will include the name of the test in the assertion output.
	 *
	 * @param testName - name of the test being run
	 * @param args     - arguments to pass to {@link Driver}
	 * @param actual	   - path to actual output
	 * @param expected - path to expected output
	 */
	private static void testProject(
			String testName, String[] args,
			Path actual, Path expected) {
		try {
			if (actual != null) {
				Files.deleteIfExists(actual);
			}

			Driver.main(args);
			Assert.assertTrue(testName, BaseTester.testFiles(actual, expected));
		} catch (Exception e) {
			Assert.fail(String.format("Test Case: %s%nException: %s",
					testName, e.toString()));
		}
	}

	/**
	 * Tests the index result output.
	 */
	private static void testThreadIndexOutput(String test, String link) {
		String base = BaseTester.getBaseDirectory();
		String name = String.format("invertedindex-%s.txt", test);

		Path actual = Paths.get(name);
		Path expected = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-u", link,
				"-i", name,
				"-t", NUM_THREADS};

		testProject(name, args, actual, expected);
	}

	/**
	 * Tests the search result output.
	 */
	private static void testThreadSearchOutput(String test, String link) {
		String base = BaseTester.getBaseDirectory();
		String name = String.format("searchresults-%s.txt", test);

		Path query = Paths.get(base, "input", "search", "queries-multi.txt");
		Path actual = Paths.get(name);
		Path expected = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-u", link,
				"-q", query.toAbsolutePath().toString(),
				"-r", name,
				"-t", NUM_THREADS};

		testProject(name, args, actual, expected);
	}

	/**
	 * This class tests whether {@link Driver} produces the correct
	 * inverted index and search result output for different LOCAL seed urls.
	 * Test these BEFORE running the external seeds to avoid spamming those
	 * web servers.
	 *
	 * You can right-click this class to run just these set of tests!
	 */
	public static class LocalTester {

		@Test
		public void testIndexBirds() {
			String test = "birds";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/birds.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchBirds() {
			String test = "birds";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/birds.html";

			testThreadSearchOutput(test, link);
		}

		@Test
		public void testIndexYellow() {
			String test = "yellow";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/yellowthroat.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchYellow() {
			String test = "yellow";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/yellowthroat.html";

			testThreadSearchOutput(test, link);
		}

		@Test
		public void testIndexHolmes() {
			String test = "holmes";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/gutenberg/1661-h.htm";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchHolmes() {
			String test = "holmes";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/gutenberg/1661-h.htm";

			testThreadSearchOutput(test, link);
		}

		@Test
		public void testIndexGuten() {
			String test = "gutenweb";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/gutenberg.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchGuten() {
			String test = "gutenweb";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/crawl/gutenberg.html";

			testThreadSearchOutput(test, link);
		}

		@Test
		public void testIndexRecurse() {
			String test = "recurse";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/recurse/link01.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchRecurse() {
			String test = "recurse";
			String link = "http://www.cs.usfca.edu/~sjengle/cs212/recurse/link01.html";

			testThreadSearchOutput(test, link);
		}
	}

	/**
	 * This class tests whether {@link Driver} produces the correct
	 * inverted index and search result output for different EXTERNAL seed urls.
	 * Test these AFTER running the local seeds to avoid spamming those
	 * web servers.
	 *
	 * WARNING: This class is NOT run by default by this tester class. Instead,
	 * it is run by the {@link Project4} test suite. If you want to run these
	 * tests ONLY, please right-click the class and "Run-As" a Junit test.
	 */
	public static class ExternalTester {

		@Test
		public void testIndexHTMLHelp() {
			String test = "htmlhelp";
			String link = "http://htmlhelp.com/reference/html40/olist.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchHTMLHelp() {
			String test = "htmlhelp";
			String link = "http://htmlhelp.com/reference/html40/olist.html";

			testThreadSearchOutput(test, link);
		}

		@Test
		public void testIndexLog4j() {
			String test = "log4j";
			String link = "http://logging.apache.org/log4j/1.2/apidocs/allclasses-noframe.html";

			testThreadIndexOutput(test, link);
		}

		@Test
		public void testSearchLog4j() {
			String test = "log4j";
			String link = "http://logging.apache.org/log4j/1.2/apidocs/allclasses-noframe.html";

			testThreadSearchOutput(test, link);
		}
	}

	/**
	 * This class tests whether {@link Driver} runs without generating
	 * an exception.
	 *
	 * You can right-click this class to run just these set of tests!
	 */
	public static class ExceptionTester {

		@Test
		public void testOnlyURLFlag() {
			String[] args = new String[] {"-u"};
			Driver.main(args);
		}

		@Test
		public void testJustURL() {
			String[] args = new String[] {"-u", "http://www.cs.usfca.edu/~sjengle/crawltest/parsetest.html"};
			Driver.main(args);
		}

		@Test
		public void testBadURL() {
			String[] args = new String[] {"-u", "http://www.cs.usfca.edu/~sjengle/nowhere.html"};
			Driver.main(args);
		}
	}
}
