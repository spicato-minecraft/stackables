package stackables.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public class ConsumeOneGameTest {

	@GameTest(maxTicks = 200)
	public void anvilTakesOneBookFromStack(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);

		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack books = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.SHARPNESS, 1, 4);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(sword);
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(books);

		context.assertFalse(menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().isEmpty(), "anvil should show a result");
		ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		context.assertValueEqual(1, result.getCount(), "gear + book is one combine");
		menu.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);

		ItemStack remaining = menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
		context.assertValueEqual(Items.ENCHANTED_BOOK, remaining.getItem(), "book slot");
		context.assertValueEqual(3, remaining.getCount(), "anvil should consume one book onto gear");
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void anvilBookCombineScalesCostAndConsumesBothStacks(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		AnvilMenu unitMenu = ConsumeOneGameTestHelper.openAnvil(context, player);
		ItemStack unitLeft = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 1);
		ItemStack unitRight = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 1);
		unitMenu.getSlot(AnvilMenu.INPUT_SLOT).set(unitLeft);
		unitMenu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(unitRight);
		int unitCost = unitMenu.getCost();
		context.assertTrue(unitCost > 0, "1+1 efficiency books should have a cost");

		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);
		ItemStack left = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 8);
		ItemStack right = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 8);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(left);
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(right);

		ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		context.assertFalse(result.isEmpty(), "8+8 efficiency I should combine");
		context.assertValueEqual(8, result.getCount(), "result count should match successful combines");
		context.assertValueEqual(unitCost * 8, menu.getCost(), "cost should be unit cost times combines");

		menu.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);
		context.assertTrue(menu.getSlot(AnvilMenu.INPUT_SLOT).getItem().isEmpty(), "left books should all be consumed");
		context.assertTrue(menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem().isEmpty(), "right books should all be consumed");
		context.assertValueEqual(8, menu.getCarried().getCount(), "player should receive 8 combined books");
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void anvilFourPlusFourConsumesEntireStacks(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(
			ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 2, 4)
		);
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(
			ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 2, 4)
		);

		ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		context.assertValueEqual(4, result.getCount(), "4+4 efficiency II should make 4 results");
		menu.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);

		context.assertTrue(menu.getSlot(AnvilMenu.INPUT_SLOT).getItem().isEmpty(), "left stack fully consumed");
		context.assertTrue(menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem().isEmpty(), "right stack fully consumed");
		context.assertValueEqual(4, menu.getCarried().getCount(), "player receives 4 combined books");
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void anvilBookCombineLeavesUnequalRemainder(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(
			ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 8)
		);
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(
			ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 3)
		);

		context.assertValueEqual(3, menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().getCount(), "ops = min(8,3)");
		menu.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);

		ItemStack leftRemaining = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
		context.assertValueEqual(5, leftRemaining.getCount(), "left should keep the extra 5");
		context.assertTrue(menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem().isEmpty(), "right stack fully consumed");
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void anvilRenamesEntirePotionStack(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);
		ItemStack potions = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		potions.setCount(16);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(potions);
		context.assertTrue(menu.setItemName("Labeled"), "anvil should accept a custom name");

		ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
		context.assertFalse(result.isEmpty(), "vanilla allows renaming potions");
		context.assertValueEqual(16, result.getCount(), "rename should keep the whole stack");
		context.assertValueEqual(1, menu.getCost(), "rename cost is 1 for the stack, not per bottle");
		menu.clicked(AnvilMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);
		context.assertTrue(menu.getSlot(AnvilMenu.INPUT_SLOT).getItem().isEmpty(), "rename consumes the whole stack");
		context.assertValueEqual(16, menu.getCarried().getCount(), "player receives the named stack");
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void anvilRejectsIncompatibleBookOnGearInSurvival(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 50);
		context.assertFalse(player.hasInfiniteMaterials(), "survival must not bypass enchant compatibility");
		AnvilMenu menu = ConsumeOneGameTestHelper.openAnvil(context, player);
		menu.getSlot(AnvilMenu.INPUT_SLOT).set(new ItemStack(Items.DIAMOND_SWORD));
		menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).set(
			ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.EFFICIENCY, 1, 1)
		);
		context.assertTrue(
			menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().isEmpty(),
			"efficiency cannot go on a sword in survival"
		);
		context.succeed();
	}

	@GameTest(maxTicks = 200)
	public void grindstoneDisenchantsWholeBookStack(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 0);
		GrindstoneMenu menu = ConsumeOneGameTestHelper.openGrindstone(context, player);

		ItemStack books = ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.SHARPNESS, 1, 4);
		menu.getSlot(0).set(books);

		ItemStack result = menu.getSlot(2).getItem();
		context.assertFalse(result.isEmpty(), "grindstone should show a result");
		context.assertValueEqual(Items.BOOK, result.getItem(), "disenchanted books become books");
		context.assertValueEqual(4, result.getCount(), "result count should match the input stack");
		menu.clicked(2, 0, ContainerInput.PICKUP, player);

		context.assertTrue(menu.getSlot(0).getItem().isEmpty(), "grindstone should consume the whole stack");
		context.assertValueEqual(Items.BOOK, menu.getCarried().getItem(), "output item");
		context.assertValueEqual(4, menu.getCarried().getCount(), "player receives the full stack of books");
		context.succeed();
	}

	@GameTest(maxTicks = 500)
	public void brewingConvertsOnePotionFromStack(GameTestHelper context) {
		ConsumeOneGameTestHelper.floor(context);
		BlockPos rel = new BlockPos(1, 1, 1);
		context.setBlock(rel, Blocks.BREWING_STAND.defaultBlockState());
		BrewingStandBlockEntity stand = context.getBlockEntity(rel, BrewingStandBlockEntity.class);

		ItemStack waters = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		waters.setCount(4);
		stand.setItem(0, waters);
		stand.setItem(3, new ItemStack(Items.NETHER_WART, 2));
		stand.setItem(4, new ItemStack(Items.BLAZE_POWDER, 1));

		BlockPos abs = context.absolutePos(rel);
		for (int i = 0; i < 420; i++) {
			BrewingStandBlockEntity.serverTick(
				context.getLevel(),
				abs,
				context.getBlockState(rel),
				stand
			);
		}

		ItemStack bottled = stand.getItem(0);
		context.assertTrue(
			PotionContents.createItemStack(Items.POTION, Potions.AWKWARD).getItem() == bottled.getItem(),
			"bottle slot should still be a potion"
		);
		context.assertValueEqual(1, bottled.getCount(), "brew should replace one potion in the slot");
		context.assertTrue(
			ItemStack.isSameItemSameComponents(
				bottled,
				PotionContents.createItemStack(Items.POTION, Potions.AWKWARD)
			),
			"brewed potion should be awkward"
		);
		context.assertValueEqual(1, stand.getItem(3).getCount(), "one nether wart consumed");

		int leftoverWater = context.getLevel()
			.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(2))
			.stream()
			.mapToInt(entity -> {
				ItemStack stack = entity.getItem();
				if (stack.is(Items.POTION)
					&& ItemStack.isSameItemSameComponents(stack, PotionContents.createItemStack(Items.POTION, Potions.WATER))) {
					return stack.getCount();
				}
				return 0;
			})
			.sum();
		context.assertValueEqual(3, leftoverWater, "unbrewed water potions should drop");
		context.succeed();
	}
}
