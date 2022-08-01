package com.nibblepoker.expandedironbundles;

import com.nibblepoker.expandedironbundles.items.CustomBundleItem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

public class ExpandedIronBundlesModClient implements ClientModInitializer {
	// Misc stuff
	public static final Logger LOGGER = ExpandedIronBundlesMod.LOGGER;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Registering \"filled\" predicate for bundles...");
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.COPPER_INGOT_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.IRON_INGOT_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.GOLD_INGOT_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.DIAMOND_INGOT_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.NETHERITE_CHUNK_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
		
		ModelPredicateProviderRegistry.register(
				ExpandedIronBundlesMod.NETHERITE_INGOT_BUNDLE, new Identifier("filled"),
				(stack, world, entity, seed) -> CustomBundleItem.getAmountFilled(stack)
		);
	}
}
