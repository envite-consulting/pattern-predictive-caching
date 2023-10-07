package de.envite.pattern.caching.benchmark;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class ExitCodeHolder implements ExitCodeGenerator {

    private int exitCode = 0;

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

}
