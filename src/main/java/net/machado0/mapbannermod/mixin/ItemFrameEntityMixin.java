package net.machado0.mapbannermod.mixin;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void preventMapRotation(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemFrameEntity itemFrame = (ItemFrameEntity) (Object) this;
        ItemStack frameItem = itemFrame.getHeldItemStack();

        if (player.isSneaking() && frameItem.getItem() instanceof FilledMapItem) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}