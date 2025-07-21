package me.corruptionhades.jimcver.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SetupUtil {

    public static String setup(File downloadDir, String clientJarLink, String mappingsLink, String remapperLink) {
        System.out.println("Setting up...");

        if(downloadDir.exists()) {
            downloadDir.delete();
        }

        downloadDir.mkdirs();

        File clientJar = download(clientJarLink, downloadDir);
        System.out.println("Downloaded client.jar");

        File mappings = download(mappingsLink, downloadDir);
        System.out.println("Downloaded yarn mappings.");

        File remapper = download(remapperLink, downloadDir);
        System.out.println("Downloaded tiny-remapper");

        System.out.println("Downloads complete.");

        System.out.println("Unzipping yarn mappings...");
        unzip(mappings);
        System.out.printf("Unzipped yarn mappings to %s\n", mappings.getParentFile().getAbsolutePath());

        File mappingsFile = new File(mappings.getParentFile(), "mappings/mappings.tiny");

        System.out.println("Now remapping client.jar...");
        remap(clientJar, remapper, mappingsFile, "remapped-named.jar", "named");
        System.out.println("Remapped client.jar to remapped-named.jar");

        remap(clientJar, remapper, mappingsFile, "remapped-intermediary.jar", "intermediary");
        System.out.println("Remapped client.jar to remapped-intermediary.jar");

        System.out.println("Done setting up!");

        return "remapped-named.jar";
    }

    private static void remap(File clientJar, File remapper, File mappings, String newName, String to) {

        // check if new file already exists
        File newFile = new File(clientJar.getParentFile(), newName);
        if (newFile.exists()) {
            System.out.println("File " + newName + " already exists. Skipping remap.");
            return;
        }

        try {
            String cmd = "java -jar " +
                    remapper.getAbsolutePath() + " " +
                    clientJar.getAbsolutePath() + " " +
                    clientJar.getParentFile().getAbsolutePath() + "/" + newName + " " +
                    mappings.getAbsolutePath() + " " +
                    "official" + " " + to;

            System.out.println("Remapping with: " + cmd);

            Process proc = Runtime.getRuntime().exec(cmd);
            proc.waitFor();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unzip(File mappings) {
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(mappings));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if(zipEntry.getName().contains("META-INF")) {
                    zipEntry = zis.getNextEntry();
                    continue;
                }

                File newFile = newFile(mappings.getParentFile(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static File download(String link, File dir) {

        File file = new File(dir, link.substring(link.lastIndexOf('/') + 1));

        try {
            URL url = new URL(link);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            long remoteFileSize = httpConnection.getContentLengthLong();

            if (file.exists() && file.length() == remoteFileSize) {
                System.out.println("File already exists and has the same size. Skipping download.");
                return file;
            }

            try (BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }

        return file;
    }
}
