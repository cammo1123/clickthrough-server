package dev.cammo1123.clickthrough;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public enum ClickType {
	SIGNS(Mode.NORMAL_WHEN_SNEAKING), ITEM_FRAMES(Mode.NORMAL_WHEN_SNEAKING), GLOW_ITEM_FRAMES(Mode.NORMAL_WHEN_SNEAKING), PAINTINGS(Mode.NORMAL_WHEN_SNEAKING);

	private final Mode defaultMode;

	ClickType(Mode defaultMode) {
		this.defaultMode = defaultMode;
	}

	public Mode defaultMode() {
		return defaultMode;
	}

	public static Optional<ClickType> ofBlock(BlockState state) {
		if (state.getBlock() instanceof WallSignBlock || state.getBlock() instanceof WallHangingSignBlock) {
			return Optional.of(SIGNS);
		}
		return Optional.empty();
	}

	public static Optional<ClickType> ofEntity(Entity entity) {
		if (entity instanceof GlowItemFrame) {
			return Optional.of(GLOW_ITEM_FRAMES);
		}
		if (entity instanceof ItemFrame) {
			return Optional.of(ITEM_FRAMES);
		}
		if (entity instanceof Painting) {
			return Optional.of(PAINTINGS);
		}
		return Optional.empty();
	}
}
