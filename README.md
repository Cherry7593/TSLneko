# TSLneko 插件项目分析文档

## 项目概述

TSLneko 是一个为 Minecraft 服务器开发的 Bukkit/Paper 插件，主要功能是根据玩家的经济余额自动将玩家转换为"猫娘"状态，并提供相应的行为限制和特效。

### 基本信息
- **插件名称**: TSLneko
- **版本**: 1.0-SNAPSHOT
- **主类**: org.zvbj.tslneko.TSLneko
- **API版本**: 1.21
- **作者**: Zvbj
- **Folia支持**: 是

## 项目结构

```
TSLneko/
├── pom.xml                    # Maven 项目配置文件
├── TSL_Neko.iml              # IntelliJ IDEA 项目文件
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── zvbj/
│       │           └── tslneko/
│       │               ├── TSLneko.java           # 主插件类
│       │               ├── TSLnekoManager.java    # 核心管理器
│       │               ├── NekoCommand.java       # 指令处理器
│       │               └── listeners/
│       │                   ├── ChatListener.java    # 聊天监听器
│       │                   ├── DeathListener.java   # 死亡监听器
│       │                   └── FoodListener.java    # 食物监听器
│       └── resources/
│           ├── plugin.yml     # 插件描述文件
│           └── config.yml     # 配置文件
└── target/                   # 编译输出目录
```

## 核心功能分析

### 1. 经济监控系统
- **触发条件**: 当玩家余额 ≤ 5.0 TSLC 时自动触发猫娘状态
- **检测频率**: 每40 ticks (2秒) 检查一次在线玩家余额
- **经济插件**: 集成 XConomyAPI 进行余额查询

### 2. 猫娘状态管理
- **自动模式**: 基于经济余额自动切换状态
- **手动模式**: 管理员可通过指令强制设置玩家状态
- **状态持久化**: 使用内存缓存管理玩家状态

### 3. 行为限制系统
- **聊天后缀**: 自动在猫娘玩家的聊天消息后添加 "喵~"
- **食物限制**: 猫娘状态下只能食用指定的鱼类食物
  - 允许的食物: COD (鳕鱼), SALMON (鲑鱼), TROPICAL_FISH (热带鱼)
  - 特殊豁免: POTION (药水) 和 MILK_BUCKET (牛奶桶) 总是允许

### 4. 权限集成
- **LuckPerms集成**: 自动添加/移除 'catgirl' 权限组
- **状态切换时执行的指令**:
  - 进入猫娘: `lp user %player% parent add catgirl`
  - 退出猫娘: `lp user %player% parent remove catgirl`

## 技术架构

### 类设计

#### TSLneko (主类)
- **职责**: 插件生命周期管理、组件初始化
- **关键方法**:
  - `onEnable()`: 初始化配置、创建管理器、注册监听器和指令
  - `onDisable()`: 清理资源

#### TSLnekoManager (核心管理器)
- **职责**: 状态管理、余额检测、指令执行
- **关键功能**:
  - 定时余额检测循环
  - 玩家状态评估和切换
  - 配置重载
  - 食物权限检查

#### NekoCommand (指令处理)
- **支持的指令**:
  - `/tslneko reload` - 重载配置
  - `/tslneko make <玩家>` - 手动切换玩家状态
- **别名**: `/tneko`
- **权限**: `tslneko.admin` (默认OP)

### 监听器设计

#### ChatListener
- **监听事件**: AsyncPlayerChatEvent
- **功能**: 为猫娘玩家的聊天消息自动添加后缀

#### FoodListener  
- **监听事件**: PlayerItemConsumeEvent
- **功能**: 限制猫娘玩家只能食用特定食物

#### DeathListener
- **监听事件**: PlayerDeathEvent
- **功能**: 玩家死亡后重新评估状态（考虑死亡扣款）

## 配置系统

### config.yml 配置项

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| threshold | double | 5.0 | 触发猫娘状态的余额阈值 |
| chat_suffix | string | "喵~" | 聊天后缀 |
| checkPeriodTicks | long | 40 | 检测周期(tick) |
| food_restriction | boolean | true | 是否启用食物限制 |
| allowed_foods | list | [COD, SALMON, TROPICAL_FISH] | 允许的食物列表 |
| apply_commands | list | ["lp user %player% parent add catgirl"] | 进入猫娘时执行的指令 |
| remove_commands | list | ["lp user %player% parent remove catgirl"] | 退出猫娘时执行的指令 |

## 依赖关系

### Maven 依赖
```xml
<dependencies>
    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Folia API -->
    <dependency>
        <groupId>dev.folia</groupId>
        <artifactId>folia-api</artifactId>
        <version>1.21.4-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- XConomy API -->
    <dependency>
        <groupId>com.github.YiC200333</groupId>
        <artifactId>XConomyAPI</artifactId>
        <version>2.25.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 外部插件依赖
- **XConomy**: 经济系统插件，用于余额查询
- **LuckPerms**: 权限管理插件，用于权限组管理

## 特性与优势

### 1. Folia 兼容性
- 使用 GlobalRegionScheduler 进行任务调度
- 支持多线程服务器环境
- 异步安全的设计模式

### 2. 配置灵活性
- 可自定义触发阈值
- 可配置检测频率
- 支持自定义指令执行
- 可选的食物限制功能

### 3. 错误处理
- XConomy API 异常处理
- 配置文件容错机制
- 安全的玩家状态管理

### 4. 性能优化
- 使用 ConcurrentHashMap 进行线程安全的状态存储
- 最小化数据库查询频率
- 高效的事件监听器设计

## 使用场景

1. **经济服务器**: 为经济困难的玩家提供特殊身份
2. **角色扮演服务器**: 增加游戏趣味性和互动性
3. **社交服务器**: 创造独特的玩家体验

## 潜在改进方向

1. **数据持久化**: 添加数据库支持，保存玩家状态历史
2. **更多限制**: 扩展猫娘状态下的行为限制
3. **UI界面**: 添加GUI配置界面
4. **多语言支持**: 国际化消息系统
5. **统计功能**: 添加猫娘状态统计和报告

## 部署说明

1. 确保服务器安装了 XConomy 和 LuckPerms 插件
2. 将编译后的 TSLneko-1.0-SNAPSHOT.jar 放入 plugins 目录
3. 启动服务器，插件会自动生成默认配置文件
4. 根据需要修改 config.yml 配置
5. 使用 `/tslneko reload` 重载配置

---
*文档生成时间: 2025年7月18日*
*插件版本: 1.0-SNAPSHOT*
