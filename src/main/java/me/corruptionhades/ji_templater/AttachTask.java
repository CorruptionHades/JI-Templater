package me.corruptionhades.ji_templater;

import com.sun.tools.attach.VirtualMachine;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachTask extends DefaultTask {

    public AttachTask() {
        setGroup("ji-templater");
        setDescription("Attach to the Minecraft process.");
    }

    @TaskAction
    public void run() {
        System.out.println("Attaching...");
        List<ProcessInfo> processes = getPids();

        System.out.println();
        System.out.println("Select an index to attach to:");
        for (int i = 0; i < processes.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + processes.get(i));
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            int index = Integer.parseInt(reader.readLine()) - 1;

            if(index < 0 || index >= processes.size()) {
                System.err.println("Invalid index.");
                return;
            }

            ProcessInfo selectedProcess = processes.get(index);
          //
           // attach(selectedProcess);

            System.out.println();

            System.out.println("Select your agent to attach:");

            File deployDir = getBuildDir();
            File[] files = deployDir.listFiles();

            if(files == null || files.length == 0) {
                System.err.println("No agents found. Try running the deploy task first.");
                return;
            }

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                System.out.println("[" + (i + 1) + "] " + file.getName());
            }

            int agentIndex = Integer.parseInt(reader.readLine()) - 1;
            if(agentIndex < 0 || agentIndex >= files.length) {
                System.err.println("Invalid index.");
                return;
            }

            File selectedAgent = files[agentIndex];

            System.out.println("Attaching " + selectedAgent.getName() + " to " + selectedProcess.getName() + " (" + selectedProcess.getPid() + ")");

            attach(selectedProcess, selectedAgent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
                if(split.length < 2) {
                    continue;
                }
                int pid = Integer.parseInt(split[0]);
                String name = split[1];
                processes.add(new ProcessInfo(pid, name));
            }
            reader.close();
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getDownloadDir() {
        return new File(getProject().getRootDir(),".gradle/download");
    }

    private File getBuildDir() {
        return new File(getProject().getRootDir(), "build/libs");
    }
}

class ProcessInfo {
    private int pid;
    private String name;

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
