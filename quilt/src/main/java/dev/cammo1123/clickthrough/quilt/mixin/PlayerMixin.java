package dev.cammo1123.clickthrough.quilt.mixin;

import dev.cammo1123.clickthrough.ClickThroughServer;
import dev.cammo1123.clickthrough.quilt.ClickThroughServerQuilt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "interactOn", at = @At(value = "HEAD"), cancellable = true)
	private void clickthrough$onUseEntity(Entity entity, InteractionHand hand, Vec3 hitVec, CallbackInfoReturnable<InteractionResult> cir) {
		ClickThroughServer core = ClickThroughServerQuilt.clickThroughServer;
		if (core == null) {
			return;
		}
		Player self = (Player) (Object) this;
		InteractionResult result = core.onUseEntity(self, self.level(), hand, entity);
		if (result != InteractionResult.PASS) {
			cir.setReturnValue(result);
		}
	}
}
