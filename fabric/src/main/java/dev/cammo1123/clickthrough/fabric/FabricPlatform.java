package dev.cammo1123.clickthrough.fabric;

import dev.cammo1123.clickthrough.Platform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricPlatform implements Platform {
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
