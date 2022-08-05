package com.nibblepoker.expandedironbundles.helpers.nbt;

import com.nibblepoker.expandedironbundles.ExpandedIronBundlesMod;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
		CompoundTag stackNbtCompound = stack.getTag();
		
		if (stack.hasTag() && stackNbtCompound != null) {
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
		stack.removeTagKey(NBT_FILTER_KEY);
	}
	
	/**
	 * ???
	 * @param filterContainerStack ???
	 * @param filteredStack ???
	 */
	public static void setFilter(ItemStack filterContainerStack, ItemStack filteredStack) {
		CompoundTag nbtCompound = filterContainerStack.getOrCreateTag();
		ResourceLocation itemIdentifier = Registry.ITEM.getKey(filteredStack.getItem());
		nbtCompound.putString(NBT_FILTER_KEY, itemIdentifier == null ? "minecraft:air" : itemIdentifier.toString());
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public static String getFilteredItemName(ItemStack stack) {
		CompoundTag stackNbtCompound = stack.getTag();
		
		if (stack.hasTag() && stackNbtCompound != null) {
			/*try {
				return Registry.ITEM.get(new Identifier(stackNbtCompound.getString(NBT_FILTER_KEY))).getName().getString();
			} catch(InvalidIdentifierException ignored) {}/**/
			return "Goo goo ga ga";
		}
		
		// Fallback in case the function is called when it shouldn't.
		return "???";
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public static String getFilteredItemIdentifier(ItemStack stack) {
		CompoundTag stackNbtCompound = stack.getTag();
		
		if (stack.hasTag() && stackNbtCompound != null) {
			try {
				return new ResourceLocation(stackNbtCompound.getString(NBT_FILTER_KEY)).toString();
			} catch(ResourceLocationException ignored) {}
			// FIXME: Check if this is the right exception
		}
		
		// Fallback in case the function is called when it shouldn't.
		return "???";
	}
	
	/**
	 * Checks if a given <i>ItemStack</i> passes the filter applied to another <i>ItemStack</i>.
	 * @param filteringStack ???
	 * @param filteredStack ???
	 * @return <i>true</i> if the filter matches the filtered item, <i>false</i> otherwise.
	 */
	public static boolean doesItemPassFilter(ItemStack filteringStack, ItemStack filteredStack) {
		if (!doesItemHaveFilter(filteringStack)) {
			return true;
		}
		
		String a = getFilteredItemIdentifier(filteringStack);
		String b = Registry.ITEM.getKey(filteredStack.getItem()).toString();
		
		ExpandedIronBundlesMod.LOGGER.info(a+" vs "+b);
		
		return a.equals(b);
	}
	
	/**
	 * Used by the "filled" predicate through <i>"ModelPredicateProviderRegistry"</i>.
	 * @param stack ???
	 * @return ???
	 */
	public static float getFilledPredicateValue(ItemStack stack) {
		return doesItemHaveFilter(stack) ? 1.0F : 0.0F;
	}
}
