package me.corruptionhades;

import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    public static void combineJarFiles(File jar, File[] jars) {
        try {
            combineJars(jar, jars);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void combineJars(File jar, File[] jars) throws IOException {
        File tempFile = File.createTempFile(jar.getName(), null);
        byte[] buffer = new byte[1024];
        Set<String> entryNames = new HashSet<>();

        // Read the original JAR contents and write them to the temp file
        try (ZipInputStream originalJarZis = new ZipInputStream(new FileInputStream(jar));
             ZipOutputStream tempZos = new ZipOutputStream(new FileOutputStream(tempFile))) {
            ZipEntry entry;
            while ((entry = originalJarZis.getNextEntry()) != null) {
                if (entryNames.add(entry.getName())) {
                    tempZos.putNextEntry(new ZipEntry(entry.getName()));
                    int len;
                    while ((len = originalJarZis.read(buffer)) > 0) {
                        tempZos.write(buffer, 0, len);
                    }
                    tempZos.closeEntry();
                }
            }

            // Process each source JAR
            for (File sourceJar : jars) {
                if (sourceJar.getAbsolutePath().contains("download/")) {
                    continue;
                }

                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceJar))) {
                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();
                        // Skip META-INF and duplicate entries
                        if (!name.startsWith("META-INF/") && entryNames.add(name)) {
                            tempZos.putNextEntry(new ZipEntry(name));
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                tempZos.write(buffer, 0, len);
                            }
                            tempZos.closeEntry();
                        }
                    }
                }
            }
        }

        // Replace the original JAR with the temp file
        if (!jar.delete()) {
            throw new IOException("Could not delete original JAR file.");
        }
        if (!tempFile.renameTo(jar)) {
            throw new IOException("Could not replace original JAR file with combined JAR file.");
        }
    }
}