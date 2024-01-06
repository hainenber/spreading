package com.hainenber.spreading.hackernews;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@WireMockTest
@ContextConfiguration(classes = ItemScraperTestSchedulerConfig.class)
class ItemScraperTest {
    @Autowired
    private ItemScraper itemScraper;

    @Test
    public void testFetchLatestItemOnScheduled(WireMockRuntimeInfo wmRuntimeInfo) throws InterruptedException {
        stubFor(post("/v0/maxitem.json")
                .withHeader("Content-Type", containing("text/plain"))
                .willReturn(ok()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("1")
                )
        );

        itemScraper.fetchLatestItem();

        Thread.sleep(6000);
    }
}