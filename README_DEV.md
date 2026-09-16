<!--
==============================================================================
LuckyWheel - Minecraft Plugin
Copyright (c) 2026 Daperkz

README
==============================================================================
-->
# `LuckyWheel` — Fully Customizable & Animated Wheel Plugin

[![Build Status](https://img.shields.io/badge/build-Gradle%209.1-brightgreen?style=for-the-badge&logo=gradle)](https://gradle.org/)
[![Minecraft Support](https://img.shields.io/badge/minecraft-1.20.6%20%E2%80%93%2026.3-blue?style=for-the-badge&logo=minecraft)](https://papermc.io)
[![Paper API](https://img.shields.io/badge/platform-Paper%20%2F%20Purpur-informational?style=for-the-badge&logo=paper)](https://purpurmc.org)
[![Folia Ready](https://img.shields.io/badge/folia-supported-9cf?style=for-the-badge)](https://papermc.io/software/folia)
[![Java Version](https://img.shields.io/badge/java-21%20%2F%2025-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

`LuckyWheel` is a high-performance, feature-rich Minecraft plugin developed for **Paper / Purpur / Folia**. The build produces separate JARs for Minecraft `1.20.6`, `1.21`, `1.21.1`, `1.21.4`, `26.1`, `26.2`, and `26.3` (alpha). Older targets use Java 21 bytecode; 26.x targets use Java 25 bytecode.

> ⚡ **Paper & Folia Ready**: Built from the ground up to support both traditional Paper tick loops and Folia region-based execution models via Kyori MiniMessage, global region schedulers, and entity-scheduled inventory interactions.

---

## Table of Contents
- [Key Features](#key-features)
- [Directory Architecture](#directory-architecture)
- [Core Modules & Class Reference](#core-modules--class-reference)
- [Prerequisites](#prerequisites)
- [Build System & Compilation](#build-system--compilation)
- [Commands & Permissions](#commands--permissions)
- [Configuration Reference](#configuration-reference)
- [Wheel & Ticket Setup](#wheel--ticket-setup)
- [Performance & Thread Safety Guarantees](#performance--thread-safety-guarantees)
- [AI Disclosure & Usage](#ai-disclosure--usage)
- [License](#license)

---

## Key Features

- **Animated GUI Wheel Engine**: Real-time sliding inventory GUI animation with dynamic tick delays, start delays, smooth slowdown curves, and designated winning slots.
- **Persistent Data Protection**: Tickets are securely tagged using Spigot/Paper `PersistentDataContainer` (PDC) storing `TICKET_KEY` and `OWNER_KEY` (UUID), ensuring tickets cannot be forged, duplicated, or stolen by other players.
- **Weighted Chance System**: Fully customizable chance/weight probability engine for prizes, supporting everything from high-frequency common drops to ultra-rare 0.001% legendary rewards.
- **Multi-Wheel Configuration**: Modular architecture supporting unlimited separate wheel YAML configuration files (e.g., `wheels/Daily.yml`, `wheels/VIP.yml`).
- **Flexible Rewards & Holograms**: Supports giving raw item drops, playing distinct win sounds, showing custom item display names and lore holograms, and executing console commands dynamically with `%player%` placeholders.
- **Folia Multi-Threading Support**: Fully compatible with Folia region-based execution using `getGlobalRegionScheduler()` for console commands and entity-based task scheduling.

---

## Directory Architecture

```text
LuckyWheel/

├── .github/
│   └── workflows/
│       └── ci.yml                  # GitHub Actions CI/CD Pipeline (Build & Release)
├── Makefile                        # Compilation & packaging shortcut recipes
├── build.gradle.kts                 # Kotlin DSL build and Paper API version matrix
├── settings.gradle.kts              # Gradle repositories and project settings
├── gradlew                          # Gradle Wrapper entry point
├── src/
│   └── main/
│       ├── java/
│       │   └── com/daperkz/luckywheel/
│       │       ├── LuckyWheelPlugin.java   # Main plugin entry point & lifecycle manager
│       │       ├── command/
│       │       │   ├── CommandHandler.java # Main command executor & sub-command parser
│       │       │   ├── WheelTabCompleter.java
│       │       │   └── sub/
│       │       │       ├── GiveSubCommand.java
│       │       │       ├── ReloadSubCommand.java
│       │       │       ├── SpinSubCommand.java
│       │       │       └── SubCommand.java
│       │       ├── config/
│       │       │   └── WheelConfigManager.java # Dynamic wheel YML loader & store
│       │       ├── listener/
│       │       │   └── InventoryClickListener.java # Right-click ticket & GUI click intercepter
│       │       ├── manager/
│       │       │   ├── InventoryManager.java   # NBT/PDC verification & item generator
│       │       │   ├── SoundManager.java       # Configuration sound execution wrapper
│       │       │   └── WheelManager.java       # Weighted probability random prize engine
│       │       └── wheel/
│       │           └── WheelAnimation.java     # Runnable GUI animation controller
│       └── resources/
│           ├── plugin.yml          # Plugin metadata, commands, & permissions
│           └── wheels/
│               └── Daily.yml       # Default sample wheel configuration
├── LICENSE
└── README.md
```

---

## Core Modules & Class Reference

| Module | Primary Class | Key Capabilities |
| :--- | :--- | :--- |
| **`luckywheel`** | `LuckyWheelPlugin` | Plugin startup lifecycle, PDC key registrar (`wheel_ticket`, `owner`), and command/event listener setup. |
| **`command`** | `CommandHandler` | Command dispatcher handling routing for `/luckywheel <spin\|give\|reload>`. |
| **`command.sub`** | `GiveSubCommand` | Admin command allocating signed PDC tickets to online players with custom amounts or prize overrides. |
| **`command.sub`** | `SpinSubCommand` | Command trigger validating player-held tickets and starting `WheelAnimation`. |
| **`command.sub`** | `ReloadSubCommand` | Live hot-reloader for plugin settings and all custom wheel configuration files. |
| **`config`** | `WheelConfigManager` | Manages reading, indexing, and runtime storage of YAML files located under `plugins/LuckyWheel/wheels/`. |
| **`listener`** | `InventoryClickListener` | Handles ticket consumption on right-click and cancels GUI clicks to prevent prize stealing during wheel spins. |
| **`manager`** | `InventoryManager` | Encapsulates PDC ticket ownership checks, secure item stack deduction, and legacy/MiniMessage text parsing. |
| **`manager`** | `WheelManager` | Calculates cumulative weight algorithms for selecting weighted random prize keys. |
| **`wheel`** | `WheelAnimation` | Multi-threaded `ScheduledTask` managing inventory ticks, shift movement, sound ticks, and final reward delivery. |

---

## Prerequisites

To build and run `LuckyWheel`, ensure your server environment meets the following requirements:

- **Java Development Kit (JDK)**: Java 25 for the default all-version build; Java 21 for 1.20/1.21-only builds
- **Build System**: Gradle Wrapper 9.1 and GNU Make
- **Minecraft Server Engine**: Paper, Purpur, or Folia (`1.20.6` – `26.3`)

---

## Build System & Compilation

The repository uses the Gradle Wrapper and Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`). `make` checks for the required JDK and downloads a project-local Eclipse Temurin JDK into `.tools/` when necessary.

### Build Commands

```bash
# Compile all configured Minecraft versions
make build

# Clean the Gradle build directory
./gradlew clean

# Package one JAR per configured Minecraft version
make jar

make clean
make re
```

The versioned JAR files are generated under `./build/libs/`, for example `LuckyWheel-1.20.6-1.2.3.jar`.

### Compiling for Multiple Java Versions

The default matrix contains Paper APIs for Minecraft 1.20.6, 1.21, 1.21.1, 1.21.4, 26.1, 26.2, and 26.3. The 26.3 API is currently an alpha release. Minecraft 1.20/1.21 JARs target Java 21; 26.x JARs target Java 25. Configure another matrix with `PAPER_VERSIONS`; entries use `minecraftVersion[:paperApiVersion]`:

```bash
# Build the default matrix
make jar

# Build a selected matrix
make jar PAPER_VERSIONS="1.21,1.21.4"
make jar PAPER_VERSIONS="1.20.6:1.20.6-R0.1-SNAPSHOT,1.21.4:1.21.4-R0.1-SNAPSHOT"
make jar PAPER_VERSIONS="26.1:26.1.2.build.74-stable,26.2:26.2.build.124-stable,26.3:26.3.build.8-alpha"

# The default compiler JDK is Java 25 because Paper 26.x requires it
make jar

# Build only older releases with Java 21
make jar JAVA_VERSION=21 PAPER_VERSIONS="1.20.6,1.21,1.21.1,1.21.4"

# See which JDKs Gradle can detect
./gradlew javaToolchains
```

The selected JDK must be a full JDK, not a JRE. `make` requires `curl` and `tar` only when it needs to download one. The downloaded JDK is ignored by Git. Each JAR is compiled independently against its Paper API, so a newer API cannot accidentally leak into older-version builds.

Upon successful compilation, the output `.jar` file will be generated under the `./build/libs/` directory:
```text
build/libs/LuckyWheel-*.*.*.jar
```

---

## Commands & Permissions

### Commands

| Command | Usage | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/luckywheel spin` | `/luckywheel spin <wheel>` | Consumes 1 valid ticket held in hand and starts the wheel spin animation. | `Daperkz.luckywheel` |
| `/luckywheel give` | `/luckywheel give <wheel> [player] [amount] [prize_id]` | Grants bounded tickets linked to the player's UUID. | `Daperkz.luckywheel.admin` |
| `/luckywheel reload` | `/luckywheel reload` | Reloads all wheel configurations and resets active schedulers. | `Daperkz.luckywheel.admin` |

*Aliased as `/lw` for convenience.*

### Permissions

```yaml
permissions:
  Daperkz.luckywheel:
    description: Permission to use /luckywheel commands and spin wheels
    default: true
  Daperkz.luckywheel.admin:
    description: Permission to reload plugin and give ticket items
    default: op
```

---

## Configuration Reference

`LuckyWheel` uses a modular structure where individual wheels reside inside the `wheels/` subfolder.

### Main Config (`config.yml`)
```yaml
settings:
  debug: false
```

### Sample Wheel Configuration (`wheels/Daily.yml`)
```yaml
settings:
  slots: 16
  rotation: CLOCKWISE
  total-ticks: 55
  animation-speed: 1
  slowdown-ticks: 2
  start-delay-ticks: 30
  winning-slot: 5

sounds:
  open: "BLOCK_CHEST_OPEN"
  spin: "BLOCK_NOTE_BLOCK_PLING"
  win_default: "ENTITY_EXPERIENCE_ORB_PICKUP"

ticket:
  material: PAPER
  name: "&6Lucky Wheel Ticket"
  lore:
    - "&d&lClique Gauche Pour Tourner la Roue!"
    - "&fBonne Chance!"
  custom-model-data: 0

permissions:
  require-permission: false
  admin-require-permission: true

prizes:
  prize1:
    material: ENCHANTED_GOLDEN_APPLE
    quantity: 1
    chance: 1000 # 0.1%
    sound: "ENTITY_PLAYER_LEVELUP"
  prize5:
    material: OAK_LOG
    quantity: 20
    chance: 157900
  prize15:
    material: PAPER
    quantity: 1
    chance: 50000
    hologram: ["&d&lReroll Ticket", "&fUn nouveau ticket!"]
    commands: ["lw give Daily %player% 1 prize15"]
  prize16:
    material: BARRIER
    quantity: 1
    chance: 100000
    hologram: ["&c&lRien du tout", "&7Dommage..."]
    commands: ["say %player% a perdu!"]
    sound: "ENTITY_VILLAGER_NO"
```

---

## Wheel & Ticket Setup

1. **Creating a New Wheel**: Add a new `.yml` file under `plugins/LuckyWheel/wheels/` (e.g., `VIP.yml`).
2. **Issue Tickets**: Run `/lw give VIP <player> <amount>` to issue PDC-bound tickets to a target player.
3. **Spinning the Wheel**:
   - **Method A (Interactive)**: Right-click air or a block while holding the matching ticket in your main hand.
   - **Method B (Command)**: Execute `/lw spin VIP` while holding the required ticket.
4. **Ticket Safety**: Each ticket contains the `wheel_ticket` identifier and the player's explicit `owner` UUID stored directly inside the item's `PersistentDataContainer`. Players cannot trigger a wheel with forged items or another player's ticket.

---

## Performance & Thread Safety Guarantees

- **Folia Multithreading**: Utilizes Paper's `player.getScheduler().runAtFixedRate()` for region-bound inventory updates and `Bukkit.getGlobalRegionScheduler()` for console command dispatching, avoiding cross-thread exceptions.
- **Safe Inventory Cancellation**: Prevents player item extraction from animated GUI menus using strict title checking via Kyori `PlainTextComponentSerializer`.
- **Memory Efficiency**: Custom wheels are indexed on startup in lightweight memory maps (`WheelConfigManager`), allowing rapid zero-lag lookup during spins.

---

## AI Disclosure & Usage

`LuckyWheel` was developed with assistance from Artificial Intelligence tools (Gemini) throughout its development lifecycle. AI capabilities were utilized for:

* **Code Architecture & Refactoring**: Assisting with thread-safety patterns, Paper/Folia scheduler abstractions, and PDC item management.
* **Configuration & Logic Design**: Designing the cumulative probability wheel weight engine and GUI tick animation mathematics.
* **Documentation**: Generating technical specifications, command breakdowns, and project `README` assets.

All AI-generated outputs, logic paths, and safety checks were reviewed, tested, and validated by the maintainer to ensure maximum stability and performance across Paper and Folia environments.

---

## License

Distributed under the MIT License. See [`LICENSE`](./LICENSE) for more information.
