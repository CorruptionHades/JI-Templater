package me.corruptionhades.ji_templater.tasks;

import me.corruptionhades.ji_templater.utils.FabricMeta;
import me.corruptionhades.ji_templater.utils.SetupUtil;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class UpgradeToLatestTask extends DefaultTask {

    public UpgradeToLatestTask() {
        setGroup("ji-templater");
        setDescription("Upgrade to latest stable mc version");
    }

    @TaskAction
    public void run() {
        String newestStableVersion = FabricMeta.getNewestStableVersion();
        if(newestStableVersion == null) {
            System.err.println("Failed to get newest stable version.");
            return;
        }

        System.out.println("Newest stable version: " + newestStableVersion);

        String mappingLink = FabricMeta.getMappingLink(newestStableVersion);
        if(mappingLink == null) {
            System.err.println("Failed to get mapping link.");
            return;
        }

        System.out.println("Mapping link: " + mappingLink);

        String clientLink = FabricMeta.getClientLink(newestStableVersion);
        if(clientLink == null) {
            System.err.println("Failed to get client link.");
            return;
        }

        System.out.println("Client link: " + clientLink);

        File downloadDir = getDownloadDir();
        SetupUtil.setup(downloadDir, clientLink, mappingLink, "https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.9.0/tiny-remapper-0.9.0-fat.jar");
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(),".gradle/download");
    }
}
