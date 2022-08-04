package com.nibblepoker.expandedironbundles.helpers.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class BundleUpgradeNbtHelpers {
	/**
	 * NBT key used to store all upgrades' information in a NBTCompound.
	 */
	public static final String NBT_UPGRADES_KEY = "Upgrades";
	
	/**
	 * Checks if a given <i>ItemStack</i> has any upgrades applied to it.
	 * @param stack The <i>ItemStack</i> to check.
	 * @return <i>true</i> if any upgrade was found, <i>false</i> otherwise.
	 */
	public static boolean doesItemHaveUpgrades(ItemStack stack) {
		NbtCompound stackNbtCompound = stack.getNbt();
		
		if (stack.hasNbt() && stackNbtCompound != null) {
			return stackNbtCompound.contains(NBT_UPGRADES_KEY);
		} else {
			return false;
		}
	}
	
	/**
	 * Checks how many upgrades a given <i>ItemStack</i> has.
	 * @param stack The <i>ItemStack</i> to check.
	 * @return The amount of upgrades as an <i>int</i>
	 */
	public static int getItemUpgradesCount(ItemStack stack) {
		NbtCompound stackNbtCompound = stack.getNbt();
		
		if (stack.hasNbt() && stackNbtCompound != null) {
			return stackNbtCompound.getList(NBT_UPGRADES_KEY, 10).size();
		}
		
		return 0;
	}
	
	
}