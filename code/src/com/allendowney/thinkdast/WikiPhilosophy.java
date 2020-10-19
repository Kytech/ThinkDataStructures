package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Node;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        /*
        curPage = source
        continueSearch = true
        searchFailed = false
        do
            // Fetch page and log as visited
            paragraphs = wf.fetchWikipedia(curPage)
            visited.add(curPage)

            search for valid new link in returned paragraphs (getFirstValidLinkIfNoLoop)
                valid if: 
                1. No italics
                2. Not in parentheses
                3. Not to current page
                4. No external links
                5. In page body
                6. If first valid link found loops back to page we've seen, failure condition.
            
            found destination? success! Add to visited. Stop search

            no valid links from above test? Fail. Stop Search
            First valid link to page seen before? Fail. Stop search

            follow found link to next page and repeat
        while continue search = true

        display results and failure status
            // Failure msg - Could not find valid link in page or first valid link on page
            // creates a loop
        */

        String curPage = source;
        boolean continueSearch = true;
        boolean searchFailed = false;
        int pageCounter = -1;

        do {
            Elements paragraphs = wf.fetchWikipedia(curPage);
            visited.add(curPage);

            String link = getFirstValidLinkIfNoLoop(paragraphs, curPage);

            if (link == destination) {
                visited.add(link);
                continueSearch = false;
            } else if (link == null) {
                searchFailed = true;
                continueSearch = false;
            } else {
                curPage = link;
                pageCounter++;
            }

        } while (continueSearch && pageCounter < limit);

        if (pageCounter >= limit) {
            System.out.println("Unable to get to " + destination + " within the limit of " + limit + " pages from " + source + ".");
        } else if (!searchFailed) {
            System.out.println("Successfully got to " + destination + " from " + source + "!");
        } else {
            System.out.println("Unable to get to " + destination + " from " + source + ".");
        }
        
        System.out.println("The following route was taken: (" + pageCounter + " pages from 1st page)");
        for (String url : visited) {
            System.out.println(url);
        }

        if (searchFailed) {
            System.out.println("Could not find a valid link in the last listed page or the first link");
            System.out.println("on the page created an infinite loop.");
        }
    }

    /** 
     * Takes a collection of HTML DOM tree elements and returns the first valid link address, or null
     * if there are no valid links or if the first valid link creates an endless loop by linking to
     * a page that we have visited before.
     * 
     * @param pElements
     * @param currentPage
     * @return Null or the URL of the first link that does not create a loop
     */
    private static String getFirstValidLinkIfNoLoop(Elements pElements, String currentPage) {
        /*
        search for valid new link in returned paragraphs (getFirstValidLinkIfNoLoop)
                valid if: 
                3. Not to current page
                4. No external links
                5. If first valid link found loops back to page we've seen, failure condition.
        */

        // Loop through each paragraph
        for (Element pElement : pElements) {

            // Track the depth of parentheses. We'll treat the end of
            // a paragraph as explicitly closing all unclosed parentheses in case those exist.
            int parenthDepth = 0;

            for (Node pNode : pElement.childNodes()) {
                if (pNode instanceof TextNode) {
                    TextNode pTextNode = (TextNode)pNode;
                    String nodeText = pTextNode.text();

                    // Iterate over the characters of the text node
                    // to figure out if we are inside parentheses or not
                    char[] nodeTextChars = nodeText.toCharArray();
                    for (char strchar : nodeTextChars) {
                        if (strchar == '(') parenthDepth++;
                        if (strchar == ')') parenthDepth--;
                    }
                } else if (pNode instanceof Element) {
                    Element pChildElem = (Element)pNode;

                    // Ignore any non link tags and ignore tags between parentheses. We're only interested in
                    // links without extra formatting (links directly in the paragraph text), so this will exclude
                    // links in citations, bold, italics, etc.
                    if (pChildElem.tagName() == "a" && pChildElem.attributes().hasKey("href") && parenthDepth <= 0) {
                        // Validate the link. If it's valid, check to see if it creates a loop.
                        // If it creates a loop, return null. Otherwise, link is good, return it.
                        // If link is not valid, we'll just move on to the next link
                        String linkURL = pChildElem.attributes().get("abs:href");
                        if (isValidURL(linkURL, currentPage)) {
                            if (!visited.contains(linkURL)) {
                                return linkURL;
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
        }

        // We didn't find any links
        return null;
    }

    /**
     * Checks to see if the URL does not link to the same page,
     * and if it is not an external link.
     * 
     * @param url
     * @param currentUrl
     * @return Whether the provided url is valid or not
     */
    private static boolean isValidURL(String url, String currentURL) {
        // Links to same page test
        if (url.contains("https://en.wikipedia.org" + currentURL)) return false;

        // External link test. All internal wikipedia links
        // will be relative to wikipedia's structure instead of absolute
        if (url.contains("http:")) return false;

        // If we made it here, the link's valid
        return true;
    }
}
