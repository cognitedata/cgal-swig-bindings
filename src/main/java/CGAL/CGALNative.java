package CGAL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class CGALNative {

  private static final String[] REQUIRED_LIBS = new String[] {
      "CGAL_AABB_tree_cpp",
      "CGAL_Alpha_shape_2_cpp",
      "CGAL_Java_cpp",
      "CGAL_Kernel_cpp",
      "CGAL_Mesh_3_cpp",
      "CGAL_Surface_mesher_cpp",
      "CGAL_Triangulation_2_cpp",
      "CGAL_Triangulation_3_cpp"
  };

  private static final ConcurrentHashMap<String, Boolean> LIB_LOADED = new ConcurrentHashMap<>();

  private static final AtomicReference<File> TMP_LOCATION = new AtomicReference<File>();

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

  public static void loadRequired() {
    for (String libName : REQUIRED_LIBS) {
      load(libName);
    }
  }

  public static void load(String libnameShort) {
    load(libnameShort, null);
  }

  public static File getTemporaryLocation() {
    File location = TMP_LOCATION.get();
    if (location == null) {
      try {
        Path tempDirWithPrefix = Files.createTempDirectory("cgal");
        location = tempDirWithPrefix.toAbsolutePath().toFile();
      } catch (IOException e) {
        location = new File("./");
      }
      TMP_LOCATION.set(location);
      location.deleteOnExit();
    }
    return location;
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
    File tempLib = getTemporaryLocation();
    try {
      if (!tempLib.exists()) {
        String libDirectory = "/" + Path.of(osName() + "-" + osArch());
        copyAll(libDirectory, tempLib);
      }
      File tmpLibNi = new File(tempLib, libnameShort + "." + libExtension());
      try {
        System.out.println("Load: " + tmpLibNi);
        System.load(tmpLibNi.getAbsolutePath());
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
        if (tempLib != null && tempLib.exists()) {
          tempLib.delete();
        }
      } catch (IOException e) {
        // ignore
      }
    }
  }

  private static void copyAll(String libDirectory, File tempLib) throws IOException {
    List<String> filenames = new ArrayList<>();
    try (InputStream is = CGALNative.class.getResourceAsStream(libDirectory)) {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String resource;
      while ((resource = br.readLine()) != null) {
        System.out.println(resource);
        filenames.add(resource);
      }
    }

    for (String file : filenames) {
      try (InputStream is = CGALNative.class.getResourceAsStream(file)) {
        File newLocation = new File(tempLib, file);
        System.out.println(file + " to " + newLocation);
        copy(is, newLocation.toString());
      }
    }
  }

  private static void copy(InputStream is, String pathFile) throws IOException {
    try (FileOutputStream out = new FileOutputStream(pathFile)) {
      byte[] buf = new byte[4096];
      while (true) {
        int read = is.read(buf);
        if (read == -1) {
          break;
        }
        out.write(buf, 0, read);
      }
    }
  }
}

