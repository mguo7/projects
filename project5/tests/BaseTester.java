import java.io.BufferedReader;
import java.io.File;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

public class BaseTester {

	/**
	 * Directory of files on the lab computer. Do not modify this variable!
	 */
	public static final String CSLAB_DIR = "/home/public/cs212";

	/**
	 * Tests if the project setup is incorrect.
	 */
	@Test
	public void testEnvironment() {
		String message = "Unable to find input and output directories, "
				+ "please check your project setup!";
		Assert.assertTrue(message, isEnvironmentSetup());
	}

	/**
	 * Tests whether you are running from a lab computer.
	 */
	@Test
	public void testLabComputer() {
		try {
			// output system information for debugging purposes
			System.out.println("Host Address: " +
					InetAddress.getLocalHost().getHostAddress());

			System.out.println("   Host Name: " +
					InetAddress.getLocalHost().getCanonicalHostName());

			System.out.println("   Base Path: " + getBaseDirectory());
		}
		catch (Exception ex) {
			System.out.println("Unable to determine host information.");
		}

		String errorMessage = "You must be testing from the CS lab " +
				"computers to pass this test.";
		Assert.assertTrue(errorMessage, isLabComputer());
	}

	/**
	 * Tests whether Driver runs without exceptions when given no arguments.
	 */
	@Test
	public void testNoArguments() {
		// assumes no exceptions are thrown with bad arguments
		String[] args = new String[] {};
		Driver.main(args);
	}

	/**
	 * Tries to determine if the tests are being run on a lab computer by
	 * looking at the hostname and ip address.
	 *
	 * @return true if it appears the tests are being run on a lab computer
	 */
	public static boolean isLabComputer() {
		boolean lab = false;

		try {
			Path base = Paths.get(CSLAB_DIR);
			String addr = InetAddress.getLocalHost().getHostAddress();
			String name = InetAddress.getLocalHost().getCanonicalHostName();

			if (Files.isReadable(base) &&
					addr.startsWith("138.202.171.") &&
					name.endsWith(".cs.usfca.edu")) {
				lab = true;
			}
			else {
				lab = false;
			}
		}
		catch (Exception ex) {
			lab = false;
		}

		return lab;
	}

	/**
	 * Gets the base directory to use for testing purposes. Either the CS Lab
	 * directory or the current project directory.
	 *
	 * @return path of base directory
	 */
	public static String getBaseDirectory() {
		String base = isLabComputer() ? CSLAB_DIR : ".";

		try {
			base = Paths.get(base).toRealPath().toString();
		}
		catch (Exception ex) {
			base = ".";
		}

		return base;
	}

	/**
	 * Tests whether environment setup is correct, with a input and output
	 * directory located within the base directory.
	 */
	public static boolean isEnvironmentSetup() {
		String base = getBaseDirectory();
		Path input  = Paths.get(base, "input");
		Path output = Paths.get(base, "output");

		return Files.isReadable(input) && Files.isReadable(output);
	}

	/**
	 * Tests line-by-line if two files are equal. If one file contains extra
	 * blank lines at the end of the file, the two are still considered equal.
	 *
	 * This test will pass even if the files are generated from different base
	 * directories, and hence have slightly different absolute paths.
	 *
	 * @param path1 path to first file to compare with
	 * @param path2 path to second file to compare with
	 * @return true if the two files are equal
	 */
	public static boolean testFiles(Path path1, Path path2) {
		Charset charset = java.nio.charset.StandardCharsets.UTF_8;

		try (
			BufferedReader reader1 =
					Files.newBufferedReader(path1, charset);
			BufferedReader reader2 =
					Files.newBufferedReader(path2, charset);
		) {
			String line1 = reader1.readLine();
			String line2 = reader2.readLine();

			// used to output line mismatch
			int count = 0;

			// used to remove base directory from paths
			String pattern = Matcher.quoteReplacement(getBaseDirectory());

			while(true) {
				count++;

				if ((line1 != null) && (line2 != null)) {
					if (!line1.trim().equals(line2.trim())) {
						// it is possible the base directory is different

						// replace base directory with CS_LAB directory
						line1 = line1.replaceFirst(pattern, CSLAB_DIR);
						line2 = line2.replaceFirst(pattern, CSLAB_DIR);

						// use consistent path separators
						line1 = line1.replaceAll(Matcher.quoteReplacement(File.separator), "/");
						line2 = line2.replaceAll(Matcher.quoteReplacement(File.separator), "/");

						// now compare lines again
						if (!line1.equals(line2)) {
							System.out.println("WARNING: Mismatch found on line "
									+ count + " of " + path1.getFileName());
							return false;
						}
					}

					line1 = reader1.readLine();
					line2 = reader2.readLine();
				}
				else {
					// discard extra blank lines at end of reader1
					while ((line1 != null) && line1.trim().isEmpty()) {
						line1 = reader1.readLine();
					}

					// discard extra blank lines at end of reader2
					while ((line2 != null) && line2.trim().isEmpty()) {
						line2 = reader2.readLine();
					}

					// only true if both are null, otherwise one file had
					// extra non-empty lines
					return (line1 == line2);
				}
			}
		}
		catch (Exception ex) {
			return false;
		}
	}

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
	public static void testProject(String[] args, String expected, String actual) throws Exception {
		String base = getBaseDirectory();
		Path path1 = Paths.get(".", actual);
		Path path2 = Paths.get(base, "output", expected);

		Files.deleteIfExists(path1);
		Driver.main(args);

		Assert.assertTrue(testFiles(path1, path2));
	}

}
