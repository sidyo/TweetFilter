package com.tweetFilter.dataStructures;

import com.tweetFilter.dto.Tweet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Getter
@Slf4j
public class InvertedList {
    private HashMap<String, InvertedListEntry> list;

    public InvertedList() {
        list = new HashMap<>();
    }

    public void addTweet(Tweet tweet) {
        for (String word : Arrays.stream(tweet.getText().toLowerCase().split("\\s+")).distinct().collect(Collectors.toList())) {
            InvertedListEntry entry = list.get(word);
            if(isNull(entry)){
                entry = new InvertedListEntry(word);
                list.put(word,entry);
            }
            entry.addTweet(tweet);
        }
    }

    public void addTweets(List<Tweet> tweetList) {
        int count = 0;
        Instant start = Instant.now();
        Instant now;
        for (Tweet tweet : tweetList) {
            count++;
            if(count % 500 == 0){
                now = Instant.now();
                log.info("Inserted {} tweets. List size: {}. Elapsed time: {} milis.",count, list.size(), Duration.between(start,now).toMillis());
            }
            addTweet(tweet);
        }
    }

    /*@Override
    public Iterator<InvertedListEntry> iterator() {
        //return list.iterator();
        return list.entrySet().iterator();
    }*/

}
