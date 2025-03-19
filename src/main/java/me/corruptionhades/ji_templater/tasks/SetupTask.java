package me.corruptionhades.ji_templater.tasks;

import me.corruptionhades.ji_templater.utils.SetupUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;

public class SetupTask extends DefaultTask {

    public SetupTask() {
        setGroup("ji-templater");
        setDescription("Sets up the project for Ji Templater.");
    }

    @TaskAction
    public void run() {
        System.out.println("Setting up...");

        SetupUtil.setup( getDownloadDir(),"https://piston-data.mojang.com/v1/objects/0e9a07b9bb3390602f977073aa12884a4ce12431/client.jar",
                "https://maven.fabricmc.net/net/fabricmc/yarn/1.21%2Bbuild.9/yarn-1.21%2Bbuild.9.jar",
                "https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.9.0/tiny-remapper-0.9.0-fat.jar");

        System.out.println("Done setting up! Make sure to add \"implementation files(\".gradle/download/remapped-named.jar\")\" to your dependencies!");
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(),".gradle/download");
    }

}
