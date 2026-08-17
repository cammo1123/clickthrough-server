package dev.cammo1123.clickthrough.neoforge;

import dev.cammo1123.clickthrough.ClickThroughServer;
import dev.cammo1123.clickthrough.Constants;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.function.Consumer;

@Mod(Constants.MOD_ID)
public final class ClickThroughServerNeoForge {
	private final ClickThroughServer clickThroughServer;

	public ClickThroughServerNeoForge(IEventBus modEventBus) {
		clickThroughServer = ClickThroughServer.init(new NeoForgePlatform());

		NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
		NeoForge.EVENT_BUS.addListener(this::onRightClickEntity);
		NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
	}

	private static void handleAndCancel(InteractionResult result, Consumer<InteractionResult> cancel) {
		if (result.consumesAction()) {
			cancel.accept(result);
		}
	}

	private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		InteractionResult result = clickThroughServer.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
		handleAndCancel(result, r -> {
			event.setCanceled(true);
			event.setCancellationResult(r);
		});
	}

	private void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		InteractionResult result = clickThroughServer.onUseEntity(event.getEntity(), event.getLevel(), event.getHand(), event.getTarget());
		handleAndCancel(result, r -> {
			event.setCanceled(true);
			event.setCancellationResult(r);
		});
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		clickThroughServer.registerCommand(event.getDispatcher());
	}
}
