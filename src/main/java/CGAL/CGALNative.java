package CGAL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public final class CGALNative {

  private static final ConcurrentHashMap<String, Boolean> LIB_LOADED = new ConcurrentHashMap<>();

  private static final String ERROR_MSG =
      "Unsupported OS/arch, cannot find %s or load %s from system libraries. "
          + "Please try building from source the jar or providing %s in your system.";

  private static String osName() {
    String os = System.getProperty("os.name").toLowerCase().replace(' ', '_');
    if (os.startsWith("win")) {
      return "win";
    } else if (os.startsWith("mac")) {
      return "darwin";
    } else {
      return os;
    }
  }

  private static String osArch() {
    return System.getProperty("os.arch");
  }

  private static String libExtension() {
    if (osName().contains("os_x") || osName().contains("darwin")) {
      return "jnilib"; // "dylib";
    } else if (osName().contains("win")) {
      return "dll";
    } else {
      return "so";
    }
  }

  private static String resourceName(String libnameShort) {
    return "/" + Path.of(osName() + "-" + osArch(), "lib" + libnameShort + "." + libExtension())
        .toString();
  }

  private static String errorMessage(String libnameShort) {
    return String.format(ERROR_MSG, resourceName(libnameShort), libnameShort, "lib" + libnameShort);
  }

  public static void load(String libnameShort) {
    load(libnameShort, null);
  }

  public static void load(String libnameShort, final File tempFolder) {
    Boolean exists = LIB_LOADED.putIfAbsent(libnameShort, false);
    if (exists != null) {
      return;
    }
    String resourceName = resourceName(libnameShort);
    InputStream is = CGALNative.class.getResourceAsStream(resourceName);
    if (is == null) {
      try {
        System.loadLibrary(libnameShort);
        LIB_LOADED.putIfAbsent(libnameShort, true);
        return;
      } catch (UnsatisfiedLinkError e) {
        UnsatisfiedLinkError err = new UnsatisfiedLinkError(
            e.getMessage() + "\n" + errorMessage(libnameShort));
        err.setStackTrace(e.getStackTrace());
        throw err;
      }
    }
    File tempLib = null;
    FileOutputStream out = null;
    try {
      tempLib = File.createTempFile("lib" + libnameShort, "." + libExtension(), tempFolder);
      // try to delete on exit, does not work on Windows
      tempLib.deleteOnExit();
      // copy to tempLib
      out = new FileOutputStream(tempLib);
      byte[] buf = new byte[4096];
      while (true) {
        int read = is.read(buf);
        if (read == -1) {
          break;
        }
        out.write(buf, 0, read);
      }
      try {
        out.flush();
        out.close();
        out = null;
      } catch (IOException e) {
        // ignore
      }
      try {
        System.load(tempLib.getAbsolutePath());
      } catch (UnsatisfiedLinkError e) {
        // fall-back to loading the zstd-jni from the system library path
        try {
          System.loadLibrary(libnameShort);
        } catch (UnsatisfiedLinkError e1) {
          // display error in case problem with loading from temp folder
          // and from system library path - concatenate both messages
          UnsatisfiedLinkError err = new UnsatisfiedLinkError(
              e.getMessage() + "\n" +
                  e1.getMessage() + "\n" +
                  errorMessage(libnameShort));
          err.setStackTrace(e1.getStackTrace());
          throw err;
        }
      }
      LIB_LOADED.putIfAbsent(libnameShort, true);
    } catch (IOException e) {
      // IO errors in extacting and writing the shared object in the temp dir
      ExceptionInInitializerError err = new ExceptionInInitializerError(
          "Cannot unpack " + libnameShort + ": " + e.getMessage());
      err.setStackTrace(e.getStackTrace());
      throw err;
    } finally {
      try {
        is.close();
        if (out != null) {
          out.close();
        }
        if (tempLib != null && tempLib.exists()) {
          tempLib.delete();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }
}
