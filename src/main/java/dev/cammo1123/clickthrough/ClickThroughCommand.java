package dev.cammo1123.clickthrough;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ClickThroughCommand {
	private ClickThroughCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, environment) -> registerCommand(dispatcher));
	}

	private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("clickthrough")
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("enable")
						.executes(ctx -> setEnabled(ctx.getSource(), true)))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("disable")
						.executes(ctx -> setEnabled(ctx.getSource(), false))));
	}

	private static int setEnabled(CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		PlayerPreferences.setEnabled(player.getUUID(), enabled);

		source.sendSuccess(() -> Component.literal(enabled
				? "Click-through enabled. Right-clicking signs, item frames, and paintings will open containers behind them again."
				: "Click-through disabled. Signs, item frames, and paintings will behave normally for you."), false);

		return 1;
	}
}
