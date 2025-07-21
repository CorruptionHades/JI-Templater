import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyTest {

    @TempDir
    File testProjectDir;

    @Test
    public void test() {

        String buildFileContent = """
         plugins {
         	id 'java'
            id("ji_templater")
         }
         repositories {
			  mavenCentral()
			  mavenLocal()
		  }
         dependencies {
			jimc("minecraft:1.21")
		 }
                
          task printCustomDependencies {
                doLast {
                    // Iterate over files for file-based dependencies
                    configurations.compileClasspath.files.each {
                        println "Resolved dependency file: ${it.name}"
                    }
                }
            }
      """.trim();

        File buildFile = testProjectDir.toPath().resolve("build.gradle").toFile();
        if(!buildFile.getParentFile().exists()) {
            buildFile.getParentFile().mkdirs();
        }
        try {
            buildFile.createNewFile();
            java.nio.file.Files.writeString(buildFile.toPath(), buildFileContent);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // This is a placeholder for the test method.
        // You can implement your test logic here.
        System.out.println("Test method executed: " + testProjectDir.getAbsolutePath());

        BuildResult res = GradleRunner.create().withProjectDir(testProjectDir)
                .withArguments("printCustomDependencies")
                .withPluginClasspath()
                .forwardOutput()
                .build();

        System.out.println(res.getOutput());
        assertTrue(res.getOutput().contains("remapped-named.jar"),
                "Custom dependency was not resolved correctly.");
    }
}
