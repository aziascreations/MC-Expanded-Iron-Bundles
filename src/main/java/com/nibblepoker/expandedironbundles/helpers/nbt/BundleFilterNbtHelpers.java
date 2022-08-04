package com.nibblepoker.expandedironbundles.helpers.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

public class BundleFilterNbtHelpers {
	/**
	 * NBT key used to store all upgrades' information in a NBTCompound.
	 */
	public static final String NBT_FILTER_KEY = "Filter";
	
	/**
	 * Checks if a given <i>ItemStack</i> has a filter applied to it.
	 * @param stack The <i>ItemStack</i> to check.
	 * @return <i>true</i> if any upgrade was found, <i>false</i> otherwise.
	 */
	public static boolean doesItemHaveFilter(ItemStack stack) {
		NbtCompound stackNbtCompound = stack.getNbt();
		
		if (stack.hasNbt() && stackNbtCompound != null) {
			return stackNbtCompound.contains(NBT_FILTER_KEY);
		} else {
			return false;
		}
	}
	
	/**
	 * ???
	 * @param stack ???
	 */
	public static void removeFilter(ItemStack stack) {
		stack.removeSubNbt(NBT_FILTER_KEY);
	}
	
	/**
	 * ???
	 * @param filterContainerStack ???
	 * @param filteredStack ???
	 */
	public static void setFilter(ItemStack filterContainerStack, ItemStack filteredStack) {
		NbtCompound nbtCompound = filterContainerStack.getOrCreateNbt();
		Identifier itemIdentifier = Registry.ITEM.getId(filteredStack.getItem());
		nbtCompound.putString(NBT_FILTER_KEY, itemIdentifier == null ? "minecraft:air" : itemIdentifier.toString());
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public static String getFilteredItemName(ItemStack stack) {
		NbtCompound stackNbtCompound = stack.getNbt();
		
		if (stack.hasNbt() && stackNbtCompound != null) {
			try {
				return Registry.ITEM.get(new Identifier(stackNbtCompound.getString(NBT_FILTER_KEY))).getName().getString();
			} catch(InvalidIdentifierException ignored) {}
		}
		
		// Fallback in case the function is called when it shouldn't.
		return "???";
	}
	
	/**
	 * Used by the "filled" predicate through <i>"ModelPredicateProviderRegistry"</i>.
	 * @param stack ???
	 * @return ???
	 */
	public static float getFilledPredicateValue(ItemStack stack) {
		return doesItemHaveFilter(stack) ? 1.0F : 0.0F;
	}
	
	// TODO: Get itemStack or Item instance, idk which one is used for filtering.
}
