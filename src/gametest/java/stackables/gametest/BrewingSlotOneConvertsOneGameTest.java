package stackables.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.phys.AABB;

public class BrewingSlotOneConvertsOneGameTest {

	@GameTest(maxTicks = 500)
	public void brewingSlotOneConvertsOne(GameTestHelper context) {
		ConsumeOneGameTestHelper.floor(context);
		BlockPos rel = new BlockPos(1, 1, 1);
		context.setBlock(rel, Blocks.BREWING_STAND.defaultBlockState());
		BrewingStandBlockEntity stand = context.getBlockEntity(rel, BrewingStandBlockEntity.class);

		ItemStack waters = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		waters.setCount(4);
		stand.setItem(1, waters);
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

		context.assertTrue(stand.getItem(0).isEmpty(), "bottle slot 0 should stay empty");
		ItemStack bottled = stand.getItem(1);
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
