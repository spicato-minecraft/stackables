package stackables.gametest;

import stackables.StackableItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class DifferentBooksDoNotMergeGameTest {

	@GameTest(maxTicks = 200)
	public void differentBooksDoNotMerge(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 0);
		ItemStack sharpness = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.SHARPNESS, 1, 8);
		ItemStack efficiency = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 8);

		context.assertFalse(
			ItemStack.isSameItemSameComponents(sharpness, efficiency),
			"sharpness and efficiency books must not share identity"
		);
		context.assertValueEqual(StackableItems.MAX_STACK_SIZE, 16, "enchanted books stack to 16");
		context.assertValueEqual(16, sharpness.getMaxStackSize(), "sharpness max stack");
		context.assertValueEqual(16, efficiency.getMaxStackSize(), "efficiency max stack");

		Inventory inventory = player.getInventory();
		inventory.setItem(0, sharpness.copy());
		inventory.setItem(1, efficiency.copy());

		InventoryMenu menu = player.inventoryMenu;
		int sharpnessSlot = menuSlot(menu, inventory, 0);
		int efficiencySlot = menuSlot(menu, inventory, 1);
		menu.clicked(efficiencySlot, 0, ContainerInput.PICKUP, player);
		menu.clicked(sharpnessSlot, 0, ContainerInput.PICKUP, player);

		context.assertValueEqual(8, totalMatching(player, sharpness), "sharpness books stay their own stack");
		context.assertValueEqual(8, totalMatching(player, efficiency), "efficiency books stay their own stack");
		context.assertValueEqual(1, stackCount(player, sharpness), "sharpness remains one stack");
		context.assertValueEqual(1, stackCount(player, efficiency), "efficiency remains one stack");
		assertMaxStack(context, player, sharpness);
		assertMaxStack(context, player, efficiency);
		context.succeed();
	}

	private static int menuSlot(InventoryMenu menu, Inventory inventory, int inventoryIndex) {
		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (slot.container == inventory && slot.getContainerSlot() == inventoryIndex) {
				return i;
			}
		}
		throw new IllegalStateException("no menu slot for inventory index " + inventoryIndex);
	}

	private static int totalMatching(ServerPlayer player, ItemStack template) {
		int total = 0;
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (ItemStack.isSameItemSameComponents(stack, template)) {
				total += stack.getCount();
			}
		}
		ItemStack carried = player.inventoryMenu.getCarried();
		if (ItemStack.isSameItemSameComponents(carried, template)) {
			total += carried.getCount();
		}
		return total;
	}

	private static int stackCount(ServerPlayer player, ItemStack template) {
		int stacks = 0;
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template)) {
				stacks++;
			}
		}
		ItemStack carried = player.inventoryMenu.getCarried();
		if (!carried.isEmpty() && ItemStack.isSameItemSameComponents(carried, template)) {
			stacks++;
		}
		return stacks;
	}

	private static void assertMaxStack(GameTestHelper context, ServerPlayer player, ItemStack template) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (ItemStack.isSameItemSameComponents(stack, template)) {
				context.assertValueEqual(16, stack.getMaxStackSize(), "max stack still 16");
			}
		}
		ItemStack carried = player.inventoryMenu.getCarried();
		if (ItemStack.isSameItemSameComponents(carried, template)) {
			context.assertValueEqual(16, carried.getMaxStackSize(), "max stack still 16");
		}
	}
}
