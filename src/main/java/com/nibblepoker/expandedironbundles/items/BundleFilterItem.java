package com.nibblepoker.expandedironbundles.items;

import com.nibblepoker.expandedironbundles.helpers.nbt.BundleFilterNbtHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BundleFilterItem extends Item {
	public BundleFilterItem(Properties settings) {
		super(settings.stacksTo(1));
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
			// No item was right-clicked on, no filter will be set.
			return false;
		}
		
		// Applying the filter.
		BundleFilterNbtHelpers.setFilter(stack, targetItemStack);
		this.playFilterSetSound(player);
		
		return true;
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
		
		if (BundleFilterNbtHelpers.doesItemHaveFilter(itemStack)) {
			BundleFilterNbtHelpers.removeFilter(itemStack);
			this.playFilterSetSound(user);
			return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
		} else {
			return InteractionResultHolder.fail(itemStack);
		}
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
		if (BundleFilterNbtHelpers.doesItemHaveFilter(stack)) {
			tooltip.add(Component.translatable(
					"tooltip.expandedironbundles.bundle_filter_set",
					BundleFilterNbtHelpers.getFilteredItemName(stack)
			).withStyle(ChatFormatting.GRAY));
		} else {
			tooltip.add(Component.translatable(
					"tooltip.expandedironbundles.bundle_filter_unset"
			).withStyle(ChatFormatting.GRAY));
		}
	}
	
	private void playFilterSetSound(Entity entity) {
		entity.playSound(SoundEvents.VILLAGER_WORK_SHEPHERD, 0.8F, 0.8F + entity.getLevel().getRandom().nextFloat() * 0.4F);
	}
}
