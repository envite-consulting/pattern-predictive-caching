package de.envite.pattern.caching.userinterest.init.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.LocalDate;

@Configuration
@ConfigurationProperties("app")
public class AppProperties {

    private File usersDatasetCsvFile = new File("datasource/users.csv");
    private File newsDatasetCsvFile = new File("datasource/News_Category_Dataset_v3.csv");

    private int usersCount = 10000;
    private int minInterests = 0;
    private int maxInterests = 10;

    private LocalDate newsFromDate = LocalDate.parse("2012-01-01");
    private LocalDate newsUntilDate = LocalDate.parse("2022-09-23");

    public File getUsersDatasetCsvFile() {
        return usersDatasetCsvFile;
    }

    public void setUsersDatasetCsvFile(File usersDatasetCsvFile) {
        this.usersDatasetCsvFile = usersDatasetCsvFile;
    }

    public File getNewsDatasetCsvFile() {
        return newsDatasetCsvFile;
    }

    public void setNewsDatasetCsvFile(File newsDatasetCsvFile) {
        this.newsDatasetCsvFile = newsDatasetCsvFile;
    }

    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }

    public int getMinInterests() {
        return minInterests;
    }

    public void setMinInterests(int minInterests) {
        this.minInterests = minInterests;
    }

    public int getMaxInterests() {
        return maxInterests;
    }

    public void setMaxInterests(int maxInterests) {
        this.maxInterests = maxInterests;
    }

    public LocalDate getNewsFromDate() {
        return newsFromDate;
    }

    public void setNewsFromDate(LocalDate newsFromDate) {
        this.newsFromDate = newsFromDate;
    }

    public LocalDate getNewsUntilDate() {
        return newsUntilDate;
    }

    public void setNewsUntilDate(LocalDate newsUntilDate) {
        this.newsUntilDate = newsUntilDate;
    }
}
