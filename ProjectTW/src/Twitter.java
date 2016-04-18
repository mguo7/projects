import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class can handle different requests from backend server and store
 * Twitter
 * 
 * @author miaoguo
 * 
 */
public class Twitter {

	/*
	 * This is a JSONObject like Twitter
	 */

	public HashMap<String, TwitterObject> twitter;
	private final MultiReaderLock lock;

	public Twitter() {

		this.twitter = new HashMap<String, TwitterObject>();
		this.lock = new MultiReaderLock();
		

	}

	/**
	 * Add a tweet to Twiiter
	 * 
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	public void addTweet(String request) {

		JSONParser jsonParser = new JSONParser();

		JSONObject jsonTweet;
		try {
			jsonTweet = (JSONObject) jsonParser.parse(request);
			String tweet = (String) jsonTweet.get("tweet");
			ArrayList<String> hashtags = (ArrayList<String>) jsonTweet
					.get("hashtags");

			// delete duplicate hashtags
			HashSet<String> uniquetags = new HashSet<String>();
			uniquetags.addAll(hashtags);
			hashtags.clear();
			hashtags.addAll(uniquetags);

			for (String tag : hashtags) {
				   lock.lockWrite();
				if (!this.twitter.containsKey(tag)) {
					
					TwitterObject tweetobj = new TwitterObject();
					tweetobj.addTweet(tweet);
					tweetobj.updateVersion();
					this.twitter.put(tag, tweetobj);
					

				} else {
					 
					TwitterObject tweetobj = this.twitter.get(tag);
					tweetobj.updateVersion();

					if (!tweetobj.tweets.contains(tweet)) {
						tweetobj.tweets.add(tweet);
					}

					this.twitter.put(tag, tweetobj);
					 
				}
				lock.unlockWrite();
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(this.twitter);

	}

	/**
	 * Check if twitter has the query
	 * 
	 * @param searchterm
	 * @return True if the twiiter contains the searchterm
	 */
	public boolean hasTweet(String searchterm) {

		return this.twitter.containsKey(searchterm);

	}

	/**
	 * Check if a query is out of version.
	 * 
	 * @param searchterm
	 * @param ver1
	 * @return True if the version of query from FE or FE's cache is less than
	 *         the version from BE
	 */

	public boolean outVersion(String searchterm, Integer ver1) {

		TwitterObject obj = this.twitter.get(searchterm);
		Integer ver2 = obj.version;

		return ver1 < ver2;
	}

	/**
	 * Get Tweet from DataServer
	 * 
	 * @param searchterm
	 * @return tweets
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getTweet(String searchterm) {

		JSONArray tweets = new JSONArray();
		if (this.twitter.containsKey(searchterm)) {
			lock.lockRead();
			TwitterObject obj = this.twitter.get(searchterm);
			tweets.addAll(obj.tweets);
			lock.unlockRead();

		}

		return tweets;
	}

	/**
	 * Get versionnum of the searchterm from DataServer
	 * 
	 * @param searchterm
	 * @return versionnum
	 */

	public Integer getVersion(String searchterm) {

		Integer version = new Integer(0);

		if (this.twitter.containsKey(searchterm)) {

			version = this.twitter.get(searchterm).version;
		}

		return version;
	}

}
