package dev.cammo1123.clickthrough.quilt.mixin;

import dev.cammo1123.clickthrough.ClickThroughServer;
import dev.cammo1123.clickthrough.quilt.ClickThroughServerQuilt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.level.ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
	private void clickthrough$onUseBlock(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
		ClickThroughServer core = ClickThroughServerQuilt.clickThroughServer;
		if (core == null) {
			return;
		}

		InteractionResult result = core.onUseBlock(player, level, hand, hitResult);
		if (result != InteractionResult.PASS) {
			cir.setReturnValue(result);
		}
	}
}
