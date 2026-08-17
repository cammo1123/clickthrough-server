package dev.cammo1123.clickthrough.neoforge;

import java.nio.file.Path;

import dev.cammo1123.clickthrough.Platform;
import net.neoforged.fml.loading.FMLPaths;

public final class NeoForgePlatform implements Platform {
	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
