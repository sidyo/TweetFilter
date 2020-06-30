package com.tweetFilter.dataStructures;

import com.tweetFilter.dto.Tweet;

import java.util.*;
import java.util.stream.Collectors;

public class InvertedList implements Iterable<InvertedListEntry>{
    private List<InvertedListEntry> list;

    public InvertedList() {
        list = new LinkedList<>();
    }

    public void addTweet(Tweet tweet) {
        for (String word : Arrays.stream(tweet.getText().toLowerCase().split("\\s+")).distinct().collect(Collectors.toList())) {
            boolean newWord = true;
            for (InvertedListEntry entry : list) {
                if (entry.key.equals(word)) {
                    entry.tweetSet.add(tweet);
                    newWord = false;
                    break;
                }
            }
            if (newWord) {
                InvertedListEntry newEntry = new InvertedListEntry(word);
                newEntry.addTweet(tweet);
                list.add(newEntry);
            }
        }
    }

    public void addTweets(List<Tweet> tweetList) {
        for (Tweet tweet : tweetList) {
            addTweet(tweet);
        }
    }

    @Override
    public Iterator<InvertedListEntry> iterator() {
        return list.iterator();
    }

}
