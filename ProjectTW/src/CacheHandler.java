import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class can store the query which contains its tweets and version number
 * into a cache.
 * 
 * @author miaoguo
 * 
 */

public class CacheHandler {

	public HashMap<String, TwitterObject> cache;

	@SuppressWarnings("unchecked")
	public CacheHandler() {

		cache = new JSONObject();

	}

	/**
	 * Check if the cache contains query.
	 * 
	 * @param searchterm
	 * @return True if the cache contains query
	 */
	public boolean hasTag(String searchterm) {

		return this.cache.containsKey(searchterm);
	}

	/**
	 * Get version number of a hashtag from the cache
	 * 
	 * @param hashtag
	 * @return versionnum
	 */
	public Integer getVersion(String hashtag) {

		Integer version = this.cache.get(hashtag).version;

		return version;
	}

	/**
	 * Update Cache with a new version number
	 * 
	 * @param hashtag
	 * @param Version
	 * @param tweets
	 */
	@SuppressWarnings("unchecked")
	public void updateVersion(String hashtag, Integer Version, JSONArray tweets) {

		if (this.cache.containsKey(hashtag)) {
			TwitterObject obj = this.cache.get(hashtag);
			obj.tweets.clear();
			obj.tweets.addAll(tweets);
			obj.version = Version;
			this.cache.put(hashtag, obj);
		} else {

			TwitterObject obj = new TwitterObject();
			obj.tweets.addAll(tweets);
			obj.version = Version;
			this.cache.put(hashtag, obj);
			System.out.println(this.cache);

		}

	}

	/**
	 * Get tweets from the Cache
	 * 
	 * @param hashtag
	 * @return tweets
	 */
	@SuppressWarnings("unchecked")
	public JSONArray getTweets(String hashtag) {

		JSONArray tweets = new JSONArray();

		TwitterObject obj = this.cache.get(hashtag);
		tweets.addAll(obj.tweets);

		return tweets;

	}

	public HashMap<String, TwitterObject> getCache() {

		return this.cache;
	}

}
