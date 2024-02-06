package me.bechberger;

import me.bechberger.runtime.HashCodeHandler;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

/** Agent entry */
public class Main {

  public static void premain(String agentArgs, Instrumentation inst) {
    AgentOptions options = new AgentOptions(agentArgs);
    // clear the file
    options
        .getOutput()
        .ifPresent(
            out -> {
              try {
                Files.deleteIfExists(out);
                Files.createFile(out);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
    try {
      inst.appendToBootstrapClassLoaderSearch(new JarFile(getExtractedJARPath().toFile()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    HashCodeHandler.setLogEveryUsage(options.isLogEveryUsage());
    HashCodeHandler.setOutput(options.getOutput());
    inst.addTransformer(new ClassTransformer(), true);
  }

  private static Path getExtractedJARPath() throws IOException {
    try (InputStream in = Main.class.getClassLoader().getResourceAsStream("hashCode-runtime.jar")) {
      if (in == null) {
        throw new RuntimeException("Could not find hashCode-runtime.jar");
      }
      File file = File.createTempFile("runtime", ".jar");
      file.deleteOnExit();
      Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return file.toPath().toAbsolutePath();
    }
  }
}
