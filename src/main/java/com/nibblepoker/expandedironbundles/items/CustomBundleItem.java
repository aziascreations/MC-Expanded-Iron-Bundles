package com.nibblepoker.expandedironbundles.items;

import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CustomBundleItem extends BundleItem {
	/**
	 * NBT key used to store all items nested in the bundle.
	 */
	private static final String NBT_ITEMS_KEY = "Items";
	
	/**
	 * Represents the maximum stack size an item can normally have in the gave.
	 * This value is isolated in a constant to improve the code's readability.
	 */
	public static final int DEFAULT_STACK_SIZE = 64;
	
	/**
	 * Flat occupancy value added to any nested bundle.
	 * The occupancy value of the bundle being nested is added to this value afterward.
	 */
	public static final int NESTED_BUNDLE_BASE_OCCUPANCY = 4;
	
	/**
	 * Color used in the GUI to represent the bundle's current occupancy usage.
	 */
	private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
	
	/**
	 * Maximum amount of items a bundle can hold at any given time.
	 */
	public final int maxOccupancy;
	
	public CustomBundleItem(Settings settings, int maxOccupancy) {
		super(settings.maxCount(1));
		this.maxOccupancy = maxOccupancy;
	}
	
	/**
	 * Used by the "filled" predicate through <i>"ModelPredicateProviderRegistry"</i>.
	 * @param stack ???
	 * @return ???
	 */
	public static float getAmountFilled(ItemStack stack) {
		return (float)getBundleOccupancy(stack) / 64.0F;
	}
	
	/**
	 * Triggered when the item is clicked in the player's inventory while the item is being held.
	 * @param stack ???
	 * @param slot ???
	 * @param clickType ???
	 * @param player ???
	 * @return ???
	 */
	@Override
	public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
		if (clickType != ClickType.RIGHT) {
			// The event wasn't handled here, and we let other functions handle the click.
			return false;
		}
		
		// Grabbing the item data in the clicked slot.
		ItemStack targetItemStack = slot.getStack();
		
		if (targetItemStack.isEmpty()) {
			this.playRemoveOneSound(player);
			removeFirstStack(stack).ifPresent((removedStack) -> {
				addToBundle(
						stack,
						slot.insertStack(removedStack),
						this.maxOccupancy
				);
			});
		} else if (targetItemStack.getItem().canBeNested()) {
			int insertableItemCount = (this.maxOccupancy - getBundleOccupancy(stack)) / getItemOccupancy(targetItemStack);
			
			int itemAddedCount = addToBundle(
					stack,
					slot.takeStackRange(targetItemStack.getCount(), insertableItemCount, player),
					this.maxOccupancy
			);
			
			if (itemAddedCount > 0) {
				this.playInsertSound(player);
			}
		}
		
		return true;
	}
	
	/**
	 * Function called when the bundle is clicked on with the mouse cursor or picked up from any "inventory-like"
	 *  storage.
	 * @param stack ???
	 * @param otherStack ???
	 * @param slot ???
	 * @param clickType ???
	 * @param player ???
	 * @param cursorStackReference ???
	 * @return true is the click was processed, false otherwise.
	 */
	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
			if (otherStack.isEmpty()) {
				removeFirstStack(stack).ifPresent((itemStack) -> {
					this.playRemoveOneSound(player);
					cursorStackReference.set(itemStack);
				});
			} else {
				int i = addToBundle(stack, otherStack, this.maxOccupancy);
				if (i > 0) {
					this.playInsertSound(player);
					otherStack.decrement(i);
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Called when the item is used with a right-click in the world.
	 * @param world N/A
	 * @param user N/A
	 * @param hand Hand in which the activated item resides.
	 * @return N/A
	 */
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		if (dropAllBundledItems(itemStack, user)) {
			this.playDropContentsSound(user);
			user.incrementStat(Stats.USED.getOrCreateStat(this));
			return TypedActionResult.success(itemStack, world.isClient());
		} else {
			return TypedActionResult.fail(itemStack);
		}
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return getBundleOccupancy(stack) > 0;
	}
	
	@Override
	public int getItemBarStep(ItemStack stack) {
		return Math.min(1 + 12 * getBundleOccupancy(stack) / this.maxOccupancy, 13);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack) {
		return ITEM_BAR_COLOR;
	}
	
	/**
	 * aaa<br>
	 * <br><b>Warning</b><br>
	 * If a number of item that is greater than what the given bundle can accept, some items <b>WILL</b> be lost !
	 * @param bundle Bundle into which the items are being inserted.
	 * @param stack Items that are being inserted into the bundle.
	 * @param bundleMaxOccupancy Maximum occupancy of the given bundle.
	 * @return The amount of items that were actually inserted into the bundle.
	 */
	private static int addToBundle(ItemStack bundle, ItemStack stack, int bundleMaxOccupancy) {
		if (!stack.isEmpty() && stack.getItem().canBeNested()) {
			NbtCompound nbtCompound = bundle.getOrCreateNbt();
			if (!nbtCompound.contains(NBT_ITEMS_KEY)) {
				nbtCompound.put(NBT_ITEMS_KEY, new NbtList());
			}
			
			int insertableItemCount = Math.min(
					stack.getCount(),
					(bundleMaxOccupancy - getBundleOccupancy(bundle)) / getItemOccupancy(stack)
			);
			
			if (insertableItemCount != 0) {
				NbtList nbtList = nbtCompound.getList(NBT_ITEMS_KEY, 10);
				Optional<NbtCompound> optional = canMergeStack(stack, nbtList);
				if (optional.isPresent()) {
					NbtCompound nbtCompound2 = (NbtCompound)optional.get();
					ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
					itemStack.increment(insertableItemCount);
					itemStack.writeNbt(nbtCompound2);
					nbtList.remove(nbtCompound2);
					nbtList.add(0, nbtCompound2);
				} else {
					ItemStack itemStack2 = stack.copy();
					itemStack2.setCount(insertableItemCount);
					NbtCompound nbtCompound3 = new NbtCompound();
					itemStack2.writeNbt(nbtCompound3);
					nbtList.add(0, nbtCompound3);
				}
			}
			
			return insertableItemCount;
		} else {
			return 0;
		}
	}
	
	/**
	 * ???
	 * @param stack ???
	 * @param items ???
	 * @return ???
	 */
	private static Optional<NbtCompound> canMergeStack(ItemStack stack, NbtList items) {
		if (stack.isOf(Items.BUNDLE)) {
			return Optional.empty();
		} else {
			Stream<NbtElement> nbtStream = items.stream();
			Objects.requireNonNull(NbtCompound.class);
			nbtStream = nbtStream.filter(NbtCompound.class::isInstance);
			Objects.requireNonNull(NbtCompound.class);
			return nbtStream.map(NbtCompound.class::cast).filter((item) -> {
				return ItemStack.canCombine(ItemStack.fromNbt(item), stack);
			}).findFirst();
		}
	}
	
	/**
	 * Returns the occupancy value for any given <b>item type</b>.<br>
	 * The value being returned represents the amount of normal items that the given <b>item type</b> will use in a
	 *  bundle <b>per item</b>.<br>
	 * For example an item that cannot be stacked will return 64, and an item whose max stack size is a quarter the size
	 *  of a normal one will return 4 since <i>"64/4=16"</i> which can be rearranged as <i>"16*4=64"</i>.<br>
	 * A simple way to see the returned value is the amount of 64th of a stack that one item of the given type occupies.
	 * @param stack Item whose occupancy should be calculated.
	 * @return The occupancy value of the given item as an <i>int</i>.
	 */
	private static int getItemOccupancy(ItemStack stack) {
		if (stack.getItem() instanceof BundleItem) {
			// Used for nested bundles
			return NESTED_BUNDLE_BASE_OCCUPANCY + getBundleOccupancy(stack);
		} else {
			// Special case for Beehives and Bee nests.
			// TODO: Check if a fix is needed for modded items !
			if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
				NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(stack);
				if (nbtCompound != null && !nbtCompound.getList("Bees", 10).isEmpty()) {
					return DEFAULT_STACK_SIZE;
				}
			}
			
			return DEFAULT_STACK_SIZE / stack.getMaxCount();
		}
	}
	
	/**
	 * Returns the current occupancy value of a given bundle.<br>
	 * The value returned is the amount of 64th of a stack that all items in the given bundle utilise.
	 * @param stack Bundle whose current occupancy should be calculated.
	 * @return Current occupancy value of the given bundle
	 */
	public static int getBundleOccupancy(ItemStack stack) {
		return getBundledStacks(stack).mapToInt((itemStack) -> getItemOccupancy(itemStack) * itemStack.getCount()).sum();
	}
	
	/**
	 * Removes the first stack contained within the given bundle and returns it.
	 * @param stack Bundle from which the stack is being removed.
	 * @return An <i>"ItemStack"</i> if any could be extracted from the given bundle.
	 */
	private static Optional<ItemStack> removeFirstStack(ItemStack stack) {
		NbtCompound nbtCompound = stack.getOrCreateNbt();
		if (!nbtCompound.contains(NBT_ITEMS_KEY)) {
			return Optional.empty();
		} else {
			NbtList nbtList = nbtCompound.getList(NBT_ITEMS_KEY, 10);
			if (nbtList.isEmpty()) {
				return Optional.empty();
			} else {
				NbtCompound nbtCompound2 = nbtList.getCompound(0);
				ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
				nbtList.remove(0);
				if (nbtList.isEmpty()) {
					stack.removeSubNbt(NBT_ITEMS_KEY);
				}
				
				return Optional.of(itemStack);
			}
		}
	}
	
	/**
	 * Removes all items from a given bundle and spawns them at a given player's location in the direction they are
	 *  looking.
	 * @param stack Bundle from which the items are removed.
	 * @param player Player whose position should be used to spawn the newly dropped items.
	 * @return <i>true</i> if any items were dropped, <i>false</i> otherwise.
	 */
	private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
		NbtCompound bundleNbtCompound = stack.getOrCreateNbt();
		
		if (!bundleNbtCompound.contains(NBT_ITEMS_KEY)) {
			return false;
		} else {
			if (player instanceof ServerPlayerEntity) {
				NbtList bundleNbtItemList = bundleNbtCompound.getList(NBT_ITEMS_KEY, 10);
				
				for(int i = 0; i < bundleNbtItemList.size(); ++i) {
					NbtCompound droppedItemNbtCompound = bundleNbtItemList.getCompound(i);
					ItemStack itemStack = ItemStack.fromNbt(droppedItemNbtCompound);
					player.dropItem(itemStack, true);
				}
			}
			
			stack.removeSubNbt(NBT_ITEMS_KEY);
			return true;
		}
	}
	
	/**
	 * Grabs the item stacks from the NBT data for the current bundle and returns it as a stream for further processing.
	 * @param stack Bundle from which the items are grabbed.
	 * @return a <i>"Stream"</i> Object containing all the NBT elements mapped as <i>"ItemStack"</i> contained within
	 *  the item's <i>NBT_ITEMS_KEY</i> NBT field or an empty Stream if no appropriate NBT tag was found.
	 */
	private static Stream<ItemStack> getBundledStacks(ItemStack stack) {
		NbtCompound nbtCompound = stack.getNbt();
		if (nbtCompound == null) {
			return Stream.empty();
		} else {
			NbtList nbtList = nbtCompound.getList(NBT_ITEMS_KEY, 10);
			Stream<NbtElement> nbtStream = nbtList.stream();
			Objects.requireNonNull(NbtCompound.class);
			return nbtStream.map(NbtCompound.class::cast).map(ItemStack::fromNbt);
		}
	}
	
	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		DefaultedList<ItemStack> defaultedList = DefaultedList.of();
		Stream<ItemStack> bundleItemsStream = getBundledStacks(stack);
		Objects.requireNonNull(defaultedList);
		bundleItemsStream.forEach(defaultedList::add);
		return Optional.of(new BundleTooltipData(defaultedList, getBundleOccupancy(stack)));
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
		tooltip.add(Text.translatable("item.minecraft.bundle.fullness", getBundleOccupancy(stack), this.maxOccupancy).formatted(Formatting.GRAY));
	}
	
	/**
	 * Triggered when the bundle is destroyed in the world by a fire or lava.
	 * @param entity The destroyed bundle as an <i>ItemEntity</i>.
	 */
	@Override
	public void onItemEntityDestroyed(ItemEntity entity) {
		ItemUsage.spawnItemContents(entity, getBundledStacks(entity.getStack()));
	}
	
	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}
	
	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}
	
	private void playDropContentsSound(Entity entity) {
		entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
	}
}
