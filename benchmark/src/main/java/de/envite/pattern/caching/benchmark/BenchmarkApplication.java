package de.envite.pattern.caching.benchmark;

import de.envite.pattern.caching.benchmark.domain.BenchmarkException;
import de.envite.pattern.caching.benchmark.domain.BenchmarkService;
import de.envite.pattern.caching.benchmark.support.StopSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class BenchmarkApplication implements CommandLineRunner, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkApplication.class);

    private final BenchmarkService benchmarkService;
    private final ExitCodeHolder exitCodeHolder;

    private final StopSignal destroySignal = new StopSignal();
    private final CountDownLatch terminationSignal = new CountDownLatch(1);

    @Autowired
    public BenchmarkApplication(final BenchmarkService benchmarkService,
                                final ExitCodeHolder exitCodeHolder) {
        this.benchmarkService = benchmarkService;
        this.exitCodeHolder = exitCodeHolder;
    }
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(BenchmarkApplication.class, args)));
    }

    @Override
    public void run(String... args) {
        try {
            benchmarkService.runBenchmark(destroySignal);
        } catch (final BenchmarkException e) {
            log.warn(e.getMessage() !=  null ? e.getMessage() : "Benchmark has been aborted.");
            exitCodeHolder.setExitCode(1);
        } catch (final InterruptedException e) {
            log.warn(e.getMessage() !=  null ? e.getMessage() : "Benchmark has been interrupted.");
            exitCodeHolder.setExitCode(2);
            Thread.currentThread().interrupt();
        } catch (final RuntimeException e) {
            log.warn("Benchmark has been terminated in an unexpected way.", e);
            exitCodeHolder.setExitCode(3);
        } finally {
            terminationSignal.countDown();
        }
    }

    @Override
    public void destroy() {
        log.info("Closing Benchmark application ...");
        final var startTimeMillis = System.currentTimeMillis();
        destroySignal.stop();
        try {
            terminationSignal.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Benchmark application closed after {} ms.", (System.currentTimeMillis() - startTimeMillis));
    }
}
