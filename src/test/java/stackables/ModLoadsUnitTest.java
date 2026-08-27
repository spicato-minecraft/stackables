package stackables;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModLoadsUnitTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void modMainClassLoads() {
		assertNotNull(Stackables.MOD_ID);
	}

	@Test
	void targetsPotionsAndEnchantedBooksOnly() {
		assertEquals(16, StackableItems.MAX_STACK_SIZE);
		assertEquals(4, StackableItems.TARGETS.size());
		assertTrue(StackableItems.isTarget(Items.POTION));
		assertTrue(StackableItems.isTarget(Items.SPLASH_POTION));
		assertTrue(StackableItems.isTarget(Items.LINGERING_POTION));
		assertTrue(StackableItems.isTarget(Items.ENCHANTED_BOOK));
		assertFalse(StackableItems.isTarget(Items.TOTEM_OF_UNDYING));
		assertFalse(StackableItems.isTarget(Items.TIPPED_ARROW));
	}
}
