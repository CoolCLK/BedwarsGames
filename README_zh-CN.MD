# BedwarsGames

[[English](README.MD)] | [[简体中文](README_zh-CN.MD)]

---

## 简介

这是一个依赖于 [BedwarsRel](https://github.com/BedwarsRel/BedwarsRel) 1.3.6 的插件，同时支持 [BedwarsXP](https://github.com/SakuraKoi/BedwarsXP)。

这个插件允许你对游戏进行特别设定。

## 使用

### 随机地图选择

你可以使用指令 ```/bedwarsgames:bedwarsgames mode solo``` 打开你的第一个地图选择器。

它也支持随机一张地图加入。

你可以添加或移除一个模式，也可以像这样修改界面:

```yaml
...
modes:
  solo:
    name: '单人'
    menu:
      title: '起床战争 (单人)'
      rows: 4
      items:
        random:
          slot: 12
          name: '§a起床战争 (单人)'
          lore:
            - '§7游玩单人起床战争'
            - ' '
            - '§e点击游玩!'
        map:
          slot: 14
          name: '§a选择地图 (单人)'
          lore:
            - '§7从服务器可用的地图列表中'
            - '§7选择你想要游玩的地图。'
            - ' '
            - '§e点击选择!'
        close:
          slot: 31
          name: '§c关闭'
          lore: []
        page:
          slot: 31
          name: '§e页面 §f[§e{page}§f/§b{max_page}§f]'
          lore:
            - '§7点击返回界面'
        previous:
          slot: 27
          name: '§e上一页'
          lore: []
        next:
          slot: 35
          name: '§e下一页'
          lore: []
    games:
      - Game1
      - Game2
...
```

### 商店

你可以对某个游戏设定特别的商店。你需要把商店文件拖到文件夹 **plugins/BedwarsGames/shops** 。

举个例子，你只需要像这样在 **config.yml** 的 **games** 中如此修改:

```yaml
...
games:
  tnt-mode-game:
    shop: tnt-shop.yml
...
```

那就好了! 你就可以在特定游戏中使用特定商店了。

### 伤害

你可以像这样修改某个游戏的特定伤害:

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

## 开发

你可以下载插件并将插件作为依赖导入，并在 **plugin.yml** 如此修改:

```yaml
...
depend:
  - BedwarsGames
...
```