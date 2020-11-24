package com.tweetFilter.service;

import com.tweetFilter.dataStructures.InvertedList;
import com.tweetFilter.dataStructures.Trie;
import com.tweetFilter.dto.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tweetFilter.utils.Constants.*;

@Service
@Slf4j
public class TweetService {

    private Set<Tweet> tweets;
    private Trie trie;
    @Autowired
    private MLService mlService;
    @Autowired
    private LexicalDictionaryService lexicalDictionaryService;

    public TweetService() throws Exception {
        tweets = new HashSet<>();
        trie = new Trie();
    }

    public void reset() {
        log.info("Received request to reset data structures.");
        tweets = new HashSet<>();
        trie = new Trie();
        System.gc();
        log.info("Data structures have been reset.");
    }

    public void addTweets(List<Tweet> tweetList) throws Exception {
        log.info("Received tweets: {}", tweetList.size());
        this.tweets.addAll(tweetList);
        InvertedList temporaryList = new InvertedList();
        Instant start = Instant.now();
        temporaryList.addTweets(tweetList);
        Instant end = Instant.now();
        log.info("Total unique terms: {}. Elapsed time building Inverted List {} millis.", temporaryList.getList().size(), Duration.between(start, end).toMillis());
        start = Instant.now();
        trie.addAll(temporaryList);
        end = Instant.now();
        log.info("Finished building trie. Elapsed time building trie {} millis.", Duration.between(start, end).toMillis());

        classifyTweets(tweetList);
        printHitPercentage(tweetList);
    }

    public Set<Tweet> search(String filter) {
        log.info("Received filter: {}", filter);
        Instant start = Instant.now();
        String[] filters = filter.trim().toLowerCase().split("\\s+");
        if (filters.length % 2 == 0) {
            return null;
        }
        Set<Tweet> resultSet = getTweets(filters[0]);

        for (int i = 1; i < filters.length; i = i + 2) {
            Set<Tweet> nextFilter = getTweets(filters[i + 1]);
            switch (filters[i]) {
                case AND:
                    resultSet.retainAll(nextFilter);
                    break;
                case OR:
                    resultSet.addAll(nextFilter);
                    break;
            }
        }
        Instant end = Instant.now();
        log.info("Total tweets matching filter: {}. Total elapsed time: {} millis.", resultSet.size(), Duration.between(start, end).toMillis());
        return resultSet;
    }

    private Set<Tweet> getTweets(String filter) {
        Set<Tweet> resultSet;
        if (filter.startsWith(NOT)) {
            resultSet = new HashSet<>(tweets);
            Set<Tweet> aux = trie.getTweetList(filter.substring(1));
            resultSet.removeAll(aux);
        } else {
            resultSet = new HashSet<>(trie.getTweetList(filter));
        }
        return resultSet;
    }

    private void classifyTweets(List<Tweet> tweets) throws Exception {
        mlService.classifyTweets(tweets);
        lexicalDictionaryService.classifyTweets(tweets);
    }

    public void printHitPercentage(List<Tweet> tweets) {
        int countTotalNegative = 0;
        int countTotalPositive = 0;
        int countTotalNeutral = 0;

        int countTotalInputErrors = 0;

        int countMLPositiveGuess = 0;
        int countMLNegativeGuess = 0;
        int countMLNeutralGuess = 0;

        int countMLNegative = 0;
        int countMLPositive = 0;
        int countMLNeutral = 0;

        int countDictPositiveGuess = 0;
        int countDictNegativeGuess = 0;
        int countDictNeutralGuess = 0;

        int countDictNegative = 0;
        int countDictPositive = 0;
        int countDictNeutral = 0;

        for (Tweet t: tweets){
            switch (t.getExpectedSentiment()) {
                case POSITIVE:
                    countTotalPositive++;
                    if (t.getSentimentML().equals(POSITIVE)) {
                        countMLPositive++;
                    }
                    if(t.getSentimentDict().equals(POSITIVE)) {
                        countDictPositive++;
                    }
                    break;
                case (NEGATIVE):
                    countTotalNegative++;
                    if (t.getSentimentML().equals(NEGATIVE)) {
                        countMLNegative++;
                    }
                    if(t.getSentimentDict().equals(NEGATIVE)) {
                        countDictNegative++;
                    }
                    break;
                case NEUTRAL:
                    countTotalNeutral++;
                    if (t.getSentimentML().equals(NEUTRAL)) {
                        countMLNeutral++;
                    }
                    if(t.getSentimentDict().equals(NEUTRAL)) {
                        countDictNeutral++;
                    }
                    break;
                default:
                    countTotalInputErrors++;
                    break;
            }
            switch ((t.getSentimentML())){
                case POSITIVE:countMLPositiveGuess++;break;
                case NEGATIVE:countMLNegativeGuess++;break;
                case NEUTRAL:countMLNeutralGuess++;break;
            }
            switch ((t.getSentimentDict())){
                case POSITIVE:countDictPositiveGuess++;break;
                case NEGATIVE:countDictNegativeGuess++;break;
                case NEUTRAL:countDictNeutralGuess++;break;
            }
        }
        log.info("\nResults: Total Entries: {}. Total ML Hits: {} ({}%). Total Dict Hits: {} ({}%)\n" +
                        "Positives: Total: {}. ML Guesses: {}. ML Got Right: {} ({}%). Dict Guesses: {}. Dict Got Right: {} ({}%)\n" +
                        "Negatives: Total: {}. ML Guesses: {}. ML Got Right: {} ({}%). Dict Guesses: {}. Dict Got Right: {} ({}%)\n" +
                        "Neutrals: Total: {}. ML Guesses: {}. ML Got Right: {} ({}%). Dict Guesses: {}. Dict Got Right: {} ({}%)\n" +
                        "Input Missing Sentiment: {}",
                tweets.size(), countMLNegative+countMLNeutral+countMLPositive,(double) (countMLNegative+countMLNeutral+countMLPositive)*100/tweets.size(), countDictNegative+countDictNeutral+countDictPositive, (double)(countDictNegative+countDictNeutral+countDictPositive)*100/tweets.size(),
                countTotalPositive, countMLPositiveGuess, countMLPositive, (double)countMLPositive*100/countTotalPositive, countDictPositiveGuess, countDictPositive, (double) countDictPositive*100/countTotalPositive,
                countTotalNegative, countMLNegativeGuess, countMLNegative, (double)countMLNegative*100/countTotalNegative, countDictNegativeGuess, countDictNegative, (double) countDictNegative*100/countTotalNegative,
                countTotalNeutral, countMLNeutralGuess, countMLNeutral, (double)countMLNeutral*100/countTotalNeutral, countDictNeutralGuess, countDictNeutral, (double) countDictNeutral*100/countTotalNeutral,
                countTotalInputErrors
        );
        lexicalDictionaryService.printMisses();
    }
}
