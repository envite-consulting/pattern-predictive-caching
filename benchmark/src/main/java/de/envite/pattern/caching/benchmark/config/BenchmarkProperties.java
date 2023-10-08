package de.envite.pattern.caching.benchmark.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.LocalDate;

@Configuration
@ConfigurationProperties("benchmark")
public class BenchmarkProperties {

    private Duration initialWaitingPeriod;
    private int numberOfUsers;
    private LocalDate date;
    private Duration testDuration;
    private Duration requestWaitingPeriod;

    public Duration getInitialWaitingPeriod() {
        return initialWaitingPeriod;
    }

    public void setInitialWaitingPeriod(Duration initialWaitingPeriod) {
        this.initialWaitingPeriod = initialWaitingPeriod;
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

    public Duration getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(Duration testDuration) {
        this.testDuration = testDuration;
    }

    public Duration getRequestWaitingPeriod() {
        return requestWaitingPeriod;
    }

    public void setRequestWaitingPeriod(Duration requestWaitingPeriod) {
        this.requestWaitingPeriod = requestWaitingPeriod;
    }
}
