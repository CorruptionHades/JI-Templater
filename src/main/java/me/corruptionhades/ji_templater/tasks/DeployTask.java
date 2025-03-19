package me.corruptionhades.ji_templater.tasks;

import me.corruptionhades.ji_templater.utils.ZipUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DeployTask extends DefaultTask {

    boolean debug = true;

    public DeployTask() {
        setGroup("ji-templater");
        setDescription("Deploy your mod.");
        dependsOn("build");
        Object val = getProject().getProperties().get("ji-templater-debug");
        if(val != null) {
            debug = (boolean) val;
        }
    }

    @TaskAction
    public void run() {

        System.out.println("Deploying...");
        File build = getBuild();
        if(build == null) {
            System.err.println("No build found.");
            return;
        }

        File deployDir = getBuildDir();

        File deployFileOfficial = new File(deployDir, build.getName().replace(".jar", "") + "-official.jar");
        File deployFileFabric = new File(deployDir, build.getName().replace(".jar", "") + "-fabric.jar");

        if(deployFileOfficial.exists()) {
            deployFileOfficial.delete();
        }

        if(deployFileFabric.exists()) {
            deployFileFabric.delete();
        }

        if(debug) {
            System.out.println("Deploying to " + deployDir.getAbsolutePath());
        }

        remap(build, deployFileFabric, "intermediary", getRemappedNamed());
        System.out.println("Deployed Fabric");

        remap(build, deployFileOfficial, "official", getRemappedNamed());
        System.out.println("Deployed Official");

        List<File> dependencies = new ArrayList<>();
        for (File runtimeClasspath : getProject().getConfigurations().getByName("runtimeClasspath").getFiles()) {
            if(runtimeClasspath.getAbsolutePath().contains("remapped-named.jar")) {
                continue;
            }

            dependencies.add(runtimeClasspath);
        }
        ZipUtil.combineJarFiles(deployFileFabric, dependencies.toArray(File[]::new));
    }

    private void remap(File file, File to, String named, File classpath) {
        try {
            File remapper = getRemapper();
            File mappings = getMapping();

            if(!remapper.exists() || !mappings.exists()) {
                System.err.println("No remapper or mappings found. Try rerunning the setup task.");
                return;
            }

            // java -jar remapper.jar input.jar output.jar mappings.tiny official named
            // java -jar tiny-remapper-0.10.4-fat.jar mod.jar out.jar mappings.tiny named official --ignoreConflicts remapped.jar
            String cmd = "java -jar " + remapper.getAbsolutePath() +
                    " " + file.getAbsolutePath() +
                    " " + to.getAbsolutePath() +
                    " " + mappings.getAbsolutePath() +
                    " " + "named " + named + " --ignoreConflicts" +
                    " " + classpath.getAbsolutePath();

            if(debug)
                System.out.println("Running: " + cmd);

            Process process = Runtime.getRuntime().exec(cmd);
            InputStream in = process.getInputStream();

        //    System.out.println("--------------- Output ---------------");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            // read the output from the command
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
            }

            reader.close();

         //   System.out.println("--------------- Output End ---------------");

            if(debug)
                System.out.println("Remapped " + file.getName() + " to " + to.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getBuild() {
        File dir = getBuildDir();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        for (String s : dir.list()) {
            if(s.contains("-fabric") || s.contains("-official")) {
                continue;
            }
            return new File(dir, s);
        }

        return null;
       // return new File("H:/IntelijProjects/Ji-Template-Test/build/libs/Ji-Template-Test-1.0-SNAPSHOT.jar");
    }

    private File getMapping() {
        File dir = getDownloadDir();
        return new File(dir, "mappings/mappings.tiny");
    }

    private File getRemapper() {

        File downloadDir = getDownloadDir();

        for (String s : downloadDir.list()) {
            if(s.contains("tiny-remapper")) {
                return new File(downloadDir, s);
            }
        }

        return new File("download/tiny-remapper-0.9.0-fat.jar");
    }

    private File getRemappedNamed() {
        File dir = getDownloadDir();
        return new File(dir,"remapped-named.jar");
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(),".gradle/download");
    }

    private File getBuildDir() {
        return new File(getProject().getRootDir(), "build/libs");
    }
}
