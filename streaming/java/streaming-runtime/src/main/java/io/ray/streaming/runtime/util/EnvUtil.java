package io.ray.streaming.runtime.util;

import io.ray.runtime.RayNativeRuntime;
import io.ray.runtime.util.JniUtils;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvUtil {

  private static final Logger LOG = LoggerFactory.getLogger(EnvUtil.class);

  public static String getJvmPid() {
    return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
  }

  public static String getHostName() {
    String hostname = "";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      LOG.error("Error occurs while fetching local host.", e);
    }
    return hostname;
  }

  public static void loadNativeLibraries() {
    // Explicitly load `RayNativeRuntime`, to make sure `core_worker_library_java`
    // is loaded before `streaming_java`.
    try {
      Class.forName(RayNativeRuntime.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    JniUtils.loadLibrary("streaming_java");
  }

  /**
   * Execute an external command.
   *
   * @return Whether the command succeeded.
   */
  public static boolean executeCommand(List<String> command, int waitTimeoutSeconds) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command)
          .redirectOutput(ProcessBuilder.Redirect.INHERIT)
          .redirectError(ProcessBuilder.Redirect.INHERIT);
      Process process = processBuilder.start();
      boolean exit = process.waitFor(waitTimeoutSeconds, TimeUnit.SECONDS);
      if (!exit) {
        process.destroyForcibly();
      }
      return process.exitValue() == 0;
    } catch (Exception e) {
      throw new RuntimeException("Error executing command " + String.join(" ", command), e);
    }
  }

}
