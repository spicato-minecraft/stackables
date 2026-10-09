package stackables;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnvilStackOperationsTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void scaleCostIsFlatMultiplier() {
		assertEquals(16, AnvilStackOperations.scaleCost(2, 8));
		assertEquals(2, AnvilStackOperations.scaleCost(2, 1));
		assertEquals(6, AnvilStackOperations.scaleCost(2, 3));
	}

	@Test
	void emptyResultIsZeroOperations() {
		assertEquals(0, AnvilStackOperations.operations(
			new ItemStack(Items.ENCHANTED_BOOK),
			new ItemStack(Items.ENCHANTED_BOOK),
			ItemStack.EMPTY
		));
	}
}
