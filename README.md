# MythicLib – FancyHolograms Fork  (中 / EN)

---

## 简介 (ZH)
本仓库基于 [io.lumine/PhantomProject/MythicLib](https://github.com/IO-Lumine/PhantomProject) 的 **MythicLib 1.7.1-SNAPSHOT** 进行二次开发，
重点解决了伤害指示器（Damage Indicator）只能使用护甲座（ArmorStand）实现的问题。

此 Fork 通过 **[FancyHolograms](https://github.com/FancyMc/FancyHolograms)** 替换默认的全息实现，
支持 2.x 与 3.x API，运行时自动识别并以反射方式调用，无需强制依赖。

### 主要特性
1. **伤害数字使用 FancyHolograms 渲染**：不再生成实体，性能更优。  
2. **兼容 FH 2.x 与 3.x**：`FancyHologramsFactory` 通过反射自适应。  
3. **自动禁用 InteractionTrait**：避免在 `plugins/FancyHolograms/data/traits/interaction_trait/` 目录生成大量 JSON。  
4. **升级到 Paper-API 1.21.5**。  
5. **精简构建**：`pom.xml` 仅保留 `mythiclib-plugin / -dist / -v1_21_r4 / -vr` 模块，编译速度更快。  

### 快速开始
1. 下载/编译本 Fork（见下文 _Build_ 部分），将生成的 `MythicLib-1.7.1-SNAPSHOT.jar` 置于服务器 `plugins/`。  
2. 放入对应版本的 `FancyHolograms`（≥ 2.0.0）。如使用 3.x，需同时安装 `FancyNpcs`；本补丁已自动屏蔽 InteractionTrait，可不放置亦可。  
3. 打开 `plugins/MythicLib/config.yml`：
   ```yml
   hologram-provider: FancyHolograms
   ```
4. 重启服务器，击打生物即可看到由 FH 渲染的伤害数字。

---

## Overview (EN)
This fork of **MythicLib 1.7.1-SNAPSHOT** replaces the default armor-stand based damage indicators with 
**FancyHolograms** text holograms.

Key goals:
- Zero runtime entities ➜ better performance.  
- Works with **FancyHolograms** **2.x _and_ 3.x** APIs via reflection.  
- Disables the default **`InteractionTrait`** to prevent junk JSON files in 
  `plugins/FancyHolograms/data/traits/interaction_trait/`.  
- Updated to **Paper-API 1.21.5**.  
- Trimmed build modules for faster compilation.

### How to use
1. Build or download `MythicLib-1.7.1-SNAPSHOT.jar` from this repo and drop it into your server's `plugins/` folder.  
2. Install **FancyHolograms** (≥ 2.0). 3.x also needs **FancyNpcs**, but the fork unregisters its default trait so it is optional.  
3. Edit `plugins/MythicLib/config.yml` and set:
   ```yml
   hologram-provider: FancyHolograms
   ```
4. Restart the server — hit any mob and you will see floating damage numbers rendered by FH.

---

## Build / 编译
本仓库使用 **Maven**。

```bash
# Windows / *nix
mvn clean install -DskipTests
```

生成的 JAR 位于 `target/` 目录。

> 若仅想使用，请在「Releases」或 Actions Artifact 获取已编译包。

---

## 依赖 / Dependencies
- **FancyHolograms 2.x / 3.x**  (必需)  
- **FancyNpcs** ≥ 2.6 _(only if you need InteractionTrait in FH ≥ 3.0; otherwise optional)_  
- Paper / Spigot 1.19+ (tested on **1.21.5**)

---

## Fork 修改要点 / Changes in this fork
| File / Class | Purpose |
|--------------|---------|
| `io.lumine.mythic.lib.hologram.factory.FancyHologramsFactory` | New factory bridging MythicLib->FancyHolograms (reflection dual-path) & unregistering *InteractionTrait* |
| `config.yml` | default `hologram-provider` doc updated |
| Root `pom.xml` | modules trimmed; Paper API bumped to 1.21.5; FH/FN repos added |
| Various gradle sub-projects | stubbed out `git` calls; disabled strict Javadoc to ease build |

---

## 致谢 / Credits
- [Lumine Team](https://www.mythicmobs.net/) for the original MythicLib.  
- [FancyPlugins](https://github.com/FancyMc/) for FancyHolograms & FancyNpcs.  
- Community contributors for bug reports & testing.

---

## License / 许可
原项目使用 GPL-3.0。本 Fork 保持相同许可证。详情见 `LICENSE`.

### Using MythicLib as dependency
Register the PhoenixDev repo
```
<repository>
    <id>phoenix</id>
    <url>https://nexus.phoenixdevt.fr/repository/maven-public/</url>
</repository>
```
Then add MythicLib-dist as dependency
```
<dependency>
    <groupId>io.lumine</groupId>
    <artifactId>MythicLib-dist</artifactId>
    <version>1.5.2-SNAPSHOT</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```

### Compiling MythicLib
MythicLib centralizes all the version-dependent code for MMOItems and MMOCore, requiring the use of NMS instead of the regular Bukkit API. Since Spigot 1.17, you now need to run a few additional commands if you're willing to use server NMS code. I encourage you to read [this post](https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/#post-4184317) first.

Here are the commands you can use to generate the required server artifacts using [BuildTools](https://www.spigotmc.org/wiki/buildtools/). Additional Note: since spigot 1.17 runs on Java 16 and my default Java installation is version 17, I had to redownload a Java 16 JDK and have BuildTools run on that one instead in order to build a remapped spigot 1.17.
```
"C:\Program Files\Java\jdk-16.0.1\bin\java" -jar BuildTools.jar --rev 1.17   --remapped
java                                        -jar BuildTools.jar --rev 1.18   --remapped
java                                        -jar BuildTools.jar --rev 1.18.2 --remapped
java                                        -jar BuildTools.jar --rev 1.19   --remapped
java                                        -jar BuildTools.jar --rev 1.19.3 --remapped
java                                        -jar BuildTools.jar --rev 1.20.1 --remapped
// etc...
```

To save time, you can also keep the only version that corresponds to your server build and remove the rest.
Version wrappers all have a different Maven modules, so just keep the one you're interested in.
This method also allows you to directly use your server JAR as plugin dependency, simplifying dependency management.

The official Phoenix repo contains MythicLib >1.5.2 builds, so you can work on a custom build of MMOItems or MMOCore without having to locally build MythicLib, if you're only considering minor edits.