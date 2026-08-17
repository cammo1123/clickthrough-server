package dev.cammo1123.clickthrough.quilt;

import dev.cammo1123.clickthrough.ClickThroughServer;
import net.fabricmc.api.ModInitializer;

@SuppressWarnings("deprecation")
public final class ClickThroughServerQuilt implements ModInitializer {
	public static ClickThroughServer clickThroughServer;

	@Override
	public void onInitialize() {
		clickThroughServer = ClickThroughServer.init(new QuiltPlatform());
	}
}
