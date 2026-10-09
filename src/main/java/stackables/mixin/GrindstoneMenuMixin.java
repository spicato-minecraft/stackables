package stackables.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {

	/**
	 * Vanilla rejects any input with count &gt; 1. Allow a stacked book (or tool) in a
	 * <em>single</em> slot so the whole stack can be disenchanted. Two filled slots still
	 * use real counts, so stacked merge-grind stays vanilla-illegal.
	 */
	@WrapOperation(
		method = "computeResult",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I")
	)
	private int allowStackedSingleInput(
		ItemStack stack,
		Operation<Integer> original,
		ItemStack left,
		ItemStack right
	) {
		if (left.isEmpty() != right.isEmpty()) {
			return 1;
		}
		return original.call(stack);
	}
}
