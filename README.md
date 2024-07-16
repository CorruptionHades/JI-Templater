# JI Templater

This is a gradle plugin which generates the Yarn (fabric) Minecraft jar file for you.

To use it in your project, replace your build.gradle with this:
```gradle
buildscript {
    repositories {
        mavenCentral()
        mavenLocal()
        flatDir {
            dirs 'libs'
        }
    }
    dependencies {
        classpath files('path/to/JI-Template-1.0-SNAPSHOT.jar')
    }
}

plugins {
    id 'java'
}

apply plugin: 'ji_templater'

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // dependencies
    // we'll come back to it later
}
```

Once you've added it, run the setup task

![image](https://github.com/user-attachments/assets/de1173f4-e227-4589-8e8f-65b768f1a428)

This will download and remap everything for you.

To use it add this line to your dependencies:
```gradle
implementation files(".gradle/download/remapped-named.jar")
```

Now you have access to all the Minecraft classes in your project!
If you now want build it into a jar, the ``deploy`` task will do it for you!
You can find it here.

![image](https://github.com/user-attachments/assets/83ba9be0-fecb-4f0d-86b3-d0f52324e8ae)

There are 3 different jar files.
1. This is just your normal code that you wrote

![image](https://github.com/user-attachments/assets/8fafeffb-e38d-4130-9f13-4dd5f1293e7f)

2. This is mapped with the fabric intermediary mappings like they are used in mods.

![image](https://github.com/user-attachments/assets/cf2ca430-5b0d-4852-95c1-cf9130279045)

3. This is mapped to mojangs official mappings

![image](https://github.com/user-attachments/assets/eb35c5a4-9552-4d89-9bab-d9e080f6ddba)

(Decompiled with ReCaf)

There is also an ``attach`` task, which will let you inject a jar into a running java process as a java agent (as long as it is a valid agent).

## Info
Currently it will download the client 1.21 file and map it with tiny-remapper 0.10.4
