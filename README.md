# JI-MC-Ver

This is a gradle plugin which generates the Yarn mapped (fabric) Minecraft jar file for you.

To use it in your project, replace your build.gradle with this:
```gradle
plugins {
    id 'java'
    id 'ji-mc-ver' version '1.0' // uses local maven repository
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    
}
```

Create a file called ``config.properties`` in the root of your project. Here you can specify the process name and the agent name.

Example:
```properties
processName=net.fabricmc.loader.impl.launch.knot.KnotClient 
agentName=Ji-Template-Test-fabric.jar
```

In your ``build.gradle`` Use the ``jimc()`` function to specify the Minecraft version you want to use.
```gradle
dependencies {
    jimc("1.21")
}
```

You can find the downloads in ```.gradle/.ji/download/```.

**Note: ji-mc-ver only supports a single version at a time. You can specify multiple versions and they will download, but only the last version will be the dependency!**

Now you have access to all the Minecraft classes in your project!
If you now want build your project into a jar, the ``deploy`` task will do it for you!

There will be 3 different output jar files.

![image](https://github.com/user-attachments/assets/83ba9be0-fecb-4f0d-86b3-d0f52324e8ae)

1. This is just your normal code that you wrote

![image](https://github.com/user-attachments/assets/8fafeffb-e38d-4130-9f13-4dd5f1293e7f)

2. This is mapped with the fabric intermediary mappings like they are used in mods.

![image](https://github.com/user-attachments/assets/cf2ca430-5b0d-4852-95c1-cf9130279045)

3. This is mapped to mojangs official mappings

![image](https://github.com/user-attachments/assets/eb35c5a4-9552-4d89-9bab-d9e080f6ddba)

(Decompiled with ReCaf)

There is also an ``attach`` task, which will let you inject a jar into a running java process as a java agent (as long as it is a valid agent).
