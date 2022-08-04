package com.nibblepoker.expandedironbundles.helpers.nbt;

import com.nibblepoker.expandedironbundles.helpers.NbtHelpers;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class StorageNbtHelpers {
	/**
	 * NBT key used to store all items in a NBTCompound.
	 */
	public static final String NBT_ITEMS_KEY = "Items";
	
	/**
	 * Represents the maximum stack size an item can normally have in the gave.
	 * This value is isolated in a constant to improve the code's readability.
	 */
	public static final int DEFAULT_STACK_SIZE = 64;
	
	/**
	 * Flat occupancy value added to any stored bundle.
	 * The occupancy value of the stored bundle is added to this value afterward.
	 */
	public static final int NESTED_BUNDLE_BASE_OCCUPANCY = 4;
	
	/**
	 * Used by the "filled" predicate through <i>"ModelPredicateProviderRegistry"</i>.
	 * @param stack ???
	 * @return ???
	 */
	public static float getAmountFilled(ItemStack stack) {
		return (float)getStorageItemOccupancy(stack) / 64.0F;
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public static boolean doesItemHaveItemsStored(ItemStack stack) {
		return doesItemHaveItemsStored(stack, NBT_ITEMS_KEY);
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @param nbtStorageKey ???
	 * @return ???
	 */
	public static boolean doesItemHaveItemsStored(ItemStack stack, String nbtStorageKey) {
		NbtCompound stackNbtCompound = stack.getNbt();
		
		if (stack.hasNbt() && stackNbtCompound != null) {
			return stackNbtCompound.contains(nbtStorageKey);
		} else {
			return false;
		}
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public static int getStorageItemOccupancy(ItemStack stack) {
		return getStorageItemOccupancy(stack, NBT_ITEMS_KEY);
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @param nbtStorageKey ???
	 * @return ???
	 */
	public static int getStorageItemOccupancy(ItemStack stack, String nbtStorageKey) {
		return getStoredItemsStacks(stack, nbtStorageKey)
					   .mapToInt((itemStack) -> getItemOccupancy(itemStack) * itemStack.getCount()).sum();
	}
	
	public static Stream<ItemStack> getStoredItemsStacks(ItemStack stack) {
		return getStoredItemsStacks(stack, NBT_ITEMS_KEY);
	}
	
	/*public static Stream<ItemStack> getStoredItemsSplittedStacks(ItemStack stack) {
		return getStoredItemsStacks(stack, NBT_ITEMS_KEY, true);
	}/**/
	
	public static Stream<ItemStack> getStoredItemsStacks(ItemStack stack, String nbtStorageKey) {
		// Param: , boolean splitStacksAtMaxSize
		
		NbtCompound nbtCompound = stack.getNbt();
		
		if (nbtCompound == null) {
			return Stream.empty();
		}
		
		NbtList nbtList = nbtCompound.getList(nbtStorageKey, 10);
		Stream<NbtElement> nbtStream = nbtList.stream();
		Objects.requireNonNull(NbtCompound.class);
		
		// TODO: Handle 'splitStacksAtMaxSize' !
		
		return nbtStream.map(NbtCompound.class::cast).map(NbtHelpers::readLargeItemStackFromNbt);
	}
	
	public static int getItemOccupancy(ItemStack stack) {
		// Checking if the given item is a bundle.
		// TODO: Improve this and make an interface/tags ?
		if (stack.getItem() instanceof BundleItem) {
			// FIXME: Cannot handle items which don't use the constant key !
			return NESTED_BUNDLE_BASE_OCCUPANCY + getStorageItemOccupancy(stack);
		}
		
		// Checking if it is a beehive or a bee nest.
		if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
			NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(stack);
			if (nbtCompound != null && !nbtCompound.getList("Bees", 10).isEmpty()) {
				return DEFAULT_STACK_SIZE;
			}
		}
		
		// TODO: Check if a fix/condition is needed for modded items !
		// A merging comparison is done elsewhere, nothing should be lost here !
		
		return DEFAULT_STACK_SIZE / stack.getMaxCount();
	}
	
	public static Optional<NbtCompound> canMergeStack(ItemStack stack, NbtList itemsList) {
		// FIXME: Check how a stack splitter can be handled here !
		if (stack.isOf(Items.BUNDLE)) {
			return Optional.empty();
		} else {
			Stream<NbtElement> nbtStream = itemsList.stream();
			Objects.requireNonNull(NbtCompound.class);
			nbtStream = nbtStream.filter(NbtCompound.class::isInstance);
			Objects.requireNonNull(NbtCompound.class);
			return nbtStream.map(NbtCompound.class::cast).filter((itemNbt) -> {
				return ItemStack.canCombine(NbtHelpers.readLargeItemStackFromNbt(itemNbt), stack);
			}).findFirst();
		}
	}
	
	public static int addStackToStorage(ItemStack storageStack, ItemStack addedStack, int maxOccupancy) {
		return addStackToStorage(storageStack, addedStack, maxOccupancy, NBT_ITEMS_KEY);
	}
	
	/**
	 *
	 * aaa<br>
	 * <br><b>Warning</b><br>
	 * If a number of item that is greater than what the given bundle can accept, some items <b>WILL</b> be lost !
	 * @param storageStack Storage item into which the items are being inserted.
	 * @param addedStack Items that are being inserted into the storage item.
	 * @param maxOccupancy Maximum occupancy of the given storage.
	 * @param nbtStorageKey ???
	 * @return The amount of items that were actually inserted into the bundle.
	 */
	public static int addStackToStorage(ItemStack storageStack, ItemStack addedStack, int maxOccupancy, String nbtStorageKey) {
		// Checking if there is some space left and if the item itself can be inserted in a bundle
		if (!addedStack.isEmpty() && addedStack.getItem().canBeNested()) {
			// Preparing the NBT compound that will keep all the item data.
			NbtCompound nbtCompound = storageStack.getOrCreateNbt();
			if (!nbtCompound.contains(nbtStorageKey)) {
				nbtCompound.put(nbtStorageKey, new NbtList());
			}
			
			int insertableItemCount = Math.min(
					addedStack.getCount(),
					(maxOccupancy - getStorageItemOccupancy(storageStack)) / getItemOccupancy(addedStack)
			);
			
			if (insertableItemCount != 0) {
				// Grabbing the list of all item stacks as NBT.
				NbtList nbtList = nbtCompound.getList(nbtStorageKey, 10);
				
				// Grabbing the item stack's NBT compound into which the given item stack can be merged.
				// Will be empty if no other stack was found and a new one needs to be created for it.
				Optional<NbtCompound> optional = canMergeStack(addedStack, nbtList);
				
				if (optional.isPresent()) {
					// Grabbing the actual ItemStack from the NbtCompound, and removing it from the existing NBT list.
					NbtCompound mergedItemStackNbtCompound = optional.get();
					ItemStack itemStack = NbtHelpers.readLargeItemStackFromNbt(mergedItemStackNbtCompound);
					nbtList.remove(mergedItemStackNbtCompound);
					
					// Incrementing the stack's size
					itemStack.increment(insertableItemCount);
					
					// Updating the NbtCompound read from the bundle.
					NbtHelpers.writeLargeItemStackNbt(itemStack, mergedItemStackNbtCompound);
					
					// Adding back the NBT compound at the start of the list.
					nbtList.add(0, mergedItemStackNbtCompound);
				} else {
					// An existing stack for that item couldn't be found, so we make a copy to store in the bundle.
					// The process is roughly the same as above.
					ItemStack stackCopy = addedStack.copy();
					stackCopy.setCount(insertableItemCount);
					NbtCompound stackNbtCompound = new NbtCompound();
					NbtHelpers.writeLargeItemStackNbt(stackCopy, stackNbtCompound);
					nbtList.add(0, stackNbtCompound);
				}
			}
			
			return insertableItemCount;
		} else {
			return 0;
		}
	}
	
	/**
	 * Removes the first stack contained within the given storage item and returns it.
	 * @param stack Storage item from which the stack is being removed.
	 * @return An <i>"ItemStack"</i> if any could be extracted from the given storage item.
	 */
	public static Optional<ItemStack> removeFirstStack(ItemStack stack) {
		return removeFirstStack(stack, NBT_ITEMS_KEY);
	}
	
	/**
	 * Removes the first stack contained within the given storage item and returns it.
	 * @param stack Storage item from which the stack is being removed.
	 * @param nbtStorageKey ???
	 * @return An <i>"ItemStack"</i> if any could be extracted from the given storage item.
	 */
	public static Optional<ItemStack> removeFirstStack(ItemStack stack, String nbtStorageKey) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		
		if (!nbtCompound.contains(nbtStorageKey)) {
			// No items were stored in the bundle
			return Optional.empty();
		} else {
			NbtList nbtList = nbtCompound.getList(nbtStorageKey, 10);
			if (nbtList.isEmpty()) {
				// No items were stored in the list, this shouldn't normally happen, probably...
				return Optional.empty();
			} else {
				NbtCompound extractedItemNbtCompound = nbtList.getCompound(0);
				ItemStack itemStack = NbtHelpers.readLargeItemStackFromNbt(extractedItemNbtCompound);
				
				nbtList.remove(0);
				if (nbtList.isEmpty()) {
					stack.removeSubNbt(nbtStorageKey);
				}
				
				return Optional.of(itemStack);
			}
		}
	}
}
