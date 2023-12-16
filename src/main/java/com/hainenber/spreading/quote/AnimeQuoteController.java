package com.hainenber.spreading.quote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AnimeQuoteController {
    private final RestClient restClient;
    private final AtomicLong counter = new AtomicLong();

    @Autowired
    private AnimeQuoteController(RestClient restClient) {
        this.restClient = restClient;
    }

    private ZonedDateTime getReadableRateLimitResetTime(HttpHeaders headers) {
        ZoneId localTimezone = ZoneId.systemDefault();
        return Instant.ofEpochMilli(
                Long.parseLong(
                        Objects.requireNonNull(
                                headers.get("x-ratelimit-reset")
                        ).getFirst()
                )
        )
                .atZone(localTimezone);
    }

    // @Scheduled(fixedRateString = "${animeQuote.collectorIntervalMillisecond}")
    private void runTask() throws HttpClientErrorException.TooManyRequests {
        String animeQuoteURL = "https://animechan.xyz/api/random";
        try {
            AnimeQuote quote = restClient.get()
                    .uri(animeQuoteURL)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(AnimeQuote.class);
            if (Optional.ofNullable(quote).isPresent()) {
                System.out.printf("Collect #%s: %s\n", counter.incrementAndGet(), quote);
            }
        } catch (HttpClientErrorException.TooManyRequests e)  {
            log.error("Got 429 from {}. Next retry at {}",
                    animeQuoteURL,
                    getReadableRateLimitResetTime(Objects.requireNonNull(e.getResponseHeaders()))
            );

        }
    }
}
