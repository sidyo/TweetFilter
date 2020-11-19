package com.tweetFilter.service;

import com.tweetFilter.dto.Tweet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.*;

import static com.tweetFilter.utils.Constants.*;
import static com.tweetFilter.utils.Constants.POSITIVE;

@Service
@Slf4j
public class MLService {
    private final FilteredClassifier fc;
    private final Instances ins;
    @Getter
    private final Map<String, Set<Tweet>> map;

    public MLService() throws Exception {
        map = new HashMap<>();
        map.put(POSITIVE,new HashSet<>());
        map.put(NEGATIVE,new HashSet<>());
        map.put(NEUTRAL,new HashSet<>());
        String dataset = MULTIPLESETS;
        log.info("Started training with dataset: {}",dataset);
        ConverterUtils.DataSource ds = new ConverterUtils.DataSource("src/main/resources/"+dataset);
        ins = ds.getDataSet();
        ins.setClassIndex(1);

        J48 tree = new J48();
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(ins);
        filter.setIDFTransform(true);
        LovinsStemmer stemmer = new LovinsStemmer();
        filter.setStemmer(stemmer);
        filter.setLowerCaseTokens(true);

        fc = new FilteredClassifier();
        fc.setFilter(filter);
        fc.setClassifier(tree);
        fc.buildClassifier(ins);
        log.info("Finished Building Classifier. Ready to go.");
    }

    public void classifyTweets(List<Tweet> tweets) throws Exception {
        log.info("Starting ML tweet Classifier.");

        Instance novo = new DenseInstance(2);
        novo.setDataset(ins);
        for (Tweet t : tweets) {
            novo.setValue(0, t.getText());
            double[] probabilidade = fc.distributionForInstance(novo);
            //log.info("{} Negativo: '{}'. Neutro:'{}'. Positivo '{}'.",t.getText(),probabilidade[0],probabilidade[1], probabilidade[2]);
            if(probabilidade[0] > probabilidade[1] && probabilidade[0] > probabilidade[2]){
                t.setSentimentML(NEGATIVE);
                Set<Tweet> aux = map.get(NEGATIVE);
                aux.add(t);
                map.put(NEGATIVE,aux);
            } else if(probabilidade[1] > probabilidade[0] && probabilidade[1] > probabilidade[2]){
                t.setSentimentML(NEUTRAL);
                Set<Tweet> aux = map.get(NEUTRAL);
                aux.add(t);
                map.put(NEUTRAL,aux);
            }else{
                t.setSentimentML(POSITIVE);
                Set<Tweet> aux = map.get(POSITIVE);
                aux.add(t);
                map.put(POSITIVE,aux);
            }
        }
        log.info("ML Tweet classification finished.");
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

        for (Tweet t: tweets){
            switch (t.getExpectedSentiment()) {
                case POSITIVE:
                    countTotalPositive++;
                    if (t.getSentimentML().equals(POSITIVE)) {
                        countMLPositive++;
                    }
                    break;
                case (NEGATIVE):
                    countTotalNegative++;
                    if (t.getSentimentML().equals(NEGATIVE)) {
                        countMLNegative++;
                    }
                    break;
                case NEUTRAL:
                    countTotalNeutral++;
                    if (t.getSentimentML().equals(NEUTRAL)) {
                        countMLNeutral++;
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
        }
        log.info("\nResults: Total Entries: {}. Total Rights: {}. {}%\n" +
                        "Positives: Total: {}. ML Guesses: {}. ML Got Right: {}. {}%\n" +
                        "Negatives: Total: {}. ML Guesses: {}. ML Got Right: {}. {}%\n" +
                        "Neutrals: Total: {}. ML Guesses: {}. ML Got Right: {}. {}%\n" +
                        "Input Missing Sentiment: {}",
                tweets.size(),countMLNegative+countMLNeutral+countMLPositive,(double) (countMLNegative+countMLNeutral+countMLPositive)*100/tweets.size(),
                countTotalPositive, countMLPositiveGuess, countMLPositive, (double)countMLPositive*100/countTotalPositive,
                countTotalNegative, countMLNegativeGuess, countMLNegative, (double)countMLNegative*100/countTotalNegative,
                countTotalNeutral, countMLNeutralGuess, countMLNeutral, (double)countMLNeutral*100/countTotalNeutral,
                countTotalInputErrors
        );

    }
}
