package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
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
    private String lastItemId;

    @Value(value = "${hackernews.topics.id}")
    private String hackernewsTopic;

    @Value(value = "${hackernews.api-url}")
    private String apiUrl;

    private ItemScraper(RestClient restClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.restClient = restClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String fetchLatestItem() {
        try {
            String itemId = restClient.get()
                    .uri(apiUrl)
                    .accept(MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);
            Instant collectTime = Instant.now();

            // Log error and exit early if seeing empty item ID or duplicating
            if (Optional.ofNullable(itemId).isEmpty()) {
                log.error("Empty HackerNews item ID at {}", collectTime);
                return "";
            } else if (itemId.equals(this.lastItemId)) {
                log.error("Duplicate HN item {}. Skip sending", itemId);
                return "";
            }

            return itemId;
        } catch (HttpClientErrorException e) {
            log.error("Encounter {} when trying to fetch HackerNews's latest item ID at URL {}",
                    e.getStatusCode(),
                    apiUrl
            );
        }

        return "";
    }

    @Scheduled(fixedRateString = "${hackernews.collector-interval-millisecond}")
    public void fetchAndSend() {
        String itemId = fetchLatestItem();
        if (!itemId.isEmpty()) {
            send(itemId);
        }
    }

    public void send(String itemId) throws KafkaException {
        // Send to Kafka topic for further processing
        kafkaTemplate
            .send(hackernewsTopic, itemId)
            .whenComplete((result, ex) -> {
                if (Optional.ofNullable(ex).isEmpty()) {
                    log.info("Sent message=[{}] with offset=[{}]",
                            itemId,
                            result.getRecordMetadata().offset()
                    );
                    this.lastItemId = itemId;
                } else {
                    log.error("Unable to send message=[{}] due to: {}",
                            itemId,
                            ex.getMessage()
                    );
                    throw new KafkaException("unable to send message", ex);
                }
        });
    }
}
