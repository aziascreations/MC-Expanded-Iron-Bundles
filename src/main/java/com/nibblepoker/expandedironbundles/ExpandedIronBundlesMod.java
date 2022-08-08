package com.nibblepoker.expandedironbundles;

import com.mojang.logging.LogUtils;
import com.nibblepoker.expandedironbundles.items.BundleFilterItem;
import com.nibblepoker.expandedironbundles.items.DebuggingItem;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(ExpandedIronBundlesMod.MOD_ID)
public class ExpandedIronBundlesMod {
	// Misc Stuff
	public static final String MOD_ID = "expandedironbundles";
	public static final Logger LOGGER = LogUtils.getLogger();
	
	// Registers
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	
	// Item Group
	public static final CreativeModeTab BUNDLE_ITEM_TAB = new CreativeModeTab(ExpandedIronBundlesMod.MOD_ID + ".main") {
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(ExpandedIronBundlesMod.BUNDLE_FILTER.get());
		}
	};
	
	// Items
	public static final RegistryObject<Item> BUNDLE_FILTER = ITEMS.register(
			"bundle_filter", () -> new BundleFilterItem(new Item.Properties().tab(ExpandedIronBundlesMod.BUNDLE_ITEM_TAB))
	);
	public static final RegistryObject<Item> DEBUG_JOYSTICK = ITEMS.register(
			"debug_joystick", () -> new DebuggingItem(
					new Item.Properties().tab(ExpandedIronBundlesMod.BUNDLE_ITEM_TAB).fireResistant().rarity(Rarity.EPIC)
			)
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
	
	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			LOGGER.info("Registering \"filled\" predicate for bundles and filters...");
			// TODO: This !
		}
	}
}
