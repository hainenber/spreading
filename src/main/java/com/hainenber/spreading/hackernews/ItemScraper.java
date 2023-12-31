package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "hackernews.enabled", havingValue = "true")
public class ItemScraper {
    private final RestClient restClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${hackernews.topic}")
    private String hackernewsTopic;

    private ItemScraper(RestClient restClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.restClient = restClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRateString = "${hackernews.collector-interval-millisecond}")
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
            }

            // Send to Kafka topic for further processing
            kafkaTemplate
                    .send(hackernewsTopic, itemId)
                    .whenComplete((result, ex) -> {
                        if (Optional.ofNullable(ex).isEmpty()) {
                            log.info("Sent message=[{}] with offset=[{}]",
                                    itemId,
                                    result.getRecordMetadata().offset()
                            );
                        } else {
                            log.error("Unable to send message=[{}] due to: {}",
                                    itemId,
                                    ex.getMessage()
                            );
                        }
                    });
        } catch (HttpClientErrorException e) {
            log.error("Encounter {} when trying to fetch HackerNews's latest item Id",
                e.getStatusCode()
            );
        }
    }
}
