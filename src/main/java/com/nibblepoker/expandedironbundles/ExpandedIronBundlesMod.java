package com.nibblepoker.expandedironbundles;

import com.mojang.logging.LogUtils;
import com.nibblepoker.expandedironbundles.items.BundleFilterItem;
import com.nibblepoker.expandedironbundles.items.DebuggingItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ExpandedIronBundlesMod.MOD_ID)
public class ExpandedIronBundlesMod {
	// Misc Stuff
	public static final String MOD_ID = "expandedironbundles";
	
	public static final Logger LOGGER = LogUtils.getLogger();
	
	// Registers
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	
	// Item Group
	
	// Items
	public static final RegistryObject<Item> BUNDLE_FILTER = ITEMS.register(
			"bundle_filter", () -> new BundleFilterItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
	);
	public static final RegistryObject<Item> DEBUG_JOYSTICK = ITEMS.register(
			"debug_joystick", () -> new DebuggingItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC))
	);
	
	
	public ExpandedIronBundlesMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		// Register the "ExpandedIronBundlesMod.commonSetup()" function for mod loading
		modEventBus.addListener(this::commonSetup);
		
		// Register the Deferred Register to the mod event bus so items get registered
		ITEMS.register(modEventBus);
		
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void commonSetup(final FMLCommonSetupEvent event) {
		LOGGER.info("HELLO FROM COMMON SETUP");
	}
}
