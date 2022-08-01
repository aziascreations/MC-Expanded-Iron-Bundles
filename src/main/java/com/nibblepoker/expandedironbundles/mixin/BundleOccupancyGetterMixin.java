package com.nibblepoker.expandedironbundles.mixin;

import com.nibblepoker.expandedironbundles.items.CustomBundleItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public class BundleOccupancyGetterMixin {
	// Injects the condition contained in the following function at the start of "BundleItem.getItemOccupancy()" in
	//  order to allow it to detect and support other bundles whose item class extends the original "BundleItem" class
	//  since the original condition "if (stack.isOf(Items.BUNDLE))" only works with the original class.
	
	// Function's bytecode signature:
	//  private static int getItemOccupancy(ItemStack stack) => getItemOccupancy(Lnet/minecraft/item/ItemStack;)I
	
	// You also need to specify the full path for the classes otherwise Fabric can't resolve them in the final build.
	
	//  See: https://fabricmc.net/wiki/tutorial:mixin_injects
	@Inject(method = "getItemOccupancy(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
	private static void getItemOccupancy(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.getItem() instanceof BundleItem) {
			cir.setReturnValue(
					CustomBundleItem.NESTED_BUNDLE_BASE_OCCUPANCY + CustomBundleItem.getBundleOccupancy(stack)
			);
		}
	}
}
