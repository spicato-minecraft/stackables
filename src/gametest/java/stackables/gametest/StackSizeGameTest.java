package stackables.gametest;

import stackables.StackableItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class StackSizeGameTest {

	@GameTest(maxTicks = 100)
	public void targetedItemsStackToSixteen(GameTestHelper context) {
		for (var item : StackableItems.TARGETS) {
			ItemStack stack = new ItemStack(item);
			context.assertValueEqual(
				StackableItems.MAX_STACK_SIZE,
				stack.getMaxStackSize(),
				item.toString() + " max stack"
			);
			stack.setCount(StackableItems.MAX_STACK_SIZE);
			context.assertValueEqual(StackableItems.MAX_STACK_SIZE, stack.getCount(), item.toString() + " count");
		}
		context.assertValueEqual(1, new ItemStack(Items.TOTEM_OF_UNDYING).getMaxStackSize(), "totem unchanged");
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void mismatchedPotionsDoNotShareIdentity(GameTestHelper context) {
		ItemStack healing = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		ItemStack swiftness = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
		context.assertFalse(ItemStack.isSameItemSameComponents(healing, swiftness), "different potions must not merge");
		ItemStack healingCopy = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
		context.assertTrue(ItemStack.isSameItemSameComponents(healing, healingCopy), "identical potions must match");
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void emptyEnchantedBooksShareIdentity(GameTestHelper context) {
		ItemStack first = new ItemStack(Items.ENCHANTED_BOOK);
		ItemStack second = new ItemStack(Items.ENCHANTED_BOOK);
		context.assertTrue(ItemStack.isSameItemSameComponents(first, second), "blank books should match");
		ItemEnchantments stored = first.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		context.assertTrue(stored.isEmpty() || EnchantmentHelper.getEnchantmentsForCrafting(first).isEmpty(), "default book has no stored enchants");
		context.succeed();
	}
}
