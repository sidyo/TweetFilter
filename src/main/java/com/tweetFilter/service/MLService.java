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
        map.put(POSITIVE, new HashSet<>());
        map.put(NEGATIVE, new HashSet<>());
        map.put(NEUTRAL, new HashSet<>());
        String dataset = MULTIPLESETS;
        log.info("Started ML Training With Dataset: {}", dataset);
        ConverterUtils.DataSource ds = new ConverterUtils.DataSource("src/main/resources/" + dataset);
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
        log.info("Finished ML Training.");
    }

    public void classifyTweets(List<Tweet> tweets) throws Exception {
        log.info("Starting ML tweet Classifier.");

        Instance novo = new DenseInstance(2);
        novo.setDataset(ins);
        for (Tweet t : tweets) {
            novo.setValue(0, t.getText());
            double[] probabilidade = fc.distributionForInstance(novo);
            //log.info("{} Negativo: '{}'. Neutro:'{}'. Positivo '{}'.",t.getText(),probabilidade[0],probabilidade[1], probabilidade[2]);
            if (probabilidade[0] > probabilidade[1] && probabilidade[0] > probabilidade[2]) {
                t.setSentimentML(NEGATIVE);
                Set<Tweet> aux = map.get(NEGATIVE);
                aux.add(t);
                map.put(NEGATIVE, aux);
            } else if (probabilidade[1] > probabilidade[0] && probabilidade[1] > probabilidade[2]) {
                t.setSentimentML(NEUTRAL);
                Set<Tweet> aux = map.get(NEUTRAL);
                aux.add(t);
                map.put(NEUTRAL, aux);
            } else {
                t.setSentimentML(POSITIVE);
                Set<Tweet> aux = map.get(POSITIVE);
                aux.add(t);
                map.put(POSITIVE, aux);
            }
        }
        log.info("ML Tweet classification finished.");
    }
}
