package de.envite.pattern.caching.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.envite.pattern.caching.feed.adapter.RecommendedNews;
import de.envite.pattern.caching.feed.domain.FeedEntry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@WireMockTest
class FeedApplicationIntTests {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(wireMockConfig().dynamicPort())
			.build();

	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("service.news.url", wireMock::baseUrl);
	}

	static ObjectMapper om = new ObjectMapper();

	@LocalServerPort
	int serverPort;

	RestTemplate restTemplate;

	@BeforeEach
	void setupRestTemplate() {
		restTemplate = new RestTemplateBuilder().uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + serverPort)).build();
	}

	@Test
	void shouldReturnFeed() throws JsonProcessingException {
		wireMock.stubFor(post(urlEqualTo("/recommendedNews"))
				.willReturn(okJson(om.writeValueAsString(List.of(
						new RecommendedNews(
								"https://www.huffpost.com/entry/covid-boosters-uptake-us_n_632d719ee4b087fae6feaac9",
								"Over 4 Million Americans Roll Up Sleeves For Omicron-Targeted COVID Boosters",
								"U.S. NEWS",
								"Health experts said it is too early to predict whether demand would match up with the 171 million doses of the new boosters the U.S. ordered for the fall.",
								"Carla K. Johnson, AP",
								"2022-09-23"))))));

		FeedEntry[] feedEntries = restTemplate.getForObject("/feed/ada", FeedEntry[].class);
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
