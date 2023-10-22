# BedwarsGames

[[English](README.MD)] | [[简体中文](README_zh-CN.MD)]

---

## Introduction

This is a plugin depends on [BedwarsRel](https://github.com/BedwarsRel/BedwarsRel) 1.3.6.

This plugin allows you to use special settings for a game.

## Use

### Random map selector

You can use command ```/bedwarsgames:bedwarsgames mode solo``` open your first map selector.

It also supported random a map to join.

You can add or remove a mode and modify the menu like this:

```yaml
...
modes:
  solo:
    name: 'Solo'
    menu:
      title: 'Bedwars (Solo)'
      rows: 4
      items:
        random:
          slot: 12
          name: '§aBedwars (Solo)'
          lore:
            - '§7Play Solo bedwars game.'
            - ' '
            - '§eClick play!'
        map:
          slot: 14
          name: '§aSelect Map (Solo)'
          lore:
            - '§7Choose a map'
            - '§7from active maps on server.'
            - ' '
            - '§eClick choose!'
        close:
          slot: 31
          name: '§cClose'
          lore: []
        page:
          slot: 31
          name: '§ePage §f[§e{page}§f/§b{max_page}§f]'
          lore: 
            - '§7Click back to menu'
        previous:
          slot: 27
          name: '§ePrevious'
          lore: []
        next:
          slot: 35
          name: '§eNext'
          lore: []
    games:
      - Game1
      - Game2
...
```

### Shop

You can change shop for only one and more games. You need to drag the shop file to the **plugins/BedwarsGames/shops** folder.

For example, you just need to change **games** section of **config.yml** like below that:

```yaml
...
games:
  tnt-mode-game:
    shop: tnt-shop.yml
...
```

That is! Only the game when it started plugin will replace the shop to you want.

### Damage

You can modify to enable damages in a game like this:

```yaml
...
games:
  tnt-game:
    damage:
      FALL: false
      FIRE: false
  no-void-game:
    damage:
      FALL: false
      FIRE: false
      FIRE_TICK: false
      DROWNING: false
      BLOCK_EXPLOSION: false
      ENTITY_EXPLOSION: false
...
```

## Languages

You can modify **config.yml** change the messages, or you can download the language of the **config.yml** on [here](src/main/resources/languages).

## Develop

You can try to download the plugin and import it as a dependency. And modify **plugin.yml** like this:

```yaml
...
depend:
  - BedwarsGames
...
```