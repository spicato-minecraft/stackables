package stackables;

import java.util.List;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class StackableItems {
	public static final int MAX_STACK_SIZE = 16;

	public static final List<Item> TARGETS = List.of(
		Items.POTION,
		Items.SPLASH_POTION,
		Items.LINGERING_POTION,
		Items.ENCHANTED_BOOK
	);

	private StackableItems() {
	}

	public static void register() {
		DefaultItemComponentEvents.MODIFY.register(context -> {
			for (Item item : TARGETS) {
				context.modify(item, builder -> builder.set(DataComponents.MAX_STACK_SIZE, MAX_STACK_SIZE));
			}
		});
	}

	public static boolean isTarget(Item item) {
		return TARGETS.contains(item);
	}
}
