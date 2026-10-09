package stackables.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.UUID;

final class ConsumeOneGameTestHelper {

	private ConsumeOneGameTestHelper() {
	}

	static ServerPlayer createPlayer(ServerLevel level, int experienceLevels) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), "GameTestPlayer");
		ServerPlayer player = new ServerPlayer(
			level.getServer(),
			level,
			profile,
			ClientInformation.createDefault()
		);
		player.experienceLevel = experienceLevels;
		player.getAbilities().instabuild = false;
		return player;
	}

	static void floor(GameTestHelper context) {
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				context.setBlock(x, 0, z, Blocks.STONE.defaultBlockState());
			}
		}
	}

	static AnvilMenu openAnvil(GameTestHelper context, ServerPlayer player) {
		floor(context);
		BlockPos rel = new BlockPos(1, 1, 1);
		context.setBlock(rel, Blocks.ANVIL.defaultBlockState());
		ContainerLevelAccess access = ContainerLevelAccess.create(context.getLevel(), context.absolutePos(rel));
		return new AnvilMenu(0, player.getInventory(), access);
	}

	static GrindstoneMenu openGrindstone(GameTestHelper context, ServerPlayer player) {
		floor(context);
		BlockPos rel = new BlockPos(1, 1, 1);
		context.setBlock(rel, Blocks.GRINDSTONE.defaultBlockState());
		ContainerLevelAccess access = ContainerLevelAccess.create(context.getLevel(), context.absolutePos(rel));
		return new GrindstoneMenu(0, player.getInventory(), access);
	}

	static ItemStack enchantedBook(GameTestHelper context, ResourceKey<Enchantment> key, int level, int count) {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK, count);
		Holder<Enchantment> holder = context.getLevel()
			.registryAccess()
			.lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(key);
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(holder, level);
		book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
		return book;
	}
}
