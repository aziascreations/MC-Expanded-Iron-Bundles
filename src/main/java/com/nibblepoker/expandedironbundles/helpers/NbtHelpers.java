package com.nibblepoker.expandedironbundles.helpers;

import com.nibblepoker.expandedironbundles.ExpandedIronBundlesMod;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
		ResourceLocation itemIdentifier = Registry.ITEM.getKey(itemStack.getItem());
		nbt.putString("id", itemIdentifier == null ? "minecraft:air" : itemIdentifier.toString());
		nbt.putInt("Count", itemStack.getCount());
		if (itemStack.getTag() != null) {
			nbt.put("tag", itemStack.getTag().copy());
		}
		return nbt;
	}
	
	// Converted from "ItemStack.of" and the related "ItemStack(CompoundTag p_41608_)" constructor.
	public static ItemStack readLargeItemStackFromNbt(CompoundTag nbt) {
		ItemStack returnedItemStack = EMPTY;
		
		try {
			// Grabbing an item instance from the registry by using the id in the NBT.
			
			//FIXME: !!!
			//this.capNBT = nbt.contains("ForgeCaps") ? nbt.getCompound("ForgeCaps") : null;
			
			Item readItem = Registry.ITEM.get(new ResourceLocation(nbt.getString("id")));
			
			//FIXME: !!!
			//this.delegate = net.minecraftforge.registries.ForgeRegistries.ITEMS.getDelegateOrThrow(readItem);
			
			// Will return the actual amount, or 0 if not found with that data type.
			// The value of '1' is used as a fallback and to shut Intellij up.
			int itemCount = 1;
			
			try {
				Tag countNbtElement = nbt.get("Count");
				if (countNbtElement != null) {
					if (countNbtElement.getClass().equals(IntTag.class)) {
						// No transformation should be done !
						itemCount = nbt.getInt("Count");
					} else if (countNbtElement.getClass().equals(ByteTag.class)) {
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
				itemStack.setTag(nbt.getCompound("tag"));
				assert itemStack.getTag() != null;
				itemStack.getItem().verifyTagAfterLoad(itemStack.getTag());
			}
			// FIXME: !!!
			// this.forgeInit();
			
			if (itemStack.getItem().isDamageable(itemStack)) {
				itemStack.setDamageValue(itemStack.getDamageValue());
			}
			
			returnedItemStack = itemStack;
		} catch (RuntimeException err) {
			ExpandedIronBundlesMod.LOGGER.debug("Tried to load invalid item: {}", nbt, err);
			return EMPTY;
		}
		
		return returnedItemStack;
	}
}
