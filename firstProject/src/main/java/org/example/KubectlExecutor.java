package org.example;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class KubectlExecutor {

    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    public String runCommand(String context, String... args) throws IOException, InterruptedException {
        return runCommand(context, DEFAULT_TIMEOUT_SECONDS, args);
    }

    public String runCommand(String context, int timeoutSeconds, String... args) throws IOException, InterruptedException {
        List<String> withContext = new ArrayList<>();
        withContext.add("--context");
        withContext.add(context);
        withContext.addAll(Arrays.asList(args));
        return execute(timeoutSeconds, withContext.toArray(new String[0]));
    }

    public String runGlobalCommand(String... args) throws IOException, InterruptedException {
        return execute(DEFAULT_TIMEOUT_SECONDS, args);
    }

    private String execute(int timeoutSeconds, String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("kubectl");
        command.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(System.getenv());

        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("kubectl timed out for: " + command);
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("kubectl failed: " + error);
        }
        return output;
    }
}
