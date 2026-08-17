package dev.cammo1123.clickthrough.fabric;

import dev.cammo1123.clickthrough.ClickThroughServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class ClickThroughServerFabric implements ModInitializer {
	private ClickThroughServer clickThroughServer;

	@Override
	public void onInitialize() {
		clickThroughServer = ClickThroughServer.init(new FabricPlatform());

		UseBlockCallback.EVENT.register(clickThroughServer::onUseBlock);
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> clickThroughServer.onUseEntity(player, level, hand, entity));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> clickThroughServer.registerCommand(dispatcher));
	}
}
