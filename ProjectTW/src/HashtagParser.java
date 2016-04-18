import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class will create a regular expression that is able to parse hashtags
 * from request body.
 * 
 * @author Miao Guo
 */
public class HashtagParser {

	/**
	 * The regular expression used to parse hashtags.
	 * 
	 */
	public static final String REGEX = "(?<=#+)[\\w]+";

	/**
	 * The group in the regular expression that captures the raw link.
	 */

	public static ArrayList<String> getTags(String text) {

		ArrayList<String> hashtags = new ArrayList<String>();

		// compile string into regular expression
		Pattern p = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

		// match provided text against regular expression
		Matcher m = p.matcher(text);

		// loop through every match found in text

		while (m.find()) {

			String tag = m.group();
			hashtags.add(tag);
		}

		return hashtags;
	}

}