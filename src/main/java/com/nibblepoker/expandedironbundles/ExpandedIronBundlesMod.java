package com.nibblepoker.expandedironbundles;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ExpandedIronBundlesMod.MOD_ID)
public class ExpandedIronBundlesMod {
	// Misc Stuff
	public static final String MOD_ID = "expandedironbundles";
	
	public static final Logger LOGGER = LogUtils.getLogger();
	
	
	public ExpandedIronBundlesMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		// Register the "ExpandedIronBundlesMod.commonSetup()" function for mod loading
		modEventBus.addListener(this::commonSetup);
		
		
	}
	
	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("HELLO FROM COMMON SETUP");
	}
}
