# ClickThrough Server

A server-only Fabric implementation of the `ClickThrough` mod for Minecraft.
Right-clicking a wall sign, item frame, glow item frame, or painting opens 
the container mounted directly behind it, completely compatible with vanilla
clients.

It also allows for individual players to disable it, with the command 
`/clickthrough [enable|disable]`

## Behaviour

This is configurable per entity type in `config/clickthrough-server/config.json`

```json
{
  "version": 1,
  "signs": "NORMAL_WHEN_SNEAKING",
  "itemFrames": "NORMAL_WHEN_SNEAKING",
  "glowItemFrames": "NORMAL_WHEN_SNEAKING",
  "paintings": "NORMAL_WHEN_SNEAKING"
}
```

Each field accepts one of:

- `NORMAL`                      - click-through disabled, vanilla behaviour.
- `NORMAL_WHEN_SNEAKING`        - click-through by default, normal while sneaking.
- `CLICKTHROUGH_WHEN_SNEAKING`  - normal by default, click-through while sneaking.

## Building


```
./gradlew build
```

The built jar will be in `build/libs/`. Drop it into your server's `mods/`
folder alongside the compatible Fabric API and Fabric Loader.
