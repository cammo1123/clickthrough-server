package dev.cammo1123.clickthrough.quilt.mixin;

import dev.cammo1123.clickthrough.ClickThroughServer;
import dev.cammo1123.clickthrough.quilt.ClickThroughServerQuilt;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void clickthrough$onRegisterCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
		ClickThroughServer clickThroughServer = ClickThroughServerQuilt.clickThroughServer;
		if (clickThroughServer != null) {
			Commands self = (Commands) (Object) this;
			clickThroughServer.registerCommand(self.getDispatcher());
		}
	}
}
