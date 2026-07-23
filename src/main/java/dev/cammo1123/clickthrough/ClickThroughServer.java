package dev.cammo1123.clickthrough;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.slf4j.Logger;

public class ClickThroughServer implements ModInitializer {
	public static final String MOD_ID = "clickthrough-server";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static ClickThroughConfig config;

	@Override
	public void onInitialize() {
		config = ClickThroughConfig.loadOrCreate();

		UseBlockCallback.EVENT.register(ClickThroughServer::onUseBlock);
		UseEntityCallback.EVENT.register(ClickThroughServer::onUseEntity);
		ClickThroughCommand.register();

		LOGGER.info("ClickThrough Server initialized (signs={}, itemFrames={}, glowItemFrames={}, paintings={})",
				config.signs, config.itemFrames, config.glowItemFrames, config.paintings);
	}

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand,
			BlockHitResult hitResult) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = level.getBlockState(pos);

		// Only wall-mounted signs are physically flush against a block, so
		// only they make sense for click-through. Standing signs and ceiling
		// hanging signs don't sit against a wall and are left untouched.
		ClickThroughConfig.Mode mode;
		if (state.getBlock() instanceof WallSignBlock || state.getBlock() instanceof WallHangingSignBlock) {
			mode = config.signs;
		} else {
			return InteractionResult.PASS;
		}

		if (!mode.shouldClickThrough(player.isSecondaryUseActive())
				|| !PlayerPreferences.isEnabledFor(player.getUUID())) {
			return InteractionResult.PASS;
		}

		if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return InteractionResult.PASS;
		}

		Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		return openContainerBehind(serverPlayer, level, pos, facing);
	}

	private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity,
			EntityHitResult hitResult) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}

		ClickThroughConfig.Mode mode;
		if (entity instanceof GlowItemFrame) {
			mode = config.glowItemFrames;
		} else if (entity instanceof ItemFrame) {
			mode = config.itemFrames;
		} else if (entity instanceof Painting) {
			mode = config.paintings;
		} else {
			return InteractionResult.PASS;
		}

		if (!mode.shouldClickThrough(player.isSecondaryUseActive())
				|| !PlayerPreferences.isEnabledFor(player.getUUID())) {
			return InteractionResult.PASS;
		}

		if (!(entity instanceof HangingEntity hangingEntity)) {
			return InteractionResult.PASS;
		}

		Direction facing = hangingEntity.getDirection();
		return openContainerBehind(serverPlayer, level, hangingEntity.blockPosition(), facing);
	}

	/**
	 * @param frontPos the position of the sign/frame/painting itself (the space in
	 *                 front of the wall)
	 * @param facing   the direction the object faces, i.e. away from the wall it's
	 *                 mounted on
	 */
	private static InteractionResult openContainerBehind(ServerPlayer player, Level level, BlockPos frontPos,
			Direction facing) {
		BlockPos behindPos = frontPos.relative(facing.getOpposite());
		BlockState behindState = level.getBlockState(behindPos);
		MenuProvider menuProvider = behindState.getMenuProvider(level, behindPos);

		if (menuProvider == null) {
			return InteractionResult.PASS;
		}

		player.openMenu(menuProvider);
		return InteractionResult.SUCCESS;
	}
}
