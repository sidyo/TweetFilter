package com.tweetFilter.controller;

import com.tweetFilter.dto.Tweet;
import com.tweetFilter.service.TweetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/tweets")
@RequiredArgsConstructor
@Slf4j
public class TweetController {
    private final TweetService tweetService;

    @PutMapping("/add")
    @ResponseStatus(CREATED)
    public void addTweets(@RequestBody List<Tweet> tweetList) throws Exception {
        tweetService.addTweets(tweetList);
    }

    @PutMapping("/reset")
    @ResponseStatus(CREATED)
    public void reset(){
        tweetService.reset();
    }

    @GetMapping("/search")
    @ResponseStatus(OK)
    public Set<Tweet> search(@RequestBody(required = false) String filter, @RequestParam(required = false,name = "sentimento") String sentiment){
        return tweetService.search(filter, sentiment);
    }
}
