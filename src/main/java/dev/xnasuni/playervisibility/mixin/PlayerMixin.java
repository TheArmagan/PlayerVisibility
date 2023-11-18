package dev.xnasuni.playervisibility.mixin;

import dev.xnasuni.playervisibility.PlayerVisibility;
import dev.xnasuni.playervisibility.types.FilterType;
import dev.xnasuni.playervisibility.util.ArrayListUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerMixin {

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void InjectRender(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {

        if (PlayerVisibility.isFilterEnabled()) {
            boolean shouldShowPlayer;
            if (abstractClientPlayerEntity.getName().getString().equalsIgnoreCase(PlayerVisibility.minecraftClient.player.getName().getString())) {
                shouldShowPlayer = true;
            } else {
                boolean IsFiltered = ArrayListUtil.ContainsLowercase(PlayerVisibility.getFilteredPlayers(), abstractClientPlayerEntity.getName().getString());
                if (PlayerVisibility.getFilterType() == FilterType.WHITELIST) {
                    shouldShowPlayer = IsFiltered;
                } else {
                    shouldShowPlayer = !IsFiltered;
                }
            }

            if (!shouldShowPlayer) {
                ci.cancel();
            }
        }

    }

}
