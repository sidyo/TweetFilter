package com.tweetFilter.dataStructures;

import com.tweetFilter.dto.Tweet;
import lombok.Getter;

import java.util.*;

import static java.util.Objects.isNull;

@Getter
public class Trie {
    TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public void add(InvertedListEntry entry) {
        TrieNode currentNode = root;
        TrieNode nextNode;
        for (Character c : entry.getKey().toCharArray()) {
            nextNode = currentNode.getChild(c);
            if (isNull(nextNode)) {
                currentNode.addChild(c);
                nextNode = currentNode.getChild(c);
            }
            currentNode = nextNode;
        }
        currentNode.addTweets(entry.getTweetSet());
    }

    public void addAll(InvertedList invertedList) {
        Iterator<Map.Entry<String, InvertedListEntry>> it = invertedList.getList().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, InvertedListEntry> pair = it.next();
            add(pair.getValue());
        }
    }

    public Set<Tweet> getTweetList(String key) {
        TrieNode currentNode = root;
        for (Character c : key.toCharArray()) {
            currentNode = currentNode.getChild(c);
            if (isNull(currentNode)) {
                return new HashSet<>();
            }
        }
        return currentNode.getTweets();
    }

}
