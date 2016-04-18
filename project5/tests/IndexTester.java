import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexTester {

	private static final String format =
			"Your output file %s does not match the expected output.";

	/**
	 * Prevents running of any tests if the project setup is incorrect.
	 */
	@Before
	public void before() {
		String message = "Unable to find input and output directories, "
				+ "please check your project setup!";
		Assert.assertTrue(message, BaseTester.isEnvironmentSetup());
	}

	/**
	 * Tests if code runs without exceptions when no directory is provided.
	 */
	@Test
	public void testNoDirectory() {
		Driver.main(new String[] {"-i", "invertedindex-nodir.txt"});
	}

	/**
	 * Tests if code runs without exceptions an in invalid directory is
	 * provided.
	 */
	@Test
	public void testBadDirectory() {
		String name = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String path = Paths.get(BaseTester.getBaseDirectory(), name).toString();
		Driver.main(new String[] {"-d", path, "-i", "invertedindex-baddir.txt"});
	}

	/**
	 * Tests if correct index is generated for the simple test case.
	 * @throws IOException
	 */
	@Test
	public void testIndexSimple() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-simple.txt";

		Path dir = Paths.get(base, "input", "index", "simple");
		Path out = Paths.get(base, "output", name);

		String[] args = new String[] {
				"-d", dir.toAbsolutePath().toString(),
				"-i", name};

		Files.deleteIfExists(Paths.get(name));

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get(name), out));
	}

	/**
	 * Tests if the correct index is generated for the simple test case
	 * when no output file name is provided. (The default should be
	 * invertedindex.txt.)
	 * @throws IOException
	 */
	@Test
	public void testIndexNoOutput() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-simple.txt";

		Path dir = Paths.get(base, "input", "index", "simple");
		Path out = Paths.get(base, "output", name);

		Files.deleteIfExists(Paths.get(name));

		String[] args = new String[] {
				"-d", dir.toAbsolutePath().toString()};

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get("invertedindex.txt"), out));
	}

	/**
	 * Tests if the correct index is generated for the simple test case
	 * when the argument order is reversed.
	 * @throws IOException
	 */
	@Test
	public void testIndexReversed() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-simple.txt";

		Path dir = Paths.get(base, "input", "index", "simple");
		Path out = Paths.get(base, "output", name);

		Files.deleteIfExists(Paths.get(name));

		String[] args = new String[] {
				"-i", "invertedindex-reversed.txt",
				"-d", dir.toAbsolutePath().toString()};

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get("invertedindex-reversed.txt"), out));
	}

	/**
	 * Tests if the correct index is generated for the RFCs test case.
	 * @throws IOException
	 */
	@Test
	public void testIndexRFCs() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-rfcs.txt";

		Path dir = Paths.get(base, "input", "index", "rfcs");
		Path out = Paths.get(base, "output", name);

		Files.deleteIfExists(Paths.get(name));

		String[] args = new String[] {
				"-d", dir.toAbsolutePath().toString(),
				"-i", name};

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get(name), out));
	}

	/**
	 * Tests if the correct index is generated for the Gutenberg test case.
	 * @throws IOException
	 */
	@Test
	public void testIndexGutenberg() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-gutenberg.txt";

		Path dir = Paths.get(base, "input", "index", "gutenberg");
		Path out = Paths.get(base, "output", name);

		Files.deleteIfExists(Paths.get(name));

		String[] args = new String[] {
				"-d", dir.toAbsolutePath().toString(),
				"-i", name};

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get(name), out));
	}

	/**
	 * Tests if the correct index is generated for the entire index test case.
	 * @throws IOException
	 */
	@Test
	public void testIndexAll() throws IOException {
		String base = BaseTester.getBaseDirectory();
		String name = "invertedindex-index.txt";

		Path dir = Paths.get(base, "input", "index");
		Path out = Paths.get(base, "output", name);

		Files.deleteIfExists(Paths.get(name));

		String[] args = new String[] {
				"-d", dir.toAbsolutePath().toString(),
				"-i", name};

		Driver.main(args);

		Assert.assertTrue(String.format(format, name),
				BaseTester.testFiles(Paths.get(name), out));
	}
}
