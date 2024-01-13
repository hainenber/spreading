package com.hainenber.spreading.hackernews;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableConfigurationProperties
@WireMockTest
class ItemScraperTest {
    @Autowired
    private ItemScraper itemScraper;
    @Autowired
    private ItemSaver itemSaver;

    @BeforeEach
    public void setup() {
        WireMockServer wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        wireMockServer.stubFor(get("/v0/maxitem.json")
                .willReturn(ok()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("1")
                )
        );
        wireMockServer.stubFor(get("/v0/item/1.json")
                .willReturn(ok()
                        .withBodyFile("1.json")
                )
        );
    }

    @Test
    public void testFetchLatestItemId_DownloadContent_PersistIntoDatabase() throws InterruptedException {
        String itemId = itemScraper.fetchLatestItem();
        assertEquals("1", itemId);
        assertDoesNotThrow(() -> itemScraper.send(itemId));
        await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> itemSaver.itemRepository.existsById(itemId));
    }
}