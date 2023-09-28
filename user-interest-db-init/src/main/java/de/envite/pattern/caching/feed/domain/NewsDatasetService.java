package de.envite.pattern.caching.feed.domain;

import com.opencsv.bean.CsvToBeanBuilder;
import de.envite.pattern.caching.feed.config.AppProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Component
public class NewsDatasetService {

    private static final Logger log = LoggerFactory.getLogger(NewsDatasetService.class);

    private final AppProperties appProperties;

    private List<NewsEntry> newsEntries;

    public NewsDatasetService(@Autowired final AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Set<String> getCategories() {
        return newsEntries.stream()
                .map(NewsEntry::getCategory)
                .collect(toUnmodifiableSet());
    }

    @PostConstruct
    private void init() throws FileNotFoundException {
        log.info("Start parsing news dataset from file {}", appProperties.getNewsDatasetCsvFile().getAbsolutePath());
        final long startTimeMs = System.currentTimeMillis();
        newsEntries = new CsvToBeanBuilder<NewsEntry>(new FileReader(appProperties.getNewsDatasetCsvFile()))
                .withType(NewsEntry.class)
                .build()
                .parse();
        log.info("Completed parsing news dataset from file {} in {} ms",
                appProperties.getNewsDatasetCsvFile().getAbsolutePath(),
                (System.currentTimeMillis() - startTimeMs));
    }
}
