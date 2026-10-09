package stackables;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumeOneUnitTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void shrinksByRequestedAmount() {
		SimpleContainer container = new SimpleContainer(1);
		container.setItem(0, new ItemStack(Items.DIAMOND, 4));
		assertTrue(ConsumeOne.shrinkInsteadOfClear(container, 0, 3));
		assertEquals(1, container.getItem(0).getCount());
	}

	@Test
	void doesNotHandleLastItem() {
		SimpleContainer container = new SimpleContainer(1);
		container.setItem(0, new ItemStack(Items.DIAMOND, 1));
		assertFalse(ConsumeOne.shrinkInsteadOfClear(container, 0, 1));
		assertEquals(1, container.getItem(0).getCount());
	}
}
