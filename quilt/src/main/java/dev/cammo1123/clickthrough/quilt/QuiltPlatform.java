package dev.cammo1123.clickthrough.quilt;

import dev.cammo1123.clickthrough.Platform;
import org.quiltmc.loader.api.QuiltLoader;

import java.nio.file.Path;

public class QuiltPlatform implements Platform {

	@Override
	public Path getConfigDir() {
		return QuiltLoader.getConfigDir();
	}
}
