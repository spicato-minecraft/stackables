package stackables.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stackables.AnvilStackOperations;

/**
 * Priority 400 so cost-override mixins (default 1000) run first and we scale the unit cost last.
 */
@Mixin(value = AnvilMenu.class, priority = 400)
public class AnvilMenuMixin {

	@Shadow
	@Final
	private DataSlot cost;

	@Unique
	private int stackables$operations = 1;

	@Unique
	private boolean stackables$taking;

	@WrapOperation(
		method = "createResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
			ordinal = 0
		)
	)
	private ItemStack copyUnitLeftItem(ItemStack left, Operation<ItemStack> original) {
		ItemStack copy = original.call(left);
		AnvilMenu self = (AnvilMenu) (Object) this;
		ItemStack right = self.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
		// Collapse to 1+1 only for book combines. Rename (and other 1-slot ops) must keep
		// vanilla stack size — otherwise a stack of potions/books becomes one renamed item.
		if (left.is(Items.ENCHANTED_BOOK) && right.is(Items.ENCHANTED_BOOK)) {
			copy.setCount(1);
		}
		return copy;
	}

	/**
	 * Vanilla forces cost to 40 when the left stack count is greater than 1.
	 * Keep the 1+1 unit cost; {@link #applyStackOperations} multiplies afterward.
	 */
	@WrapOperation(
		method = "createResult",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;getCount()I",
			ordinal = 1
		)
	)
	private int ignoreLeftStackCountForVanillaPenalty(ItemStack stack, Operation<Integer> original) {
		return 1;
	}

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void skipRebuildDuringTake(CallbackInfo ci) {
		if (this.stackables$taking) {
			ci.cancel();
		}
	}

	@Inject(method = "createResult", at = @At("TAIL"))
	private void applyStackOperations(CallbackInfo ci) {
		AnvilMenu self = (AnvilMenu) (Object) this;
		ItemStack left = self.getSlot(AnvilMenu.INPUT_SLOT).getItem();
		ItemStack right = self.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
		ItemStack result = self.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		if (result.isEmpty()) {
			this.stackables$operations = 1;
			return;
		}
		if (right.isEmpty()) {
			// Whole-stack rename: vanilla charges 1 level for the stack, then clears the left slot.
			this.stackables$operations = Math.max(1, left.getCount());
			return;
		}
		int operations = AnvilStackOperations.operations(left, right, result);
		this.stackables$operations = Math.max(1, operations);
		if (left.is(Items.ENCHANTED_BOOK) && right.is(Items.ENCHANTED_BOOK) && operations > 0) {
			result.setCount(Math.min(operations, result.getMaxStackSize()));
			this.cost.set(AnvilStackOperations.scaleCost(self.getCost(), operations));
		}
	}

	@Inject(method = "onTake", at = @At("HEAD"))
	private void beginTake(Player player, ItemStack taken, CallbackInfo ci) {
		this.stackables$taking = true;
	}

	@Inject(method = "onTake", at = @At("RETURN"))
	private void endTake(Player player, ItemStack taken, CallbackInfo ci) {
		this.stackables$taking = false;
	}

	@WrapOperation(
		method = "onTake",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"
		)
	)
	private void consumeStackedEnchantedBooks(
		Container container,
		int slot,
		ItemStack stack,
		Operation<Void> original
	) {
		if (stack.isEmpty() && (slot == AnvilMenu.INPUT_SLOT || slot == AnvilMenu.ADDITIONAL_SLOT)) {
			ItemStack current = container.getItem(slot);
			int consume = this.stackables$operations;
			if (current.is(Items.ENCHANTED_BOOK) && current.getCount() > consume) {
				ItemStack remaining = current.copy();
				remaining.shrink(consume);
				original.call(container, slot, remaining);
				return;
			}
		}
		original.call(container, slot, stack);
	}
}
