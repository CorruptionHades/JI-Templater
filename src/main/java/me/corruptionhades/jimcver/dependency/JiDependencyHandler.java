package me.corruptionhades.jimcver.dependency;

import me.corruptionhades.jimcver.JiTemplater;
import me.corruptionhades.jimcver.utils.FabricMeta;
import me.corruptionhades.jimcver.utils.SetupUtil;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

import java.io.File;

public class JiDependencyHandler {
	private final Project project;
	private final Configuration configuration;

	public JiDependencyHandler(Project project, Configuration configuration) {
		this.project = project;
        this.configuration = configuration;
    }

	/**
	 * This method will be called from the build script via Groovy's dynamic method invocation.
	 * @param version The version of Minecraft to add as a dependency.
	 */
	public void invoke(String version) {

		System.out.println("JiTemplater: Getting dependencies for Minecraft version " + version);

		project.getLogger().lifecycle( JiTemplater.DEPENDENCY_COMMAND + " getting " + version);

		File downloadDir = new File(project.getRootDir(), ".ji/download/" + version);
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}

		String clientLink = FabricMeta.getClientLink(version);
		String mappingLink = FabricMeta.getMappingLink(version);
		String remapper = "https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.9.0/tiny-remapper-0.9.0-fat.jar";

		String remappedNamed = SetupUtil.setup(downloadDir, clientLink, mappingLink, remapper);

		project.getDependencies().add(configuration.getName(),
				project.files(downloadDir.getAbsolutePath() + "/" + remappedNamed));
	}
}