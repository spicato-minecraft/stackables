package stackables.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;

public class GrindstoneXpScalesWithCountGameTest {

	@GameTest(maxTicks = 200)
	public void grindstoneXpScalesWithCount(GameTestHelper context) {
		ServerPlayer player = ConsumeOneGameTestHelper.createPlayer(context.getLevel(), 0);
		int unitXp = enchantmentMinCost(context, Enchantments.FIRE_ASPECT, 1);
		// Grindstone awards ceil(raw / 2) plus a random roll. Fire Aspect I has an even min
		// cost, so with the roll pinned at 0 a stack of 4 awards exactly four times one book.
		context.assertTrue(unitXp > 0 && unitXp % 2 == 0, "unit enchantment xp must be a positive even number");

		int oneBook = experienceFromGrind(context, player, 1, unitXp);
		int fourBooks = experienceFromGrind(context, player, 4, unitXp * 4L);
		context.assertTrue(oneBook > 0, "one book should award experience");
		context.assertValueEqual(oneBook * 4, fourBooks, "experience awarded is 4 times one book");
		context.succeed();
	}

	private static int experienceFromGrind(GameTestHelper context, ServerPlayer player, int count, long rawXp) {
		GrindstoneMenu menu = ConsumeOneGameTestHelper.openGrindstone(context, player);
		menu.getSlot(0).set(ConsumeOneGameTestHelper.enchantedBook(context, Enchantments.FIRE_ASPECT, 1, count));

		var result = menu.getSlot(2).getItem();
		context.assertValueEqual(Items.BOOK, result.getItem(), "disenchanted books become books");
		context.assertValueEqual(count, result.getCount(), "result count should match the input stack");

		BlockPos absolute = context.absolutePos(new BlockPos(1, 1, 1));
		discardOrbs(context.getLevel(), absolute);
		int half = (int) Math.ceil(rawXp / 2.0D);
		context.getLevel().getRandom().setSeed(seedForZeroRoll(half));
		menu.clicked(2, 0, ContainerInput.PICKUP, player);

		context.assertTrue(menu.getSlot(0).getItem().isEmpty(), "grindstone should consume the whole stack");
		context.assertValueEqual(count, menu.getCarried().getCount(), "player receives the disenchanted books");

		int awarded = sumOrbs(context.getLevel(), absolute);
		context.assertValueEqual(half, awarded, "awarded experience for " + count + " books");
		discardOrbs(context.getLevel(), absolute);
		return awarded;
	}

	private static int enchantmentMinCost(GameTestHelper context, ResourceKey<Enchantment> key, int level) {
		Holder<Enchantment> holder = context.getLevel()
			.registryAccess()
			.lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(key);
		return holder.value().getMinCost(level);
	}

	private static long seedForZeroRoll(int bound) {
		for (long seed = 1L; seed < 10_000L; seed++) {
			if (RandomSource.create(seed).nextInt(bound) == 0) {
				return seed;
			}
		}
		throw new IllegalStateException("no seed yields a zero grindstone xp roll for bound " + bound);
	}

	private static int sumOrbs(ServerLevel level, BlockPos origin) {
		return level.getEntitiesOfClass(ExperienceOrb.class, new AABB(origin).inflate(2))
			.stream()
			.mapToInt(ExperienceOrb::getValue)
			.sum();
	}

	private static void discardOrbs(ServerLevel level, BlockPos origin) {
		for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(origin).inflate(2))) {
			orb.discard();
		}
	}
}
