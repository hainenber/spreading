package com.hainenber.spreading.quote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnimeQuote(String anime, String character, String quote) {
    @Override
    public String toString() {
        return String.format("%s (%s): %s", character, anime, quote);
    }
}
