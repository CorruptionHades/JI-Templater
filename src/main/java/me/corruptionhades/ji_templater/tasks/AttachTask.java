package me.corruptionhades.ji_templater.tasks;

import com.sun.tools.attach.VirtualMachine;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AttachTask extends DefaultTask {

    private Properties config;

    public AttachTask() {
        setGroup("ji-templater");
        setDescription("Attach to the Minecraft process.");
        loadConfig();
    }

    @TaskAction
    public void run() {
        System.out.println("Attaching...");
        List<ProcessInfo> processes = getPids();

        ProcessInfo selectedProcess = getProcessFromConfig(processes);
        if (selectedProcess == null) {
            selectedProcess = promptForProcess(processes);
        }

        if (selectedProcess == null) {
            System.err.println("No valid process selected.");
            return;
        }

        File selectedAgent = getAgentFromConfig();
        if (selectedAgent == null) {
            selectedAgent = promptForAgent();
        }

        if (selectedAgent == null) {
            System.err.println("No valid agent selected.");
            return;
        }

        System.out.println("Attaching " + selectedAgent.getName() + " to " + selectedProcess.getName() + " (" + selectedProcess.getPid() + ")");
        attach(selectedProcess, selectedAgent);
    }

    private void loadConfig() {
        config = new Properties();
        try (InputStream input = new FileInputStream(new File(getProject().getRootDir(), "config.properties"))) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ProcessInfo getProcessFromConfig(List<ProcessInfo> processes) {

        if(config == null) {
            return null;
        }

        String processName = config.getProperty("processName");
        if (processName != null && !processName.isEmpty()) {
            for (ProcessInfo process : processes) {
                if (process.getName().contains(processName)) {
                    return process;
                }
            }
        }
        return null;
    }

    private File getAgentFromConfig() {

        if(config == null) {
            return null;
        }

        String agentName = config.getProperty("agentName");
        if (agentName != null && !agentName.isEmpty()) {
            File deployDir = getBuildDir();
            File[] files = deployDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(agentName)) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    private ProcessInfo promptForProcess(List<ProcessInfo> processes) {
        System.out.println();
        System.out.println("Select an index to attach to:");
        for (int i = 0; i < processes.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + processes.get(i));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            int index = Integer.parseInt(reader.readLine()) - 1;
            if (index >= 0 && index < processes.size()) {
                return processes.get(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private File promptForAgent() {
        System.out.println();
        System.out.println("Select your agent to attach:");

        File deployDir = getBuildDir();
        File[] files = deployDir.listFiles();

        if (files == null || files.length == 0) {
            System.err.println("No agents found. Try running the deploy task first.");
            return null;
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("[" + (i + 1) + "] " + file.getName());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            int index = Integer.parseInt(reader.readLine()) - 1;
            if (index >= 0 && index < files.length) {
                return files[index];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ProcessInfo> getPids() {
        List<ProcessInfo> processes = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("jps -l");
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(" ");
                if (split.length < 2) {
                    continue;
                }
                int pid = Integer.parseInt(split[0]);
                String name = split[1];
                processes.add(new ProcessInfo(pid, name));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processes;
    }

    private void attach(ProcessInfo processInfo, File agent) {
        try {
            VirtualMachine vm = VirtualMachine.attach(processInfo.getPid());
            vm.loadAgent(agent.getAbsolutePath());
            vm.detach();

            System.out.println("Injected " + agent.getName() + " into " + processInfo.getPid() + "!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(), ".gradle/download");
    }

    private File getBuildDir() {
        return new File(getProject().getRootDir(), "build/libs");
    }
}

class ProcessInfo {
    private final int pid;
    private final String name;

    public ProcessInfo(int pid, String name) {
        this.pid = pid;
        this.name = name;
    }

    public String getPid() {
        return String.valueOf(pid);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return pid + " -> " + name;
    }
}