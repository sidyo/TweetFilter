package com.tweetFilter.service;

import com.tweetFilter.dataStructures.InvertedList;
import com.tweetFilter.dataStructures.Trie;
import com.tweetFilter.dto.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tweetFilter.utils.Constants.*;

@Service
@Slf4j
public class TweetService {

    private Set<Tweet> tweets;
    private Trie trie;


    public TweetService() {
        tweets = new HashSet<>();
        trie = new Trie();
    }

    public void reset() {
        tweets = new HashSet<>();
        trie = new Trie();
    }

    public void addTweets(List<Tweet> tweetList) {
        this.tweets.addAll(tweetList);
        InvertedList temporaryList = new InvertedList();
        temporaryList.addTweets(tweetList);
        trie.addAll(temporaryList);
    }

    public Set<Tweet> search(String filter) {
        String[] filters = filter.trim().toLowerCase().split("\\s+");
        if(filters.length % 2 == 0){
            return null;
        }
        Set<Tweet> resultSet = getTweets(filters[0]);

        for (int i = 1; i < filters.length; i=i+2) {
            Set<Tweet> nextFilter = getTweets(filters[i+1]);
            switch (filters[i]){
                case AND:resultSet.retainAll(nextFilter);break;
                case OR: resultSet.addAll(nextFilter); break;
            }
        }
        return resultSet;
    }

    private Set<Tweet> getTweets(String filter){
        Set<Tweet> resultSet;
        if(filter.startsWith(NOT)){
            resultSet = new HashSet<>(tweets);
            Set<Tweet> aux = trie.getTweetList(filter.substring(1));
            resultSet.removeAll(aux);
        }else{
            resultSet = new HashSet<>(trie.getTweetList(filter));
        }
        return resultSet;
    }
}
