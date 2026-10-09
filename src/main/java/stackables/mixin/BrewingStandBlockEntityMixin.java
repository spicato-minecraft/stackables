package stackables.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

	@WrapOperation(
		method = "doBrew",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;mix(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	private static ItemStack mixSinglePotion(
		PotionBrewing brewing,
		ItemStack ingredient,
		ItemStack bottle,
		Operation<ItemStack> original,
		Level level,
		BlockPos pos,
		NonNullList<ItemStack> items
	) {
		int leftover = Math.max(0, bottle.getCount() - 1);
		ItemStack single = bottle.copy();
		single.setCount(1);
		ItemStack mixed = original.call(brewing, ingredient, single);
		if (leftover > 0 && !level.isClientSide()) {
			ItemStack remainder = bottle.copy();
			remainder.setCount(leftover);
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
		}
		return mixed;
	}
}
