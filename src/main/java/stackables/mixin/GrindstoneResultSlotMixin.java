package stackables.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class GrindstoneResultSlotMixin {

	@ModifyReturnValue(method = "getExperienceFromItem", at = @At("RETURN"))
	private int scaleExperienceByStackCount(int xp, ItemStack stack) {
		if (xp <= 0 || stack.getCount() <= 1) {
			return xp;
		}
		long scaled = (long) xp * (long) stack.getCount();
		return (int) Math.min(scaled, Integer.MAX_VALUE);
	}
}
