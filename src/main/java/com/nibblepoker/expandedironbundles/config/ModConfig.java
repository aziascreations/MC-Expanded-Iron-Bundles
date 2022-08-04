package com.nibblepoker.expandedironbundles.config;

import com.nibblepoker.commons.config.Config;
import org.slf4j.Logger;

public class ModConfig extends Config {
	public ModConfig(Logger logger, String namespace) {
		super(logger, namespace);
		
		// Adding default config fields and values
		this.registerStringField("__WARNING__", "This config file is not currently used by the mod !");
		
		// Storage size for each bundle
		this.registerIntegerField("bundleSizeVanilla", 64);
		this.registerIntegerField("bundleSizeCopperIngot", 128);
		this.registerIntegerField("bundleSizeIronIngot", 192);
		this.registerIntegerField("bundleSizeGoldIngot", 256);
		this.registerIntegerField("bundleSizeDiamondIngot", 320);
		this.registerIntegerField("bundleSizeObsidianIngot", 384);
		this.registerIntegerField("bundleSizeNetheriteChunk", 448);
		this.registerIntegerField("bundleSizeNetheriteIngot", 512);
		
		// Max upgrade count for each bundle
		this.registerIntegerField("bundleUpgradeCountVanilla", 0);
		this.registerIntegerField("bundleUpgradeCountCopperIngot", 1);
		this.registerIntegerField("bundleUpgradeCountIronIngot", 1);
		this.registerIntegerField("bundleUpgradeCountGoldIngot", 1);
		this.registerIntegerField("bundleUpgradeCountDiamondIngot", 2);
		this.registerIntegerField("bundleUpgradeCountObsidianIngot", 2);
		this.registerIntegerField("bundleUpgradeCountNetheriteChunk", 2);
		this.registerIntegerField("bundleUpgradeCountNetheriteIngot", 3);
		
		// Recipe toggles for the vanilla bundle
		this.registerBooleanField("addBundleRabbitHideRecipe", true);
		this.registerBooleanField("addBundleCowHideRecipe", false);
		this.registerBooleanField("addBundleWoolRecipe", false);
		
		// Misc toggles
		this.registerBooleanField("makeNetheriteIngotBundleFireproof", true);
		this.registerBooleanField("addCreativeItemGroup", true);
		// this.registerBooleanField("addDebugJoystickToItemRegistry", false);  // May cause issues if the client and server have different values !
	}
}
