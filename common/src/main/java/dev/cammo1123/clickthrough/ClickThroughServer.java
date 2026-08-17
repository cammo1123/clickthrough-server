package dev.cammo1123.clickthrough;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

public final class ClickThroughServer {
	public static final Logger LOGGER = LogUtils.getLogger();

	private final ClickThroughConfig config;
	private final PlayerPreferences preferences;

	private ClickThroughServer(Path configDir) {
		this.config = ClickThroughConfig.load(configDir);
		this.preferences = new PlayerPreferences(configDir);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ClickThrough Server initialized ({})", listModes());
		}
	}

	public static ClickThroughServer init(Platform platform) {
		return new ClickThroughServer(platform.getConfigDir());
	}

	private String listModes() {
		StringBuilder modes = new StringBuilder();
		for (ClickType type : ClickType.values()) {
			if (!modes.isEmpty()) {
				modes.append(", ");
			}

			modes.append(type.name().toLowerCase(Locale.ROOT));
			modes.append('=');
			modes.append(config.modeFor(type));
		}
		return modes.toString();
	}

	private InteractionResult handle(Player player, Level level, Optional<ClickTarget> target) {
		ServerPlayer serverPlayer = serverPlayerOrNull(player, level);
		if (serverPlayer == null || target.isEmpty()) {
			return InteractionResult.PASS;
		}

		ClickTarget clickTarget = target.get();
		if (!config.modeFor(clickTarget.type()).shouldClickThrough(player.isSecondaryUseActive()) || !preferences.isEnabledFor(player.getUUID())) {
			return InteractionResult.PASS;
		}

		return openContainerBehind(serverPlayer, level, clickTarget.frontPos(), clickTarget.facing());
	}

	public InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();
		return handle(player, level, clickTarget(pos, level.getBlockState(pos)));
	}

	public InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		return handle(player, level, clickTarget(entity));
	}

	public void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
		ClickThroughCommand.register(dispatcher, preferences);
	}

	@Nullable
	private static ServerPlayer serverPlayerOrNull(Player player, Level level) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || serverPlayer.isSpectator()) {
			return null;
		}
		return serverPlayer;
	}

	/**
	 * Maps a clicked block
	 */
	private Optional<ClickTarget> clickTarget(BlockPos pos, BlockState state) {
		if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return Optional.empty();
		}

		return ClickType.ofBlock(state).map(type -> new ClickTarget(type, state.getValue(BlockStateProperties.HORIZONTAL_FACING), pos));
	}

	/**
	 * Maps a clicked entity
	 */
	private Optional<ClickTarget> clickTarget(Entity entity) {
		if (!(entity instanceof HangingEntity hangingEntity)) {
			return Optional.empty();
		}

		return ClickType.ofEntity(entity).map(type -> new ClickTarget(type, hangingEntity.getDirection(), hangingEntity.blockPosition()));
	}

	private static InteractionResult openContainerBehind(ServerPlayer player, Level level, BlockPos frontPos, Direction facing) {
		BlockPos behindPos = frontPos.relative(facing.getOpposite());
		BlockState behindState = level.getBlockState(behindPos);
		MenuProvider menuProvider = behindState.getMenuProvider(level, behindPos);

		if (menuProvider == null) {
			return InteractionResult.PASS;
		}

		player.openMenu(menuProvider);
		return InteractionResult.SUCCESS;
	}

	private record ClickTarget(ClickType type, Direction facing, BlockPos frontPos) {
	}
}
