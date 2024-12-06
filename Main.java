import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
     static Scanner sc = new Scanner(System.in);
     public static void main(String[] args) {
          System.out.print("Enter the path to the \".minecraft\" folder \n>");
          String dmc = sc.nextLine().trim();
          System.out.print("Enter the path to .mrpack with file name and extension \n>");
          String mrp = sc.nextLine().trim();
          File mrpack = new File(mrp);
          File mcm = new File(dmc);
          check(mrpack);
          check(mcm);
          if(!mcm.isDirectory()) {
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
          try(FileInputStream fis = new FileInputStream(mrpack); ZipInputStream zip = new ZipInputStream(fis)) {
               ZipEntry entry;
               String MIJson = null;
               while((entry = zip.getNextEntry()) != null) {
                    if(!entry.getName().equals("modrinth.index.json")) {
                         extractTo(entry, dmc, zip);
                    } else {
                         MIJson = fileText(zip);
                    }
                    zip.closeEntry();
               }
               if(MIJson != null) {
                    processModrinthIndex(modsDir, MIJson);
               }
          }catch(IOException exc) {
               exc.printStackTrace(System.out);
          }
     }
     public static void check(File file) {
          if(!file.exists()) {
               throw new RuntimeException("Cannot find file or directory:" + file.getAbsolutePath());
          }
     }
     public static File extractTo(ZipEntry entry, String outputDir, ZipInputStream zip) {
          System.out.println("Extracting file " + entry.toString() + " to directory " + outputDir);
          String entryName = entry.getName();
          String targetName = entryName.replaceFirst("^overrides/", "");
          File vout = Paths.get(outputDir, targetName).toFile();
          if(vout.isDirectory()) {
               vout.mkdirs();
               return vout;
          }
          vout.getParentFile().mkdirs();
          try(FileOutputStream out = new FileOutputStream(vout)) {
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
}