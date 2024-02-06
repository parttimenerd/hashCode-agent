package me.bechberger;

import java.nio.file.Path;
import java.util.Optional;

public class AgentOptions {

  /** default is stderr */
  private Optional<Path> output = Optional.empty();

  private boolean logEveryUsage = false;

  private void printHelp() {
    System.out.println(
        """
                Usage: java -javaagent:hashCode.jar=options ...
                Options:
                    help: Print this help message
                    output: file to store the found hashCode usages
                    logEveryUsage: log every usage of hashCode to stderr
                """);
  }

  private void optionsError(String msg) {
    System.err.println(msg);
    printHelp();
    System.exit(1);
  }

  private void initOptions(String agentArgs) {
    if (agentArgs == null || agentArgs.isEmpty()) {
      return;
    }
    for (String part : agentArgs.split(",")) {
      String[] kv = part.split("=");
      if (kv.length != 2) {
        optionsError("Invalid argument: " + part);
      }
      String key = kv[0];
      String value = kv[1];
      switch (key) {
        case "help" -> printHelp();
        case "output" -> output = Optional.of(Path.of(value));
        case "logEveryUsage" -> logEveryUsage = Boolean.parseBoolean(value);
        default -> optionsError("Unknown argument: " + key);
      }
    }
  }

  public AgentOptions(String agentArgs) {
    initOptions(agentArgs);
  }

  public Optional<Path> getOutput() {
    return output;
  }

  public boolean isLogEveryUsage() {
    return logEveryUsage;
  }
}
