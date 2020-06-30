package com.tweetFilter.dataStructures;

import com.tweetFilter.dto.Tweet;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class InvertedListEntry {
    String key;
    Set<Tweet> tweetSet;

    public InvertedListEntry(String key) {
        this.key = key;
        tweetSet = new HashSet<>();
    }

    public void addTweet(Tweet tweet){
        tweetSet.add(tweet);
    }
}
