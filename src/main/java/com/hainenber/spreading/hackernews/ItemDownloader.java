package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Slf4j
@Service
@ConditionalOnProperty(name = "hackernews.enabled", havingValue = "true")
public class ItemDownloader {
    private final RestClient restClient;

    public ItemDownloader(RestClient restClient) {
        this.restClient = restClient;
    }

    @KafkaListener(topics = "${hackernews.topic}")
    private void download(String itemId) {
        Item item = restClient.get()
                .uri(String.format("https://hacker-news.firebaseio.com/v0//item/%s.json", itemId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Item.class);
        log.info("{} HackerNews's item: {}", Instant.now(), item);
    }
}
