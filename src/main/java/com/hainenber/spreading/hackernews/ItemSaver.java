package com.hainenber.spreading.hackernews;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(name = "hackernews.enabled", havingValue = "true")
public class ItemSaver {
    private final RestClient restClient;
    @Autowired
    private final ItemRepository itemRepository;
    private String lastItemId;

    public ItemSaver(RestClient restClient, ItemRepository itemRepository) {
        this.restClient = restClient;
        this.itemRepository = itemRepository;
        this.lastItemId = "";
    }

    @KafkaListener(topics = "${hackernews.topic}")
    private void download(String itemId) {
        // Get IDs from Kafka's topic and fetch content
        Item item = restClient.get()
                .uri(String.format("https://hacker-news.firebaseio.com/v0//item/%s.json", itemId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Item.class);

        // Persist into database if fetched Item is not null AND not duplicating
        boolean itemIsNotNull = Optional.ofNullable(item).isPresent();
        if (itemIsNotNull && !Objects.equals(item.getId(), this.lastItemId)) {
            try {
                itemRepository.save(item);
            } catch (DataException dataException) {
                log.error(dataException.getMessage(), item);
            }
        }

        // Cache item ID for next iteration's check
        if (itemIsNotNull) {
            this.lastItemId = item.getId();
        }

        log.info("{} HackerNews's item: {}", Instant.now(), item);
    }
}
