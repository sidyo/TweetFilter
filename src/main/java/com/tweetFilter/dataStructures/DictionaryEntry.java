package com.tweetFilter.dataStructures;

public class DictionaryEntry {
    private final String id;
    private final String word;
    private final double posScore, negScore;

    public DictionaryEntry(String id, double posScore, double negScore, String word) {
        this.id = id;
        this.word = word;
        this.posScore = posScore;
        this.negScore = negScore;
    }

    public double getScore() {
        return posScore - negScore;
    }
}
