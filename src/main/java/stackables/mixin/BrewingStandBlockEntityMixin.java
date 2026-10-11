package stackables.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

	@WrapOperation(
		method = "doBrew",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/BrewingRecipe;assemble(Lnet/minecraft/world/item/crafting/BrewingInput;)Lnet/minecraft/world/item/ItemStack;"
		)
	)
	private static ItemStack mixSinglePotion(
		BrewingRecipe recipe,
		BrewingInput input,
		Operation<ItemStack> original,
		ServerLevel level,
		BlockPos pos,
		BrewingStandBlockEntity stand
	) {
		ItemStack bottle = input.input();
		int leftover = Math.max(0, bottle.getCount() - 1);
		ItemStack mixed = original.call(recipe, input);
		if (leftover > 0) {
			ItemStack remainder = bottle.copy();
			remainder.setCount(leftover);
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
		}
		return mixed;
	}
}
