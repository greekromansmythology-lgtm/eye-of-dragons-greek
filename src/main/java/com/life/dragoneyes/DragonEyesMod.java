package com.life.dragoneyes;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = DragonEyesMod.MODID,
        name = DragonEyesMod.NAME,
        version = DragonEyesMod.VERSION,
        acceptedMinecraftVersions = "[1.12.2]"
)
@Mod.EventBusSubscriber(modid = DragonEyesMod.MODID)
public class DragonEyesMod {
    public static final String MODID = "dragoneyes";
    public static final String NAME = "Dragon Eyes";
    public static final String VERSION = "1.0.0";

    public static Item FIRE_EYE;
    public static Item ICE_EYE;
    public static Item LIGHTNING_EYE;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        FIRE_EYE = new ItemDragonEye("fire_eye", DragonType.FIRE);
        ICE_EYE = new ItemDragonEye("ice_eye", DragonType.ICE);
        LIGHTNING_EYE = new ItemDragonEye("lightning_eye", DragonType.LIGHTNING);

        event.getRegistry().registerAll(FIRE_EYE, ICE_EYE, LIGHTNING_EYE);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // leave empty for now
    }

    // ✅ CLIENT ONLY: register item models so they don't render purple/black
    @Mod.EventBusSubscriber(modid = DragonEyesMod.MODID, value = Side.CLIENT)
    public static class ClientModels {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            registerModel(FIRE_EYE);
            registerModel(ICE_EYE);
            registerModel(LIGHTNING_EYE);
        }

        private static void registerModel(Item item) {
            if (item == null || item.getRegistryName() == null) return;
            ModelLoader.setCustomModelResourceLocation(
                    item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory")
            );
        }
    }
}
