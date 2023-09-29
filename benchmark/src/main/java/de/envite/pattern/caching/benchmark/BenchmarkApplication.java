package de.envite.pattern.caching.benchmark;

import de.envite.pattern.caching.benchmark.domain.BenchmarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BenchmarkApplication implements CommandLineRunner {
    private final BenchmarkService benchmarkService;

    public BenchmarkApplication(@Autowired BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }
    public static void main(String[] args) {
        SpringApplication.run(BenchmarkApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        benchmarkService.runBenchmark();
    }
}
