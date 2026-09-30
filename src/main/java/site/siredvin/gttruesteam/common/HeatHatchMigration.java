package site.siredvin.gttruesteam.common;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import site.siredvin.gttruesteam.TrueSteamMachines;

import java.util.Set;

@Mod.EventBusSubscriber(modid = "gttruesteam")
public final class HeatHatchMigration {
    private static final Set<String> LEGACY = Set.of("hv_heat_hatch", "ev_heat_hatch", "iv_heat_hatch", "luv_heat_hatch");

    @SubscribeEvent
    public static void remap(MissingMappingsEvent event) {
        for (var mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, "gttruesteam")) {
            if (LEGACY.contains(mapping.getKey().getPath())) mapping.remap(TrueSteamMachines.HEAT_HATCH.getBlock());
        }
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, "gttruesteam")) {
            if (LEGACY.contains(mapping.getKey().getPath())) mapping.remap(TrueSteamMachines.HEAT_HATCH.asStack().getItem());
        }
        for (var mapping : event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, "gttruesteam")) {
            if (LEGACY.contains(mapping.getKey().getPath())) mapping.remap(TrueSteamMachines.HEAT_HATCH.getBlockEntityType());
        }
    }
}
