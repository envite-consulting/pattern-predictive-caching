package de.envite.pattern.caching.feed.domain;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.envite.pattern.caching.feed.config.AppProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

@Component
public class UsersDatasetService {

    private static final Logger log = LoggerFactory.getLogger(UsersDatasetService.class);

    private final AppProperties appProperties;

    private final Set<String> users = new HashSet<>();

    public UsersDatasetService(@Autowired final AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Set<String> getUsers() {
        return unmodifiableSet(users);
    }

    @PostConstruct
    private void init() throws IOException, CsvValidationException {
        log.info("Start parsing user dataset from file {}", appProperties.getUsersDatasetCsvFile().getAbsolutePath());
        final long startTimeMs = System.currentTimeMillis();
        try (final CSVReader reader = new CSVReader(new FileReader(appProperties.getUsersDatasetCsvFile()))) {
            reader.readNext(); // skip header
            String[] lineInArray;
            while ((lineInArray = reader.readNext()) != null) {
                users.add(lineInArray[0]);
            }
        }
        log.info("Completed parsing user dataset from file {} in {} ms",
                appProperties.getUsersDatasetCsvFile().getAbsolutePath(),
                (System.currentTimeMillis() - startTimeMs));
    }

}
