package com.nibblepoker.expandedironbundles.helpers;

import com.nibblepoker.expandedironbundles.ExpandedIronBundlesMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static net.minecraft.item.ItemStack.EMPTY;

public class NbtHelpers {
	/**
	 * Writes an item stack into a NBT compound using a TAG_Long instead of a TAG_Byte.
	 * @param itemStack The item stack that should be inserted in the NBT compound.
	 * @param nbt The NBT compound into which the item should be inserted.
	 * @return The updated NBT compound
	 */
	public static NbtCompound writeLargeItemStackNbt(ItemStack itemStack, NbtCompound nbt) {
		Identifier itemIdentifier = Registry.ITEM.getId(itemStack.getItem());
		nbt.putString("id", itemIdentifier == null ? "minecraft:air" : itemIdentifier.toString());
		nbt.putInt("Count", itemStack.getCount());
		if (itemStack.getNbt() != null) {
			nbt.put("tag", itemStack.getNbt().copy());
		}
		return nbt;
	}
	
	public static ItemStack readLargeItemStackFromNbt(NbtCompound nbt) {
		ItemStack returnedItemStack = EMPTY;
		
		try {
			Item readItem = Registry.ITEM.get(new Identifier(nbt.getString("id")));
			
			// Will return the actual amount, or 0 if not found with that data type.
			byte itemCountByte = nbt.getByte("Count");
			int itemCountInt = nbt.getInt("Count");
			
			// Attempting to read vanilla/legacy byte variables
			if (itemCountByte > 0) {
				itemCountInt = itemCountByte;
			} else if(itemCountByte < 0) {
				// Fixing items that possibly went in the negatives.
				itemCountInt = 256 - Math.abs((int) itemCountByte);
			}
			
			// We can now assume that "itemCountInt" has the right stack size.
			
			// Preparing the ItemStack.
			// This pretty much a copy of the "ItemStack(NbtCompound nbt)" constructor.
			ItemStack itemStack = new ItemStack(readItem, itemCountInt);
			
			if (nbt.contains("tag", 10)) {
				itemStack.setNbt(nbt.getCompound("tag"));
				itemStack.getItem().postProcessNbt(itemStack.getNbt());
			}
			
			if (itemStack.getItem().isDamageable()) {
				itemStack.setDamage(itemStack.getDamage());
			}
			
			returnedItemStack = itemStack;
		} catch (RuntimeException err) {
			ExpandedIronBundlesMod.LOGGER.debug("Tried to load invalid item: {}", nbt, err);
			return EMPTY;
		}
		
		return returnedItemStack;
	}
}
