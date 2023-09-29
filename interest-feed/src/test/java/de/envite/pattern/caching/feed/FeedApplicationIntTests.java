package de.envite.pattern.caching.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.envite.pattern.caching.feed.adapter.NewsEntry;
import de.envite.pattern.caching.feed.adapter.NewsResponse;
import de.envite.pattern.caching.feed.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.feed.domain.FeedEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@WireMockTest
class FeedApplicationIntTests {

    static int feedLimit = 2;
    static Period feedPeriod = Period.ofMonths(1);

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.2.1-alpine")).withExposedPorts(6379);

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("service.news.url", wireMock::baseUrl);
        registry.add("feed.limit", () -> Integer.toString(feedLimit));
        registry.add("feed.period", () -> feedPeriod.toString());
    }

    @LocalServerPort
    int serverPort;

    @Autowired
    ObjectMapper om;

    @Autowired
    RedisTemplate<String, Set<String>> redisTemplate;

    RestTemplate restTemplate;

    @BeforeEach
    void setupRestTemplate() {
        restTemplate = new RestTemplateBuilder().uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + serverPort)).build();
    }

    @AfterEach
    void cleanupWiremock() {
        wireMock.resetAll();
    }

    @Test
    void shouldReturnFeed_WithRecommendedNews() throws JsonProcessingException {
        redisTemplate.opsForValue().set("ada", Set.of("U.S. NEWS"));

        wireMock.stubFor(
                post(urlEqualTo("/recommendedNews"))
                        .withRequestBody(equalToJson(om.writeValueAsString(new RecommendedNewsQuery(Set.of("U.S. NEWS"), LocalDate.parse("2022-09-23").minus(feedPeriod), LocalDate.parse("2022-09-23"), feedLimit))))
                        .willReturn(okJson(om.writeValueAsString(new NewsResponse(List.of(
                                new NewsEntry(
                                        1234,
                                        "https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
                                        "Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
                                        "U.S. NEWS",
                                        "Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
                                        "Carla K. Johnson, AP",
                                        "2022-09-23"),
                                new NewsEntry(
                                        1345,
                                        "https://www.huffpost.com/entry/american-airlines-passenger-banned-flight-attendant-punch-justice-department_n_632e25d3e4b0e247890329fe",
                                        "American Airlines Flyer Charged, Banned For Life After Punching Flight Attendant On Video",
                                        "U.S. NEWS",
                                        "He was subdued by passengers and crew when he fled to the back of the aircraft after the confrontation, according to the U.S. attorney's office in Los Angeles.",
                                        "Mary Papenfuss",
                                        "2022-09-22")
                        ))))));

        FeedEntry[] feedEntries = restTemplate.getForObject("/feed/ada?date=2022-09-23", FeedEntry[].class);
        assertThat(feedEntries)
                .hasSize(feedLimit)
                .contains(
                        new FeedEntry(
                                "https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
                                "Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
                                "U.S. NEWS",
                                "Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
                                "Carla K. Johnson, AP",
                                LocalDate.parse("2022-09-23")
                        ),
                        new FeedEntry(
                                "https://www.huffpost.com/entry/american-airlines-passenger-banned-flight-attendant-punch-justice-department_n_632e25d3e4b0e247890329fe",
                                "American Airlines Flyer Charged, Banned For Life After Punching Flight Attendant On Video",
                                "U.S. NEWS",
                                "He was subdued by passengers and crew when he fled to the back of the aircraft after the confrontation, according to the U.S. attorney's office in Los Angeles.",
                                "Mary Papenfuss",
                                LocalDate.parse("2022-09-22")
                        )
                );
    }

    @Test
    void shouldReturnFeed_WithRecommendedNewsAndLatestNews() throws JsonProcessingException {
        redisTemplate.opsForValue().set("ada", Set.of("U.S. NEWS"));

        wireMock.stubFor(
                post(urlEqualTo("/recommendedNews"))
                        .withRequestBody(equalToJson(om.writeValueAsString(new RecommendedNewsQuery(Set.of("U.S. NEWS"), LocalDate.parse("2022-09-23").minus(feedPeriod), LocalDate.parse("2022-09-23"), feedLimit))))
                        .willReturn(okJson(om.writeValueAsString(new NewsResponse(List.of(
                                new NewsEntry(
                                        1234,
                                        "https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
                                        "Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
                                        "U.S. NEWS",
                                        "Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
                                        "Carla K. Johnson, AP",
                                        "2022-09-23")
                        ))))));
        wireMock.stubFor(
                get(urlPathEqualTo("/latestNews")).withQueryParam("untilDate", equalTo("2022-09-23")).withQueryParam("limit", equalTo("1"))
                        .willReturn(okJson(om.writeValueAsString(new NewsResponse(List.of(
                                new NewsEntry(
                                        1456,
                                        "https://www.huffpost.com/entry/funniest-tweets-cats-dogs-september-17-23_n_632de332e4b0695c1d81dc02",
                                        "23 Of The Funniest Tweets About Cats And Dogs This Week (Sept. 17-23)",
                                        "COMEDY",
                                        "Until you have a dog you don't understand what could be eaten.",
                                        "Elyse Wanshel",
                                        "2022-09-22")
                        )))))
        );

        FeedEntry[] feedEntries = restTemplate.getForObject("/feed/ada?date=2022-09-23", FeedEntry[].class);
        assertThat(feedEntries)
                .hasSize(feedLimit)
                .contains(
                        new FeedEntry(
                                "https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
                                "Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
                                "U.S. NEWS",
                                "Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
                                "Carla K. Johnson, AP",
                                LocalDate.parse("2022-09-23")
                        ),
                        new FeedEntry(
                                "https://www.huffpost.com/entry/funniest-tweets-cats-dogs-september-17-23_n_632de332e4b0695c1d81dc02",
                                "23 Of The Funniest Tweets About Cats And Dogs This Week (Sept. 17-23)",
                                "COMEDY",
                                "Until you have a dog you don't understand what could be eaten.",
                                "Elyse Wanshel",
                                LocalDate.parse("2022-09-22")
                        )
                );
    }

    @Test
    void shouldReturnUsernames_WithoutLimit() {
        redisTemplate.opsForValue().set("ada", Set.of("U.S. NEWS"));
        redisTemplate.opsForValue().set("supposed2bworking", Set.of("SPORTS"));
        redisTemplate.opsForValue().set("mightbjosh", Set.of("U.S. NEWS", "COMEDY"));
        redisTemplate.opsForValue().set("finn2605", Set.of("WEIRD NEWS", "COMEDY"));

        String[] usernames = restTemplate.getForObject("/usernames", String[].class);
        assertThat(usernames)
                .hasSize(4)
                .contains("ada", "supposed2bworking", "mightbjosh", "finn2605");
    }

    @Test
    void shouldReturnUsernames_WithLimit() {
        redisTemplate.opsForValue().set("ada", Set.of("U.S. NEWS"));
        redisTemplate.opsForValue().set("supposed2bworking", Set.of("SPORTS"));
        redisTemplate.opsForValue().set("mightbjosh", Set.of("U.S. NEWS", "COMEDY"));
        redisTemplate.opsForValue().set("finn2605", Set.of("WEIRD NEWS", "COMEDY"));

        String[] usernames = restTemplate.getForObject("/usernames?limit=3", String[].class);
        assertThat(usernames).hasSize(3);
    }
}
