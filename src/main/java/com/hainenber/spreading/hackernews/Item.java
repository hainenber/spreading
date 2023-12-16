package com.hainenber.spreading.hackernews;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Item(
        String id,
        Boolean deleted,
        String type,
        Long time,
        String text,
        Boolean dead,
        Long parent,
        Long poll,
        List<Long> kids,
        String url,
        Number score,
        String title,
        List<Long> parts,
        Number descendants
) { }
