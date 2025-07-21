package me.corruptionhades.jimcver;

import me.corruptionhades.jimcver.dependency.JiDependencyHandler;
import me.corruptionhades.jimcver.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;

public class JiTemplater implements Plugin<Project> {

    /*
     Get the client jar: https://piston-meta.mojang.com/v1/packages/177e49d3233cb6eac42f0495c0a48e719870c2ae/1.21.json
     "https://piston-data.mojang.com/v1/objects/0e9a07b9bb3390602f977073aa12884a4ce12431/client.jar"

     Get yarn mappings:
     https://maven.fabricmc.net/net/fabricmc/yarn/1.21%2Bbuild.9/yarn-1.21%2Bbuild.9.jar

     Get tiny remapper:
     https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.9.0/tiny-remapper-0.9.0-fat.jar

     java -jar tiny-remapper-0.9.0-fat.jar client.jar remapped.jar mappings.tiny official named
     */

    public static final String DEPENDENCY_COMMAND = "jimc";
    public static final String DEPENDENCY_COMMAND_INTERNAL = "jimc-internal";

    @Override
    public void apply(Project project) {
      //  project.getTasks().register("setup", SetupTask.class);
        project.getTasks().register("deploy", DeployTask.class);
        project.getTasks().register("attach", AttachTask.class);
        //project.getTasks().register("upgrade", UpgradeToLatestTask.class);

        Configuration customConfiguration = project.getConfigurations().create(DEPENDENCY_COMMAND_INTERNAL, configuration -> {
            configuration.setTransitive(false);
            configuration.setCanBeResolved(true);
        });

        // Add the custom configuration to the compile and runtime classpaths
        project.getConfigurations().getByName("compileClasspath").extendsFrom(customConfiguration);
        project.getConfigurations().getByName("runtimeClasspath").extendsFrom(customConfiguration);

        // Add a custom method to the dependencies block
        project.getDependencies().getExtensions().add(DEPENDENCY_COMMAND, new JiDependencyHandler(project, customConfiguration));
    }

}


