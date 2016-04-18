import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class ThreadStressTester {

	private static final int WARM_RUNS = 2;
	private static final int TIME_RUNS = 5;
	private static final int THREADS = 3;

	/**
	 * Tests the inverted index output multiple times, to make sure the
	 * results are always consistent.
	 */
	@Test(timeout = ThreadTester.TIMEOUT * TIME_RUNS)
	public void testIndexConsistency() {
		ThreadTester.OutputTester tester =
				new ThreadTester.OutputTester(String.valueOf(THREADS));

		for (int i = 0; i < TIME_RUNS; i++) {
			tester.testThreadIndexOutput();
		}
	}

	/**
	 * Tests the search result output multiple times, to make sure the
	 * results are always consistent.
	 */
	@Test(timeout = ThreadTester.TIMEOUT * TIME_RUNS)
	public void testSearchConsistency() {
		ThreadTester.OutputTester tester =
				new ThreadTester.OutputTester(String.valueOf(THREADS));

		for (int i = 0; i < TIME_RUNS; i++) {
			tester.testThreadSearchOutput();
		}
	}

	/**
	 * Makes sure that your code runs faster with 3 threads than with 1 thread.
	 */
	@Test(timeout = ThreadTester.TIMEOUT * (WARM_RUNS + TIME_RUNS))
	public void testRuntime() {
    		double singleAverage = benchmark(String.valueOf(1));
    		double threadAverage = benchmark(String.valueOf(THREADS));

    		System.out.printf("%d Threads: %.2f ns%n", 1, singleAverage);
    		System.out.printf("%d Threads: %.2f ns%n", THREADS, threadAverage);

    		Assert.assertTrue(singleAverage >= threadAverage);
	}

	private double benchmark(String numThreads) {
    		String base = BaseTester.getBaseDirectory();

    		Path input = Paths.get(base, "input", "index");
    		Path query = Paths.get(base, "input", "search", "queries-multi.txt");

    		String[] args = new String[] {
    				"-d", input.toAbsolutePath().toString(),
    				"-q", query.toAbsolutePath().toString(),
    				"-t", numThreads};

		long total = 0;
		long start = 0;

    		try {
    			for (int i = 0; i < WARM_RUNS; i++) {
    				Driver.main(args);
    			}

    			for (int i = 0; i < TIME_RUNS; i++) {
    				start = System.nanoTime();
    				Driver.main(args);
    				total += (System.nanoTime() - start);
    			}
    		}
    		catch (Exception e) {
    			Assert.fail(String.format(
    					"Test Case: %s%nException: %s",
    					"runtime", e.toString()));
    		}

    		return (double) total / TIME_RUNS;
	}
}
