package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Uses multithreading to crawl a given URL, following links and redirects, to a certain limit, and fetches the HTML
 * The HTML is then crawled for more links, cleaned, stemmed, and added to the word index
 *
 * @author Matthew Kahane
 */
public class WebCrawler {

	/** Logger used throughout this class */
	private static final Logger log = LogManager.getLogger("WebCrawler");


	/**
	 * Cleans and inputs the HTML into the word index with the URL as the location
	 *
	 * @param html the HTML to clean and index
	 * @param url the location the HTML was found
	 * @param index the index to input the words from the HTML into
	 */
	private static void indexHTML(String html, String url, ThreadSafeIndex index) {
		// TODO this will handle inputting parsing, cleaning, stemming, and
		// inputting the html into the index with the url as the location.
		// should be similar to FileFinder.inputFile
		log.debug("indexing HTML from: " + url);
		ArrayList<String> cleanedWords = WordCleaner.listStems(html);

		if (cleanedWords.size() > 0) {
			index.addWordCount(url, cleanedWords.size());
		}

		for (int i = 0; i < cleanedWords.size(); i++) {
			index.add(cleanedWords.get(i), url, i+1);
		}
	}

	/**
	 * Fetches and strips the HTML specified by the seed URL. It then finds all
	 * of the URLs in the HTML and adds them to a work queue so that each can be
	 * crawled for links, cleaned even further, and input into the index.
	 *
	 * @param seed the URL to start the crawl at
	 * @param maxCrawls The maximum number of URLs to crawl after the seed URL (should be decremented when calling findHTML)
	 * @param index the index to input the HTML into
	 * @param threadCount the number of threads to start the work queue with
	 * @throws MalformedURLException if the seed URL is malformed
	 */
	public static void findHTML(String seed, int maxCrawls, ThreadSafeIndex index,
			int threadCount) throws MalformedURLException {
		WorkQueue queue = new WorkQueue(threadCount);
		log.debug("Work queue created with " + threadCount + " threads");

		String html = HtmlFetcher.fetch(seed, 3);
		if (html == null) {
			return;
		}
		String strippedHtml = HtmlCleaner.stripBlockElements(html);
		ArrayList<URL> urls = new ArrayList<URL>();
		LinkFinder.findUrls(new URL(seed), strippedHtml, urls);
		ArrayList<URL> processed = new ArrayList<URL>();
		log.debug("Found " + urls.size() + " URLs from seed HTML");

		processed.add(new URL(seed)); // XXX switched these, does it work?
		queue.execute(new Task(new URL(seed), urls, index, processed)); // XXX should I just add seed to urls instead? this will go through it twice..

		Iterator<URL> iterator = urls.iterator();
		while ((iterator.hasNext()) && maxCrawls > 0) {
			URL current = iterator.next();
			if (!processed.contains(current)) {
				maxCrawls--;
				log.debug("Creating task for URL: " + current + "...");
				processed.add(current);
				queue.execute(new Task(current, urls, index, processed));
				//processed.add(current);
				/* TODO create workers for the queue on each url
				 * each worker should do the above, decrementing maxCrawls as each is created
				 * then stripHtml, convert to unicode(?)
				 * then clean, parse, and stem remaining text and add them to the index
				 * the location should be the seed (? or the url from the set?)
				 */
			}
		}
		log.debug("Waitiing for tasks to finish...");
		queue.join();
		log.debug("Tasks finished, queue terminated");

	}

	/**
	 * Creates tasks to be run by workers in the queue
	 *
	 * @author Matthew Kahane
	 */
	private static class Task implements Runnable {

		/** The URL to pull the HTML from */
		private URL url;

		/** The set of URLs that will be queued up to crawl */
		private ArrayList<URL> urls;

		/** The index to input the HTML into */
		private ThreadSafeIndex index;

		private ArrayList<URL> processed;

		/**
		 * Initializes the Task
		 *
		 * @param url the URL to pull the HTML from
		 * @param urls the set of URLs that will be queued up to crawl
		 * @param index the index to input the HTML to
		 * @param processed
		 */
		public Task(URL url, ArrayList<URL> urls, ThreadSafeIndex index, ArrayList<URL> processed) {
			this.url = url;
			this.urls = urls;
			this.index = index;
			this.processed = processed;
		}

		@Override
		public void run() {
			String html = HtmlFetcher.fetch(url, 3);
			if (html == null) {
				return;
			}
			String strippedHtml = HtmlCleaner.stripBlockElements(html);
			HashSet<URL> innerUrls = LinkFinder.uniqueUrls(url, strippedHtml);
			for (URL newUrl : innerUrls) {
				if (!processed.contains(newUrl)) {
					urls.add(newUrl);
				}
			}
			String cleanedHtml = HtmlCleaner.stripHtml(strippedHtml);
			indexHTML(cleanedHtml, url.toString(), index);
		}

	}

}
