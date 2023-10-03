package de.envite.pattern.caching.userinterest.init.domain;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.envite.pattern.caching.userinterest.init.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Component
public class UsersDatasetService {

    private static final Logger log = LoggerFactory.getLogger(UsersDatasetService.class);

    private final AppProperties appProperties;

    private final List<String> users = new LinkedList<>();

    public UsersDatasetService(@Autowired final AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public List<String> getUsers(final int limit) {
        if (users.size() < limit) {
            synchronized (users) {
                if (users.size() < limit) {
                    parseCsv(limit);
                }
            }
        }
        return unmodifiableList(users.subList(0, limit));
    }

    private void parseCsv(final int limit) {
        log.info("Start parsing user dataset from file {}", appProperties.getUsersDatasetCsvFile().getAbsolutePath());
        final var startTimeMs = System.currentTimeMillis();
        try (final var reader = new CSVReader(new FileReader(appProperties.getUsersDatasetCsvFile()))) {
            reader.skip(1 + users.size()); // skip header and already parsed lines
            while (users.size() < limit && readNextUser(reader));
        } catch (final IOException e) {
            throw new DatasetException(String.format("Error parsing users dataset file %s.", appProperties.getUsersDatasetCsvFile()), e);
        }
        log.info("Completed parsing user dataset from file {} in {} ms",
                appProperties.getUsersDatasetCsvFile().getAbsolutePath(),
                (System.currentTimeMillis() - startTimeMs));
    }

    private boolean readNextUser(final CSVReader reader) throws IOException {
        try {
            final var lineInArray = reader.readNext();
            if (lineInArray == null) {
                return false;
            }
            users.add(lineInArray[0]);
        } catch (final CsvValidationException e) {
            log.warn(String.format("Line %d of users dataset file %s is invalid. Skipping line and continue.",
                    e.getLineNumber(), appProperties.getUsersDatasetCsvFile().toString()), e);
            reader.skip(1);
        }
        return true;
    }

}
