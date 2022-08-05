package com.nibblepoker.expandedironbundles.items;

import com.nibblepoker.expandedironbundles.helpers.nbt.BundleFilterNbtHelpers;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;

import java.util.List;

public class BundleFilterItem extends Item {
	public BundleFilterItem(Settings settings) {
		super(settings.maxCount(1));
	}
	
	/**
	 * Triggered when the item is used on any inventory-like slot while the item is being held at the cursor.
	 * @param stack ???
	 * @param slot ???
	 * @param clickType Type of click used when the call was triggered, refers mostly to the mouse.
	 * @param player ???
	 * @return <i>true</i> if the event was handled, <i>false</i> otherwise and if any other handler should be called.
	 */
	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) {
			// The item was put in the inventory with a left click, most likely.
			// The event won't be handled here, we let other functions handle the event.
			return false;
		}
		
		// Grabbing the ItemStack in the clicked Slot.
		ItemStack targetItemStack = slot.getStack();
		
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
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		
		if (BundleFilterNbtHelpers.doesItemHaveFilter(itemStack)) {
			BundleFilterNbtHelpers.removeFilter(itemStack);
			this.playFilterSetSound(user);
			return TypedActionResult.success(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
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
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		if (BundleFilterNbtHelpers.doesItemHaveFilter(stack)) {
			tooltip.add(Text.translatable(
					"tooltip.expandedironbundles.bundle_filter_set",
					BundleFilterNbtHelpers.getFilteredItemName(stack)
			).formatted(Formatting.GRAY));
		} else {
			tooltip.add(Text.translatable(
					"tooltip.expandedironbundles.bundle_filter_unset"
			).formatted(Formatting.GRAY));
		}
	}
	
	private void playFilterSetSound(Entity entity) {
		entity.playSound(SoundEvents.ENTITY_VILLAGER_WORK_SHEPHERD, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}
}
