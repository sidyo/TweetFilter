package com.tweetFilter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@ToString
@Getter
@EqualsAndHashCode
public class Tweet implements Serializable {

    private static final long serialVersionUID = 3487805389649513150L;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created_at;
    @JsonProperty
    private String username;
    @JsonProperty
    private int retweets;
    @JsonProperty
    private int favorites;
    @JsonProperty
    private String text;
    @JsonProperty
    private long id;

}
