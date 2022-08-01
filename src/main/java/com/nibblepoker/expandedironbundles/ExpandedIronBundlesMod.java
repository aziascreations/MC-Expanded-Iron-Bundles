package com.nibblepoker.expandedironbundles;

import com.nibblepoker.expandedironbundles.items.CustomBundleItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpandedIronBundlesMod implements ModInitializer {
	// Misc stuff
	public static final Logger LOGGER = LoggerFactory.getLogger("ExpandedIronBundles");
	
	// Item Group
	public static final ItemGroup BUNDLE_ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier("expandedironbundles", "main"),
			() -> new ItemStack(ExpandedIronBundlesMod.NETHERITE_INGOT_BUNDLE)
	);
	
	// Items
	public static final Item COPPER_INGOT_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP), 128
	);
	public static final Item IRON_INGOT_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP), 256
	);
	public static final Item GOLD_INGOT_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP), 384
	);
	public static final Item DIAMOND_INGOT_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP), 512
	);
	//public static final Item OBSIDIAN_INGOT_BUNDLE  = new CustomBundleItem(
	//		new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP), 768
	//);
	public static final Item NETHERITE_CHUNK_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP)
					.rarity(Rarity.UNCOMMON), 896
	);
	public static final Item NETHERITE_INGOT_BUNDLE = new CustomBundleItem(
			new FabricItemSettings().group(ExpandedIronBundlesMod.BUNDLE_ITEM_GROUP)
					.fireproof().rarity(Rarity.UNCOMMON), 1024
	);
	
	@Override
	public void onInitialize() {
		LOGGER.info("Registering items...");
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "copper_ingot_bundle"), COPPER_INGOT_BUNDLE);
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "iron_ingot_bundle"), IRON_INGOT_BUNDLE);
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "gold_ingot_bundle"), GOLD_INGOT_BUNDLE);
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "diamond_ingot_bundle"), DIAMOND_INGOT_BUNDLE);
		//Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "obsidian_ingot_bundle"), OBSIDIAN_INGOT_BUNDLE);
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "netherite_chunk_bundle"), NETHERITE_CHUNK_BUNDLE);
		Registry.register(Registry.ITEM, new Identifier("expandedironbundles", "netherite_ingot_bundle"), NETHERITE_INGOT_BUNDLE);
	}
}
