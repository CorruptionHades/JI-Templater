package me.corruptionhades.ji_templater;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SetupTask extends DefaultTask {

    public SetupTask() {
        setGroup("ji-templater");
        setDescription("Sets up the project for Ji Templater.");
    }

    @TaskAction
    public void run() {
        System.out.println("Setting up...");
        File downloadDir = getDownloadDir();

        if(downloadDir.exists()) {
            downloadDir.delete();
        }

        downloadDir.mkdirs();

        File clientJar = download("https://piston-data.mojang.com/v1/objects/0e9a07b9bb3390602f977073aa12884a4ce12431/client.jar", downloadDir);
        System.out.println("Downloaded client.jar");

        File mappings = download("https://maven.fabricmc.net/net/fabricmc/yarn/1.21%2Bbuild.9/yarn-1.21%2Bbuild.9.jar", downloadDir);
        System.out.println("Downloaded yarn mappings.");

        File remapper = download("https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.10.4/tiny-remapper-0.10.4-fat.jar", downloadDir);
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

      //  getProject().getDependencies().add("implementation", getProject().files(clientJar.getParentFile() + "/remapped-named.jar"));

        System.out.println("Done setting up! Make sure to add \"implementation files(\".gradle/download/remapped-named.jar\")\" to your dependencies!");
    }

    private void remap(File clientJar, File remapper, File mappings, String newName, String to) {
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

    private void unzip(File mappings) {
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

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(),".gradle/download");
    }

    private File download(String link, File dir) {

        File file = new File(dir, link.substring(link.lastIndexOf('/') + 1));

        try (BufferedInputStream in = new BufferedInputStream(new URL(link).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
        }

        return file;
    }
}
