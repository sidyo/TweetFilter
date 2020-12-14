package com.tweetFilter.service;

import com.tweetFilter.dataStructures.DictionaryEntry;
import com.tweetFilter.dto.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static com.tweetFilter.utils.Constants.*;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class LexicalDictionaryService {

    private HashMap<String, DictionaryEntry> lexicalDictionary;
    private long totalQueries, totalMisses;

    public LexicalDictionaryService() {
        lexicalDictionary = new HashMap<>();
        totalQueries = 0;
        totalMisses = 0;
        buildDictionary(DICTPATH);
    }

    public void printMisses(){
        log.info("Total Queries on Dictionary: {}. Total Misses: {} ({}%)",totalQueries,totalMisses, (double) totalMisses*100/totalQueries);
    }

    private double getScore(String word){
        DictionaryEntry entry = lexicalDictionary.get(word);
        totalQueries++;
        if(isNull(entry)){
            totalMisses++;
            return 0;
        }else{
            return entry.getScore();
        }
    }

    public void classifyTweets(List<Tweet> tweetList){
        for(Tweet t: tweetList){
            classifyTweet(t);
        }
    }

    private void classifyTweet(Tweet t) {
        String[] words = t.getText().split(" ");
        double totalScore = 0;
        for(String word: words){
            totalScore+= getScore(word);
        }
        if(totalScore == 0){
            t.setSentimentDict(NEUTRAL);
        }else if(totalScore > 0){
            t.setSentimentDict(POSITIVE);
        }else {
            t.setSentimentDict(NEGATIVE);
        }
    }

    private void buildDictionary(String filepath) {
        log.info("Started building dictionary with {}.",filepath);
        try {
            Scanner scanner = new Scanner(new File(filepath));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().charAt(0) != '#') {

                    String[] splitLine = line.split("\t");
                    String id = splitLine[0];
                    double posScore = Double.parseDouble(splitLine[1]);
                    double negScore = Double.parseDouble(splitLine[2]);
                    String word = "";
                    for (int i = 3; i < splitLine.length; i++) {
                        word = word.concat(splitLine[i]);
                    }

                    DictionaryEntry newEntry = new DictionaryEntry(id, posScore, negScore, word);
                    lexicalDictionary.put(word, newEntry);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            log.error("Dictionary file not found.", e);
        }
        log.info("Finished building dictionary.");
    }

}
