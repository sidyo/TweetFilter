package com.tweetFilter.dataStructures;

import com.tweetFilter.dto.Tweet;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Getter
public class TrieNode {
    private Set<Tweet> tweets;
    private HashMap<Character, TrieNode> children;

    public TrieNode() {
        tweets = new HashSet<>();
        children = new HashMap<>();
    }

    public void addTweets(Set<Tweet> tweetList){
        tweets.addAll(tweetList);
    }

    public void addChild(char c){
        children.put(c, new TrieNode());
    }

    public TrieNode getChild(char c){
        return children.get(c);
    }
}