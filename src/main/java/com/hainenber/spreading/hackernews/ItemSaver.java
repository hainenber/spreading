package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "hackernews.enabled", havingValue = "true")
public class ItemSaver {
    private final RestClient restClient;
    public final ItemRepository itemRepository;

    public ItemSaver(RestClient restClient, ItemRepository itemRepository) {
        this.restClient = restClient;
        this.itemRepository = itemRepository;
    }

    @RetryableTopic(
        attempts = "4",
        backoff = @Backoff(delay = 5000, multiplier = 1.0),
        autoCreateTopics = "false",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${hackernews.topics.id}")
    public void download(String itemId) {
        // Get ID from Kafka's topic and fetch content.
        Item item = restClient.get()
                .uri(String.format("https://hacker-news.firebaseio.com/v0/item/%s.json", itemId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Item.class);

        // Throws exception for retries if HN item is empty.
        if (Optional.ofNullable(item).isEmpty()) {
            throw new RuntimeException(String.format("Empty HN item %s", itemId));
        }

        // Throws exception to signify retrying if the text content is shown to be delayed.
        String itemText = item.getText();
        if (Optional.ofNullable(itemText).isPresent() && itemText.contentEquals("[delayed]")) {
            throw new RuntimeException(String.format("Delayed text content for HN item %s", itemId));
        }

        // Persist into database
        try {
            itemRepository.save(item);
            log.info("{} Save HackerNews's item: {}", Instant.now(), item.getId());
        } catch (DataException dataException) {
            log.error(dataException.getMessage(), item);
        }
    }
}
