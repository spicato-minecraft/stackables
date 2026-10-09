package stackables;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Flat anvil stack multiplier for book-on-book combines.
 *
 * <p>Cost mods should compute the <strong>1+1 unit cost</strong> (one left item plus one
 * right item). This class supplies the operation count and {@link #scaleCost(int, int)}.
 * {@code stackables} applies the scale at the end of {@code AnvilMenu.createResult}
 * (mixin priority 400, after typical cost overrides).
 */
public final class AnvilStackOperations {
	private AnvilStackOperations() {
	}

	/**
	 * How many successful 1+1 combines this anvil take represents.
	 * Book + book uses {@code min(left, right)} count. Anything else is 1.
	 */
	public static int operations(ItemStack left, ItemStack right, ItemStack result) {
		if (result.isEmpty() || left.isEmpty()) {
			return 0;
		}
		if (right.isEmpty()) {
			return 1;
		}
		if (left.is(Items.ENCHANTED_BOOK) && right.is(Items.ENCHANTED_BOOK)) {
			return Math.min(left.getCount(), right.getCount());
		}
		return 1;
	}

	public static int scaleCost(int unitCost, int operations) {
		if (unitCost <= 0 || operations <= 1) {
			return unitCost;
		}
		long scaled = (long) unitCost * (long) operations;
		return (int) Math.min(scaled, Integer.MAX_VALUE);
	}
}
