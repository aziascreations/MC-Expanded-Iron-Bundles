package com.nibblepoker.expandedironbundles.items;

import com.nibblepoker.expandedironbundles.ExpandedIronBundlesMod;
import com.nibblepoker.expandedironbundles.helpers.NbtHelpers;
import com.nibblepoker.expandedironbundles.helpers.nbt.BundleFilterNbtHelpers;
import com.nibblepoker.expandedironbundles.helpers.nbt.StorageNbtHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ExtendedBundleItem extends BundleItem {
	/**
	 * NBT key used to store all items nested in the bundle.
	 */
	private static final String NBT_ITEMS_KEY = StorageNbtHelpers.NBT_ITEMS_KEY;
	
	/**
	 * Color used in the GUI to represent the bundle's current occupancy usage when it isn't overflowing.
	 */
	private static final int ITEM_BAR_COLOR_REGULAR = Mth.color(0.4F, 0.4F, 1.0F);
	
	/**
	 * Color used in the GUI to represent the bundle's current occupancy usage when it is overflowing.
	 */
	private static final int ITEM_BAR_COLOR_OVERFLOWING = Mth.color(1.0F, 0.2F, 0.2F);
	
	/**
	 * Maximum amount of items a bundle can hold at any given time.
	 */
	public final int maxBaseOccupancy;
	
	/**
	 * Maximum amount of upgrades that can be applied to a bundle at any given time.
	 */
	public final int maxUpgrades;
	
	public ExtendedBundleItem(Properties settings, int maxOccupancy, int maxUpgrades) {
		super(settings.stacksTo(1));
		this.maxBaseOccupancy = maxOccupancy;
		this.maxUpgrades = maxUpgrades;
	}
	
	/**
	 * Calculates the max occupancy while taking into account upgrades and filters.
	 * @param stack ???
	 * @return The bundle's final max occupancy.
	 */
	public int getMaxOccupancy(ItemStack stack) {
		return this.maxBaseOccupancy * (BundleFilterNbtHelpers.doesItemHaveFilter(stack) ? 2 : 1);
	}
	
	/**
	 * Triggered when the item is used on any inventory-like slot while the item is being held at the cursor.
	 * @param stack ???
	 * @param slot ???
	 * @param clickType Type of click used when the call was triggered, refers mostly to the mouse.
	 * @param player ???
	 * @return <i>true</i> if the event was handled, <i>false</i> otherwise and if any other handler should be called.
	 */
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
		if (clickType != ClickAction.SECONDARY) {
			// The item was put in the inventory with a left click, most likely.
			// The event won't be handled here, we let other functions handle the event.
			return false;
		}
		
		// Grabbing the ItemStack in the clicked Slot.
		ItemStack targetItemStack = slot.getItem();
		
		if (targetItemStack.isEmpty()) {
			// The targeted slot was empty, we will try to remove an item from the bundle.
			this.playRemoveOneSound(player);
			
			// We grab and remove the first stack in the bundle, put it in a slot while adding the remainder back.
			// We are also using "Integer.MAX_VALUE" instead of "this.getMaxOccupancy(stack)" to prevent item deletion
			//  when removing from the bundle into the inventory while the bundle is overflowing before AND after
			//  removing a stack.
			StorageNbtHelpers.removeFirstStack(stack, NBT_ITEMS_KEY).ifPresent((removedStack) -> StorageNbtHelpers.addStackToStorage(
					stack, slot.safeInsert(removedStack), Integer.MAX_VALUE, NBT_ITEMS_KEY
			));
		} else if (targetItemStack.getItem().canFitInsideContainerItems()) {
			// We attempt to add an item to the bundle.
			if(BundleFilterNbtHelpers.doesItemPassFilter(stack, targetItemStack)) {
				int insertableItemCount = (
						this.getMaxOccupancy(stack) - StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY)
				) / StorageNbtHelpers.getItemOccupancy(targetItemStack);
				
				int itemAddedCount = StorageNbtHelpers.addStackToStorage(
						stack,
						slot.safeTake(targetItemStack.getCount(), insertableItemCount, player),
						this.getMaxOccupancy(stack),
						NBT_ITEMS_KEY
				);
				
				if(itemAddedCount > 0) {
					this.playInsertSound(player);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Function called when the item is clicked on with the mouse cursor or picked up from any "inventory-like" storage.
	 * @param storageStack ???
	 * @param otherStack ???
	 * @param slot ???
	 * @param clickType Type of click used when the call was triggered, refers mostly to the mouse.
	 * @param player ???
	 * @param cursorStackReference ???
	 * @return <i>true</i> if the event was handled, <i>false</i> otherwise and if any other handler should be called.
	 */
	public boolean overrideOtherStackedOnMe(ItemStack storageStack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
		// Checking if we right-clicked on the bundle with an item that can be split into multiple stacks.
		if (clickType == ClickAction.SECONDARY && slot.allowModification(player)) {
			if (otherStack.isEmpty()) {
				// We remove an item from the bundle.
				StorageNbtHelpers.removeFirstStack(storageStack, NBT_ITEMS_KEY).ifPresent((removedStack) -> {
					this.playRemoveOneSound(player);
					cursorStackReference.set(removedStack);
				});
			} else {
				// We attempt to add an item to the bundle.
				if(BundleFilterNbtHelpers.doesItemPassFilter(storageStack, otherStack)) {
					int itemAddedCount = StorageNbtHelpers.addStackToStorage(
							storageStack, otherStack, this.getMaxOccupancy(storageStack), NBT_ITEMS_KEY
					);
					
					if (itemAddedCount > 0) {
						this.playInsertSound(player);
						otherStack.shrink(itemAddedCount);
					}
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Called when the item is used with a right-click in the world.
	 * This function may be called twice for the client's world and the server's world.
	 * @param world N/A
	 * @param user N/A
	 * @param hand Hand in which the activated item resides.
	 * @return N/A
	 */
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack itemStack = user.getItemInHand(hand);
		
		ExpandedIronBundlesMod.LOGGER.info(String.valueOf(user.isCrouching()));
		
		boolean wasSomethingDropped = false;
		
		if (user.isCrouching()) {
			wasSomethingDropped = dropSelectedBundledItems(itemStack, user);
		} else {
			wasSomethingDropped = dropAllBundledItems(itemStack, user);
		}
		
		if (wasSomethingDropped) {
			this.playDropContentsSound(user);
			user.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
		}
		
		return InteractionResultHolder.fail(itemStack);
	}
	
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY) > 0;
	}
	
	/**
	 * Checks of the given bundle is overflowing.
	 * @param stack ???
	 * @return <i>true</i> if the bundle is overflowing, <i>false</i> otherwise.
	 */
	public boolean isOverflowing(ItemStack stack) {
		return StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY) > this.getMaxOccupancy(stack);
	}
	
	public int getBarWidth(ItemStack stack) {
		return Math.min(1 + 12 * StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY) / this.getMaxOccupancy(stack), 13);
	}
	
	public int getBarColor(ItemStack stack) {
		return this.isOverflowing(stack) ? ITEM_BAR_COLOR_OVERFLOWING : ITEM_BAR_COLOR_REGULAR;
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @return ???
	 */
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		NonNullList<ItemStack> defaultedList = NonNullList.create();
		
		Stream<ItemStack> bundleItemsStream = StorageNbtHelpers.getStoredItemsStacks(stack, NBT_ITEMS_KEY);
		bundleItemsStream.forEach(defaultedList::add);
		
		return Optional.of(new BundleTooltip(
				defaultedList,
				StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY)
		));
	}
	
	
	/**
	 * Adds text to the tooltip when hovering the item.
	 * Should display the fullness using the <i>"{current_amount}/{max_amount}"</i> format.
	 * @param stack ???
	 * @param world ???
	 * @param tooltip ???
	 * @param context ???
	 */
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
		tooltip.add(Component.translatable(
				"item.expandedironbundles.bundle.fullness.occupancy",
				StorageNbtHelpers.getStorageItemOccupancy(stack, NBT_ITEMS_KEY),
				this.getMaxOccupancy(stack)
		).withStyle(ChatFormatting.GRAY));
		
		if (BundleFilterNbtHelpers.doesItemHaveFilter(stack)) {
			tooltip.add(Component.translatable(
					"item.expandedironbundles.bundle.fullness.filtered",
					BundleFilterNbtHelpers.getFilteredItemName(stack)
			).withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable(
					"item.expandedironbundles.bundle.fullness.unfiltered"
			).withStyle(ChatFormatting.GRAY));
		}
	}
	
	/**
	 * Triggered when the bundle is destroyed in the world by a fire or lava.
	 * @param entity The destroyed bundle as an <i>ItemEntity</i>.
	 */
	public void onDestroyed(ItemEntity entity) {
		ItemUtils.onContainerDestroyed(entity, StorageNbtHelpers.getStoredItemsStacks(entity.getItem(), NBT_ITEMS_KEY));
	}
	
	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}
	
	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}
	
	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}
	
	/**
	 * Removes all items from a given bundle and spawns them at a given player's location in the direction they are
	 *  looking.
	 * @param stack Bundle from which the items are removed.
	 * @param player Player whose position should be used to spawn the newly dropped items.
	 * @return <i>true</i> if any items were dropped, <i>false</i> otherwise.
	 */
	private static boolean dropAllBundledItems(ItemStack stack, Player player) {
		if (!StorageNbtHelpers.doesItemHaveItemsStored(stack, NBT_ITEMS_KEY)) {
			return false;
		}
		
		CompoundTag bundleNbtCompound = stack.getOrCreateTag();
		
		if (player instanceof ServerPlayer) {
			ListTag bundleNbtItemList = bundleNbtCompound.getList(NBT_ITEMS_KEY, 10);
			
			for(int i = 0; i < bundleNbtItemList.size(); ++i) {
				CompoundTag droppedItemNbtCompound = bundleNbtItemList.getCompound(i);
				ItemStack itemStack = NbtHelpers.readLargeItemStackFromNbt(droppedItemNbtCompound);
				player.drop(itemStack, true);
			}
		}
		
		stack.removeTagKey(NBT_ITEMS_KEY);
		return true;
	}
	
	/**
	 * Removes the currently selected item in the bundle and spawns it at a given player's location in the direction
	 *  they are looking.
	 * @param stack Bundle from which the items are removed.
	 * @param player Player whose position should be used to spawn the newly dropped items.
	 * @return <i>true</i> if any items were dropped, <i>false</i> otherwise.
	 */
	private static boolean dropSelectedBundledItems(ItemStack stack, Player player) {
		if (!StorageNbtHelpers.doesItemHaveItemsStored(stack, NBT_ITEMS_KEY)) {
			return false;
		}
		
		AtomicBoolean wasSomethingDropped = new AtomicBoolean(false);
		
		StorageNbtHelpers.removeFirstStack(stack, NBT_ITEMS_KEY).ifPresent((removedStack) -> {
			player.drop(removedStack, true);
			wasSomethingDropped.set(true);
		});
		
		return wasSomethingDropped.get();
	}
}
