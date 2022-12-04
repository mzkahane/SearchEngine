package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

public class WebCrawler {

	public static void indexHTML(String seed, int maxCrawls, ThreadSafeIndex index) throws MalformedURLException {
		String html = HtmlFetcher.fetch(seed, 3);
		String strippedHtml = HtmlCleaner.stripBlockElements(html);
		HashSet<URL> urls = LinkFinder.uniqueUrls(new URL(seed), strippedHtml);

		for (URL url : urls) { // should this be looping over maxCrawls?
			/* TODO create workers for the queue on each url
			 * each worker should do the above, decrementing maxCrawls as each is created
			 * then stripHtml, convert to unicode(?)
			 * then clean, parse, and stem remaining text and add them to the index
			 * the location should be the seed (? or the url from the set?)
			 */
		}

	}

}
