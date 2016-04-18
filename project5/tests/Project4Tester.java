import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	BaseTester.class,
	IndexTester.class,
	SearchTester.class,
	ThreadTester.class,
	CrawlTester.class,
	CrawlTester.ExternalTester.class})
public class Project4Tester {
	/*
	 * To be eligible for code review for project 4, you must pass
	 * this test suite on the lab computers.
	 */
}
