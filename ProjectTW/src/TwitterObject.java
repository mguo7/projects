  import java.util.ArrayList;


public class TwitterObject {
	
	Integer version;
	ArrayList<String> tweets;
	
	public TwitterObject() {
		
		this.version = new Integer(0);
		this.tweets = new ArrayList<String>();
		
	}

	public void updateVersion() {
		
		this.version += 1;
		
	}
	
	public void addTweet(String tweet) {
		
		this.tweets.add(tweet);
		
	}
	
}
