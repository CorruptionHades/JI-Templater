package me.corruptionhades.ji_templater;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class TestTask extends DefaultTask {

    public TestTask() {
        setGroup("ji-templater");
        setDescription("Testing.");
    }

    @TaskAction
    public void run() {
        // loop through dependencies and get path
        getProject().getConfigurations().getByName("runtimeClasspath").getFiles().forEach(file -> {
            System.out.println(file.getAbsolutePath());
        });
    }
}
