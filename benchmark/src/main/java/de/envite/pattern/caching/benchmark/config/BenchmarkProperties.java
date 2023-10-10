package de.envite.pattern.caching.benchmark.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
@ConfigurationProperties("benchmark")
public class BenchmarkProperties {

    private Duration initialDelay = Duration.ofSeconds(30);
    private Duration intermediateDelay = Duration.ofSeconds(30);
    private Duration shutdownDelay = Duration.ofSeconds(15);

    private int numberOfUsers = 100;

    private LocalDate date = LocalDate.parse("2013-08-05");
    private Duration requestDelay = Duration.ofSeconds(1);

    private Duration testDuration = Duration.ZERO;
    private long requestsPerUser = 0;

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Duration getIntermediateDelay() {
        return intermediateDelay;
    }

    public void setIntermediateDelay(Duration intermediateDelay) {
        this.intermediateDelay = intermediateDelay;
    }

    public Duration getShutdownDelay() {
        return shutdownDelay;
    }

    public void setShutdownDelay(Duration shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Duration getRequestDelay() {
        return requestDelay;
    }

    public void setRequestDelay(Duration requestDelay) {
        this.requestDelay = requestDelay;
    }

    public Duration getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(Duration testDuration) {
        this.testDuration = testDuration;
    }

    public long getRequestsPerUser() {
        return requestsPerUser;
    }

    public void setRequestsPerUser(long requestsPerUser) {
        this.requestsPerUser = requestsPerUser;
    }

}
