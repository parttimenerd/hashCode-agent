package me.bechberger.runtime;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HashCodeHandler {

  private static class CounterPerClass {
    private Map<Class<?>, Integer> counterPerClass = new HashMap<>();

    public synchronized void increment(Class<?> cc) {
      counterPerClass.putIfAbsent(cc, 0);
      counterPerClass.put(cc, counterPerClass.get(cc) + 1);
    }

    public void print(PrintWriter out) {
      // print table sorted by highest
      List<Class<?>> sorted = counterPerClass.keySet().stream().sorted((c1, c2) -> {
        return Integer.compare(counterPerClass.get(c2), counterPerClass.get(c1));
      }).collect(Collectors.toList());
      int size = sorted.stream().mapToInt(c -> c.getName().length()).max().orElse(0);
      for (Class<?> c : sorted) {
        out.printf("%" + (size + 2) + "s  %10d%n", c.getName(), counterPerClass.get(c));
      }
    }
  }

  private static boolean logEveryUsage = false;

  private static CounterPerClass callHashCodeCounter = new CounterPerClass();
  private static CounterPerClass hashCodeCounter = new CounterPerClass();

  public static int handleHashCode(Object calling, Object obj) {
    int hash = obj.hashCode();
    if (hash == System.identityHashCode(obj)) {
      if (logEveryUsage) {
        System.err.println(
            "Called hashCode of "
                + obj.getClass()
                + " in "
                + calling.getClass()
                + " which returns the identityHashCode");
      }
      callHashCodeCounter.increment(calling.getClass());
    }
    return hash;
  }

  public static int handleIdentityHashCode(Object obj) {
    return System.identityHashCode(obj);
  }

  public static int hashCodeImplementation(Object obj) {
    if (logEveryUsage) {
      System.err.println(
          "hashCode called on " + obj.getClass() + " which doesn't have a hashCode method");
    }
    hashCodeCounter.increment(obj.getClass());
    return System.identityHashCode(obj);
  }

  public static void setLogEveryUsage(boolean logEveryUsage) {
    HashCodeHandler.logEveryUsage = logEveryUsage;
  }

  private static void writeTables(PrintWriter out) {
    hashCodeCounter.print(out);
  }

  public static void setOutput(Optional<Path> output) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (output.isPresent()) {
        try (PrintWriter out = new PrintWriter(output.get().toFile())) {
          writeTables(out);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        writeTables(new PrintWriter(System.err));
      }
    }));
  }
}
