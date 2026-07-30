# wxh1024-addon

一个 [Meteor Client](https://meteorclient.com/) 的附属模组（Addon），运行于 Fabric Loader + Minecraft 1.21.11（Java 版）。

- 模组 ID：`wxh1024-addon`
- 包名：`com.wxh1024.addon`
- 自定义分类：`wxh1024`（青色图标）

## 构建

需要 JDK 21。

```bash
./gradlew build
```

构建产物位于 `build/libs/wxh1024-addon-<version>.jar`，放入 Minecraft 的 `mods/` 目录即可（需同时安装 Meteor Client）。

## 模块（Modules）

| 模块 | 分类 | 功能 |
|------|------|------|
| 自动回家 `Home` | wxh1024 | 监测图腾数量 / 高度，低于阈值时自动发送 `/home`、`/spawn` 等指令，带冷却与触发锁存。 |
| 展示框开箱 `ItemFrameChestOpener` | wxh1024 | 右键物品展示框时，自动打开其后方的容器（箱子、木桶、潜影盒、发射器、漏斗、熔炉、末影箱、酿造台、合成器等，可逐项开关）。 |
| 滴落测试 `DropTest` | wxh1024 | 移植自参考项目的滴落/放置测试辅助功能。 |
| Controlify 兼容 `ControlifyCompat` | wxh1024 | 兼容 Controlify 手柄按键绑定。 |

## 命令（Commands）

所有命令以 `.` 开头（Meteor 命令前缀），回显由 Meteor 统一加前缀。

### `.3c3u <玩家名>`

查询 `3c3u.info` 服务器上指定玩家的信息，并以彩色排版打印到聊天栏（在线状态、UUID、首次加入、最近上线/下线、总在线/近一月/日均时长、击杀/死亡/KD、发言数、备注）。

- 别名：`.c3u`
- 联网失败会提示「网络请求失败」。

示例：

```
.3c3u wxh1024
```

### `.tp <名称>`

触发预设的别名指令：把 `<名称>` 对应的消息通过聊天栏发送（以 `/` 开头会自动作为指令执行）。

### `.tpa` 别名管理

| 命令 | 作用 |
|------|------|
| `.tpa add <名称> <消息指令>` | 新增 / 覆盖一个别名（同名会覆盖，提示「已更新」）。 |
| `.tpa del <名称>` | 删除别名。 |
| `.tpa list` | 彩色列表查看全部别名（含数量）。 |
| `.tpa` | 单独执行打印使用说明。 |

别名数据持久化保存在 Meteor 配置目录的 `tp_aliases.txt`，重启不丢失。

示例：

```
.tpa add home /home
.tpa add spawn /spawn
.tp home
.tpa list
.tpa del home
```

## 多版本分支

仓库按 Minecraft 版本分分支维护，各分支的 `gradle.properties` 已指向对应版本：

| 分支 | Minecraft | Yarn 映射 |
|------|-----------|-----------|
| `main` | 1.21.11 | 1.21.11+build.4 |
| `1.21.1` | 1.21.1 | 1.21.1+build.3 |
| `1.21.4` | 1.21.4 | 1.21.4+build.8 |

`build.gradle` 通过 `gradle.properties` 里的 `meteor_jar` 变量引用本地的 Meteor 客户端 JAR：

```
modApi files("${rootDir}/libs/${project.meteor_jar}")
```

构建对应分支前，需确保该分支 `libs/` 下存在本版本对应的 Meteor 客户端 JAR，并让 `meteor_jar` 指向其文件名。**各分支的 `libs/` 只保留自己版本的 jar**（已纳入 git 版本管理，克隆即自带，无需手动补）：

| 分支 | `libs/` 中的 jar | `meteor_jar` |
|------|------------------|--------------|
| `main` | `meteor-client-1.21.11.jar` | `meteor-client-1.21.11.jar` |
| `1.21.1` | `meteor-client-1.21.1.jar` | `meteor-client-1.21.1.jar` |
| `1.21.4` | `meteor-client-1.21.4.jar` | `meteor-client-1.21.4.jar` |

`build.gradle` 通过 `gradle.properties` 里的 `meteor_jar` 变量引用对应的 jar：

```gradle
modApi files("${rootDir}/libs/${project.meteor_jar}")
```

运行时还需把与版本匹配的 Meteor Client 及 Controlify 等放入 Minecraft 的 `mods/` 目录。

## 仓库

私有仓库：https://github.com/wxh1024/wxh1024-addon

> 注：`hybridious_mod-main/`（第三方参考项目）仅用于本地移植参考，已加入 `.gitignore`，不会提交到仓库。
> 注：`libs/` 下的 Meteor 客户端 JAR 已纳入版本管理（`.gitignore` 中以 `!libs/*.jar` 放行），按分支各自保存对应版本，克隆仓库即可直接构建。
