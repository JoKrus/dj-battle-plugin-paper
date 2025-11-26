# DJ-Battle Plugin

Minecraft Paper Plugin developed mainly for usage on the DJ-Battle Server but can be used elsewhere with ease

## Features

### Teams

Create, join and use teams via the `/djteam` command

### Spectator Mode

Spectate your teammates and the battle automatically.

- Switch targets via `/djspec`
- Fly around as usual after your team is eliminated

### API

Supports events via `BattleStartedEvent`and `BattleStoppedEvent`

- add `depends: PaperDjBattle` to your `plugin.yml` to use them

### Configurable

Configure the lobby location, spawn location, grace period, map size and map shrink duration easily via a config file

## Usage

### Config

- Not documented completely right now!
    - After first server start, check `plugins/PaperDjBattle/config.yml` to modify

### Setup

- Use `/djbattle init` and `/djbattle reload` to apply your config to the server and set up the environment
- Let every player setup their teams via `/djteam join`
- Test if every player is in a team via `/djteam test`

### Start

- Use `/djbattle start` to start a battle

### Stop

- If the battle should be stopped, use `/djbattle stop`
    - It will check after `stop` if only 1 team is left and declare that team as a winner.
    - Happens automatically, too

## Building the jar

### Prerequisites

- Java 21+
- Gradle

### Build

- ```git checkout https://github.com/JoKrus/dj-battle-plugin-paper.git```
- ```cd dj-battle-plugin-paper```
- ```./gradlew jar```
- `build/libs/PaperDjBattle-{Version}.jar` is the built plugin.
- Copy that into your `plugins/` directory on your Paper Server

## Contribution

At the moment, it is not planned to take contributions. If you discover an issue or have a feature request,
you can always create an issue.

If the feature fits into the plugin, we can discuss implementation of it.