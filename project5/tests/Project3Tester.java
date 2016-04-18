import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	BaseTester.class,
	IndexTester.class,
	SearchTester.class,
	ThreadTester.class,
	ThreadStressTester.class})
public class Project3Tester {
	/*
	 * To be eligible for code review for project 3, you must pass
	 * this test suite on the lab computers.
	 */
}
