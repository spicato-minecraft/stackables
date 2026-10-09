package stackables;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Shared consume logic for menus that still assume unstackable inputs.
 */
public final class ConsumeOne {
	private ConsumeOne() {
	}

	/**
	 * If {@code slot} holds more than {@code amount} items, shrink by {@code amount} in place.
	 *
	 * @return true when the caller should skip replacing the slot with {@code EMPTY}
	 */
	public static boolean shrinkInsteadOfClear(Container container, int slot, int amount) {
		ItemStack current = container.getItem(slot);
		int consume = Math.max(1, amount);
		if (current.getCount() <= consume) {
			return false;
		}
		current.shrink(consume);
		return true;
	}

	public static boolean shrinkOneInsteadOfClear(Container container, int slot) {
		return shrinkInsteadOfClear(container, slot, 1);
	}
}
