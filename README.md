# ClickThrough Server

A server-side multi-loader implementation of the `ClickThrough` mod for
Minecraft. Right-clicking a wall sign, item frame, glow item frame, or painting
opens the container mounted directly behind it, completely compatible with
vanilla clients.

It also allows for individual players to disable it, with the command
`/clickthrough [enable|disable]`

This data is stored in `config/clickthrough-server/players.json`

## Supported loaders

- Fabric (requires Fabric API)
- NeoForge
- Quilt

## Behaviour

This is configurable per object type in `config/clickthrough-server/config.json`

```json
{
  "version": 2,
  "modes": {
    "SIGNS": "NORMAL_WHEN_SNEAKING",
    "ITEM_FRAMES": "NORMAL_WHEN_SNEAKING",
    "GLOW_ITEM_FRAMES": "NORMAL_WHEN_SNEAKING",
    "PAINTINGS": "NORMAL_WHEN_SNEAKING"
  }
}
```

Each mode accepts one of:

- `NORMAL`                      - click-through disabled, vanilla behaviour.
- `NORMAL_WHEN_SNEAKING`        - click-through by default, normal while sneaking.
- `CLICKTHROUGH_WHEN_SNEAKING`  - normal by default, click-through while sneaking.

## Building

```
./gradlew build
```

The built jars will be in `build/libs/` under `fabric/`, `neoforge/` and
`quilt/`. Drop the one matching your server's loader into your server's `mods/`
folder alongside the compatible loader/API:

- `fabric/build/libs/clickthrough_server-fabric-*.jar` - needs Fabric Loader + Fabric API
- `neoforge/build/libs/clickthrough_server-neoforge-*.jar` - needs NeoForge
- `quilt/build/libs/clickthrough_server-quilt-*.jar` - needs Quilt Loader
