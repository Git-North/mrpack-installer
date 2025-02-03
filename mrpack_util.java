import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class mrpack_util {
     static Scanner sc = new Scanner(System.in);
     
     public static void main(String[] args) {
          // Get the current directory where the JAR is located
          String currentDir = System.getProperty("user.dir");
          
          // Navigate to the parent directory (one directory above)
          String dmc = Paths.get(currentDir).getParent().toString();
          
          // Now, you can assume that the .minecraft folder is in this directory
          System.out.println("Found .minecraft folder at: " + dmc);
          
          // Search for the .mrpack file inside the .minecraft folder and its subdirectories
          File mrpack = findMrpackFile(new File(dmc));
          
          if (mrpack == null) {
               // If no .mrpack file is found, ask the user for the file path
               System.out.println("No .mrpack file found in the .minecraft directory or its subdirectories.");
               System.out.print("Enter the path to .mrpack with file name and extension \n>");
               String mrp = sc.nextLine().trim();
               mrpack = new File(mrp);
          }
          
          if (!mrpack.exists()) {
               System.out.println("The file does not exist at the specified path: " + mrpack.getAbsolutePath());
               return;
          }
          
          System.out.println("Using .mrpack file: " + mrpack.getAbsolutePath());
          File mcm = new File(dmc);
          
          check(mrpack);
          check(mcm);
          
          if (!mcm.isDirectory()) {
               throw new RuntimeException("Is not a directory:" + mcm.getAbsolutePath());
          }
          
          Path modsDir = Paths.get(dmc, "mods");
          if (!Files.exists(modsDir)) {
               try {
                    Files.createDirectories(modsDir);
               } catch (IOException e) {
                    throw new RuntimeException("Failed to create mods directory: " + modsDir);
               }
          }
          
          try (FileInputStream fis = new FileInputStream(mrpack); ZipInputStream zip = new ZipInputStream(fis)) {
               ZipEntry entry;
               String MIJson = null;
               while((entry = zip.getNextEntry()) != null) {
                    if (!entry.getName().equals("modrinth.index.json")) {
                         extractTo(entry, dmc, zip);
                    } else {
                         MIJson = fileText(zip);
                    }
                    zip.closeEntry();
               }
               if (MIJson != null) {
                    processModrinthIndex(modsDir, MIJson);
               }
          } catch(IOException exc) {
               exc.printStackTrace(System.out);
          }
     }

     public static void check(File file) {
          if (!file.exists()) {
               throw new RuntimeException("Cannot find file or directory:" + file.getAbsolutePath());
          }
     }

     public static File extractTo(ZipEntry entry, String outputDir, ZipInputStream zip) {
          System.out.println("Extracting file " + entry.toString() + " to directory " + outputDir);
          String entryName = entry.getName();
          String targetName = entryName.replaceFirst("^overrides/", "");
          File vout = Paths.get(outputDir, targetName).toFile();
          if (vout.isDirectory()) {
               vout.mkdirs();
               return vout;
          }
          vout.getParentFile().mkdirs();
          try (FileOutputStream out = new FileOutputStream(vout)) {
               byte[] buffer = new byte[8192];
               int length;
               while ((length = zip.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
               }
          } catch (IOException exc) {
               exc.printStackTrace(System.out);
          }
          return vout;
     }

     public static void processModrinthIndex(Path mods, String zip) {
          JSONObject root = new JSONObject(zip);
          JSONArray files = root.getJSONArray("files");
          System.out.println("Downloading mods");

          for (int i = 0; i < files.length(); i++) {
               JSONObject file = files.getJSONObject(i);
               JSONArray downloads = file.getJSONArray("downloads");
               String url = downloads.getString(0);
               downloadMod(url, mods.toFile());
          }
     }

     public static String fileText(ZipInputStream zipInputStream) {
          StringBuilder content = new StringBuilder();
          try {
               byte[] buffer = new byte[8192];
               int bytesRead;
               while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    content.append(new String(buffer, 0, bytesRead));
               }
          } catch (IOException exc) {
               exc.printStackTrace(System.out);
          }
          return content.toString();
     }

     public static void downloadMod(String url, File modsDir) {
          try (InputStream in = new URL(url).openStream()) {
               String fileName = url.substring(url.lastIndexOf("/") + 1);
               Path modPath = modsDir.toPath().resolve(fileName);
               Files.copy(in, modPath, StandardCopyOption.REPLACE_EXISTING);
               System.out.println("Downloaded: " + fileName);
          } catch (IOException exc) {
               System.out.println("Failed to download: " + url);
               exc.printStackTrace(System.out);
          }
     }
     
     // Method to recursively search for .mrpack file
     public static File findMrpackFile(File directory) {
          if (!directory.isDirectory()) {
               return null;
          }
          
          File[] files = directory.listFiles();
          if (files != null) {
               for (File file : files) {
                    if (file.isDirectory()) {
                         File result = findMrpackFile(file);  // Recursively search subdirectories
                         if (result != null) {
                              return result;
                         }
                    } else if (file.getName().endsWith(".mrpack")) {
                         return file;  // Found the .mrpack file
                    }
               }
          }
          return null;  // .mrpack file not found
     }
}
