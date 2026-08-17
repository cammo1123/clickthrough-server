package dev.cammo1123.clickthrough;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class ClickThroughCommand {
	final static String ENABLED_STRING = "Click-through enabled. Right-clicking signs, item frames, and paintings will open containers behind them again.";
	final static String DISABLED_STRING = "Click-through disabled. Signs, item frames, and paintings will behave normally for you.";

	private ClickThroughCommand() {
	}

	static void register(CommandDispatcher<CommandSourceStack> dispatcher, PlayerPreferences preferences) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("clickthrough")
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("enable").executes(ctx -> setEnabled(preferences, ctx.getSource(), true)))
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("disable").executes(ctx -> setEnabled(preferences, ctx.getSource(), false))));
	}

	private static int setEnabled(PlayerPreferences preferences, CommandSourceStack source, boolean enabled) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();

		preferences.setEnabled(player.getUUID(), enabled);
		source.sendSuccess(() -> Component.literal(enabled ? ENABLED_STRING : DISABLED_STRING), false);

		return 1;
	}
}
