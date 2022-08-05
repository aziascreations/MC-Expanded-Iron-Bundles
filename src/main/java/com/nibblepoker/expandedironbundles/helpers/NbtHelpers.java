package com.nibblepoker.expandedironbundles.helpers;

import com.nibblepoker.expandedironbundles.ExpandedIronBundlesMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.world.item.ItemStack.EMPTY;

public class NbtHelpers {
	/**
	 * Writes an item stack into a NBT compound using a TAG_Long instead of a TAG_Byte.
	 * @param itemStack The item stack that should be inserted in the NBT compound.
	 * @param nbt The NBT compound into which the item should be inserted.
	 * @return The updated NBT compound
	 */
	public static CompoundTag writeLargeItemStackNbt(ItemStack itemStack, CompoundTag nbt) {
		Identifier itemIdentifier = Registry.ITEM.getId(itemStack.getItem());
		nbt.putString("id", itemIdentifier == null ? "minecraft:air" : itemIdentifier.toString());
		nbt.putInt("Count", itemStack.getCount());
		if (itemStack.getTag() != null) {
			nbt.put("tag", itemStack.getTag().copy());
		}
		return nbt;
	}
	
	public static ItemStack readLargeItemStackFromNbt(CompoundTag nbt) {
		ItemStack returnedItemStack = EMPTY;
		
		try {
			// Grabbing an item instance from the registry by using the id in the NBT.
			Item readItem = Registry.ITEM.get(new Identifier(nbt.getString("id")));
			
			// Will return the actual amount, or 0 if not found with that data type.
			// The value of '1' is used as a fallback and to shut Intellij up.
			int itemCount = 1;
			
			try {
				NbtElement countNbtElement = nbt.get("Count");
				if (countNbtElement != null) {
					if (countNbtElement.getClass().equals(NbtInt.class)) {
						// No transformation should be done !
						itemCount = nbt.getInt("Count");
					} else if (countNbtElement.getClass().equals(NbtByte.class)) {
						// Attempting to read vanilla/legacy byte variables
						byte itemCountByte = nbt.getByte("Count");
						
						if (itemCountByte > 0) {
							itemCount = itemCountByte;
						} else if(itemCountByte < 0) {
							// Fixing items that possibly went in the negatives.
							itemCount = 256 - Math.abs((int) itemCountByte);
						}
					} else {
						itemCount = 0;
					}
				} else {
					itemCount = 0;
				}
			} catch(NullPointerException err) {
				itemCount = 0;
			}
			
			// Preparing the ItemStack.
			// This pretty much a copy of the "ItemStack(NbtCompound nbt)" constructor.
			ItemStack itemStack = new ItemStack(readItem, itemCount);
			
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
