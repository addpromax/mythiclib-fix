
### Using MythicLib as dependency
Register the PhoenixDev repo
```
<repository>
    <id>phoenix</id>
    <url>https://nexus.phoenixdvpt.fr/repository/maven-public/</url>
</repository>
```
Then add MythicLib-dist as dependencyy
```
<dependency>
    <groupId>io.lumine</groupId>
    <artifactId>MythicLib-dist</artifactId>
    <version>1.3.4-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Compiling MythicLib
MythicLib centralizes all the version-dependent code for MMOItems and MMOCore, meaning it uses NMS instead of the regular spigot API. Since Minecraft server builds now use obfuscated mappings ([since 1.17](https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/)) you will first need to setup your mojang-remapped spigot jars for the following versions: 1.17, 1.18, 1.18.2, 1.19.

Here are the instructions you can use to generate the four required mojang-remapped jars (using [BuildTools](https://www.spigotmc.org/wiki/buildtools/)). Since spigot 1.17 runs on Java 16 and my default Java installation is version 17, I had to redownload a Java 16 JDK and have BuildTools run on that one instead in order to build a remapped spigot 1.17.
```
"C:\Program Files\Java\jdk-16.0.1\bin\java" -jar BuildTools.jar --rev 1.17   --remapped
java                                        -jar BuildTools.jar --rev 1.18   --remapped
java                                        -jar BuildTools.jar --rev 1.18.2 --remapped
java                                        -jar BuildTools.jar --rev 1.19   --remapped
```

Since MythicLib 1.3.4 it is no longer required to first compile MythicLib before compiling MMOItems and MMOCore since the PhoenixDvpt repo now includes MythicLib, MMOItems and MMOCore development artifacts.
