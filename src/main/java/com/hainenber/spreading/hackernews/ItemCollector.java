package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "hackernews.enabled", havingValue = "true")
public class ItemCollector {
    private final RestClient restClient;

    @Autowired
    private ItemCollector(RestClient restClient) {
        this.restClient = restClient;
    }

    @Scheduled(fixedRateString = "${hackernews.collectorIntervalMillisecond}")
    private void fetchLatestItem() {
        try {
            String latestItemUrl = "https://hacker-news.firebaseio.com/v0/maxitem.json";
            String itemId = restClient.get()
                    .uri(latestItemUrl)
                    .accept(MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);
            Instant collectTime = Instant.now();

            if (Optional.ofNullable(itemId).isEmpty()) {
                log.error("Empty HackerNews's item ID at {}", collectTime);
                return;
            }

            // TODO: decouple this into another Bean and use Kafka as mediator
            Item item = restClient.get()
                    .uri(String.format("https://hacker-news.firebaseio.com/v0//item/%s.json", itemId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Item.class);
            log.info("{} HackerNews's item: {}", collectTime, item);
        } catch (HttpClientErrorException e) {
            log.error("Encounter {} when trying to fetch HackerNews's latest item Id",
                e.getStatusCode()
            );
        }
    }
}
