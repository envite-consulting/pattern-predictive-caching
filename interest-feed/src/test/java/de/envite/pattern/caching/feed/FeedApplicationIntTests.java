package de.envite.pattern.caching.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.envite.pattern.caching.feed.adapter.NewsEntry;
import de.envite.pattern.caching.feed.adapter.RecommendedNews;
import de.envite.pattern.caching.feed.adapter.RecommendedNewsQuery;
import de.envite.pattern.caching.feed.config.FeedProperties;
import de.envite.pattern.caching.feed.domain.FeedEntry;
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
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@WireMockTest
class FeedApplicationIntTests {

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
	}

	@LocalServerPort
	int serverPort;
	@Autowired
	FeedProperties feedProperties;

	@Autowired
	ObjectMapper om;

	@Autowired
	RedisTemplate<String, Set<String>> redisTemplate;

	RestTemplate restTemplate;

	@BeforeEach
	void setupRestTemplate() {
		restTemplate = new RestTemplateBuilder().uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + serverPort)).build();
	}

	@Test
	void shouldReturnFeed() throws JsonProcessingException {
		redisTemplate.opsForValue().set("ada", Set.of("U.S. NEWS"));

		wireMock.stubFor(
				post(urlEqualTo("/recommendedNews"))
				.withRequestBody(equalToJson(om.writeValueAsString(new RecommendedNewsQuery(Set.of("U.S. NEWS"), LocalDate.parse("2022-09-23").minus(feedProperties.getPeriod()), LocalDate.parse("2022-09-23"), feedProperties.getLimit()))))
				.willReturn(okJson(om.writeValueAsString(new RecommendedNews(List.of(
						new NewsEntry(
								1234,
								"https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
								"Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
								"U.S. NEWS",
								"Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
								"Carla K. Johnson, AP",
								"2022-09-23")))))));

		FeedEntry[] feedEntries = restTemplate.getForObject("/feed/ada?date=2022-09-23", FeedEntry[].class);
		assertThat(feedEntries)
				.hasSize(1)
				.contains(new FeedEntry(
						"https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
						"Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
						"U.S. NEWS",
						"Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
						"Carla K. Johnson, AP",
						LocalDate.parse("2022-09-23")
				));
	}

}
