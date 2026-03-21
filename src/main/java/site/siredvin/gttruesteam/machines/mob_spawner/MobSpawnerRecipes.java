package site.siredvin.gttruesteam.machines.mob_spawner;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import lombok.Getter;
import site.siredvin.gttruesteam.GTTrueSteam;
import site.siredvin.gttruesteam.TrueSteamMaterials;
import site.siredvin.gttruesteam.TrueSteamRecipeTypes;
import site.siredvin.gttruesteam.recipe.condition.SpawnerMobCondition;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLERY_RECIPES;

/**
 * Defines all supported mob types for the Mob Spawner multiblock and generates
 * their recipes automatically.
 *
 * <p>Each mob has:
 * <ul>
 *   <li>A catalyst item (common loot to avoid recipe conflicts)</li>
 *   <li>A mob essence fluid produced in the spawner machine</li>
 *   <li>Loot items produced by distilling the essence</li>
 *   <li>Whether it is a nether mob (converts Distilled Water → Hellish Water)</li>
 *   <li>Optional extra fluid production (e.g., Lava for Magma Cubes, Blaze for Blazes)</li>
 * </ul>
 */
public class MobSpawnerRecipes {

    // Base output amount of essence per recipe (in mB)
    private static final int BASE_ESSENCE_AMOUNT = 1000;
    // Base EU/t consumption
    private static final int BASE_EU = 30;
    // Base duration in ticks
    private static final int BASE_DURATION = 400;
    // Distilled water consumed by nether mob recipes (mB)
    private static final int DISTILLED_WATER_AMOUNT = 1000;
    // Hellish water produced by nether mob recipes (mB)
    private static final int HELLISH_WATER_AMOUNT = 1000;
    // Extra fluid produced by special mobs (mB)
    private static final int EXTRA_FLUID_AMOUNT = 500;
    // Essence amount required in distillery recipe (mB)
    private static final int DISTILLERY_ESSENCE_INPUT = 500;

    // ===== Loot entry record =====

    public record LootEntry(ItemStack item, int chance, int maxChance) {

        /** 100% chance loot */
        public static LootEntry always(ItemStack item) {
            return new LootEntry(item, 10000, 0);
        }

        /** Chanced loot (chance out of 10000) */
        public static LootEntry chanced(ItemStack item, int chance, int maxChance) {
            return new LootEntry(item, chance, maxChance);
        }
    }

    /**
     * Describes a mob supported by the Mob Spawner multiblock.
     */
    @Getter
    public static class SupportedMob {

        private final ResourceLocation entityType;
        private final ItemStack catalyst;
        private final Material essenceMaterial;
        private final List<LootEntry> loot;
        private final boolean isNether;
        /** If non-null, this fluid is also produced by the spawner recipe (positive loop). */
        @Nullable
        private final Material extraFluid;

        private SupportedMob(
                ResourceLocation entityType,
                ItemStack catalyst,
                Material essenceMaterial,
                List<LootEntry> loot,
                boolean isNether,
                @Nullable Material extraFluid) {
            this.entityType = entityType;
            this.catalyst = catalyst;
            this.essenceMaterial = essenceMaterial;
            this.loot = loot;
            this.isNether = isNether;
            this.extraFluid = extraFluid;
        }

        public static Builder builder(String entityTypeId, Material essenceMaterial) {
            return new Builder(new ResourceLocation(entityTypeId), essenceMaterial);
        }

        public static class Builder {

            private final ResourceLocation entityType;
            private final Material essenceMaterial;
            private ItemStack catalyst;
            private final List<LootEntry> loot = new ArrayList<>();
            private boolean isNether = false;
            @Nullable
            private Material extraFluid = null;

            private Builder(ResourceLocation entityType, Material essenceMaterial) {
                this.entityType = entityType;
                this.essenceMaterial = essenceMaterial;
            }

            public Builder catalyst(ItemStack item) {
                this.catalyst = item;
                return this;
            }

            public Builder loot(ItemStack item) {
                this.loot.add(LootEntry.always(item));
                return this;
            }

            public Builder loot(ItemStack item, int chance, int maxChance) {
                this.loot.add(LootEntry.chanced(item, chance, maxChance));
                return this;
            }

            public Builder nether() {
                this.isNether = true;
                return this;
            }

            /** Fluid GT material (e.g. GTMaterials.Blaze) to also output in the spawner recipe. */
            public Builder extraFluid(Material material) {
                this.extraFluid = material;
                return this;
            }

            public SupportedMob build() {
                if (catalyst == null) throw new IllegalStateException("Catalyst not set for " + entityType);
                if (loot.isEmpty()) throw new IllegalStateException("No loot defined for " + entityType);
                return new SupportedMob(entityType, catalyst, essenceMaterial, loot, isNether, extraFluid);
            }
        }
    }

    // ===== Supported mob registry =====

    public static final List<SupportedMob> MOBS = new ArrayList<>();

    static {
        // === Overworld mobs ===
        MOBS.add(SupportedMob.builder("minecraft:zombie", TrueSteamMaterials.ZombieEssence)
                .catalyst(new ItemStack(Items.ROTTEN_FLESH))
                .loot(new ItemStack(Items.ROTTEN_FLESH, 2))
                .build());

        MOBS.add(SupportedMob.builder("minecraft:skeleton", TrueSteamMaterials.SkeletonEssence)
                .catalyst(new ItemStack(Items.BONE))
                .loot(new ItemStack(Items.BONE, 2))
                .loot(new ItemStack(Items.ARROW, 2), 7000, 500)
                .build());

        MOBS.add(SupportedMob.builder("minecraft:spider", TrueSteamMaterials.SpiderEssence)
                .catalyst(new ItemStack(Items.SPIDER_EYE))
                .loot(new ItemStack(Items.STRING, 2))
                .loot(new ItemStack(Items.SPIDER_EYE, 1), 5000, 500)
                .build());

        MOBS.add(SupportedMob.builder("minecraft:creeper", TrueSteamMaterials.CreeperEssence)
                .catalyst(new ItemStack(Items.GUNPOWDER))
                .loot(new ItemStack(Items.GUNPOWDER, 2))
                .build());

        MOBS.add(SupportedMob.builder("minecraft:enderman", TrueSteamMaterials.EndermanEssence)
                .catalyst(new ItemStack(Items.ENDER_PEARL))
                .loot(new ItemStack(Items.ENDER_PEARL, 2))
                .build());

        // === Nether mobs ===
        MOBS.add(SupportedMob.builder("minecraft:blaze", TrueSteamMaterials.BlazeEssence)
                .catalyst(new ItemStack(Items.BLAZE_ROD))
                .loot(new ItemStack(Items.BLAZE_ROD, 2))
                .nether()
                .extraFluid(Blaze)       // produces Blaze fluid (positive loop)
                .build());

        MOBS.add(SupportedMob.builder("minecraft:wither_skeleton", TrueSteamMaterials.WitherSkeletonEssence)
                .catalyst(new ItemStack(Items.COAL))
                .loot(new ItemStack(Items.COAL, 2))
                .loot(new ItemStack(Items.WITHER_SKELETON_SKULL, 1), 800, 200)
                .nether()
                .build());

        MOBS.add(SupportedMob.builder("minecraft:ghast", TrueSteamMaterials.GhastEssence)
                .catalyst(new ItemStack(Items.GUNPOWDER))
                .loot(new ItemStack(Items.GUNPOWDER, 2))
                .loot(new ItemStack(Items.GHAST_TEAR, 1), 5000, 500)
                .nether()
                .build());

        MOBS.add(SupportedMob.builder("minecraft:magma_cube", TrueSteamMaterials.MagmaSlimeEssence)
                .catalyst(new ItemStack(Items.MAGMA_CREAM))
                .loot(new ItemStack(Items.MAGMA_CREAM, 2))
                .nether()
                .extraFluid(Lava)        // produces Lava (positive loop)
                .build());

        MOBS.add(SupportedMob.builder("minecraft:zombified_piglin", TrueSteamMaterials.ZombiePiglinEssence)
                .catalyst(new ItemStack(Items.GOLD_NUGGET))
                .loot(new ItemStack(Items.GOLD_NUGGET, 4))
                .loot(new ItemStack(Items.GOLD_INGOT, 1), 3000, 300)
                .nether()
                .build());
    }

    // ===== Recipe generation =====

    public static void registerRecipes(Consumer<FinishedRecipe> provider) {
        for (SupportedMob mob : MOBS) {
            registerSpawnerRecipe(provider, mob);
            registerDistilleryRecipe(provider, mob);
        }
    }

    private static void registerSpawnerRecipe(Consumer<FinishedRecipe> provider, SupportedMob mob) {
        String recipeName = "mob_spawner_" + sanitize(mob.getEntityType().toString());

        var builder = TrueSteamRecipeTypes.MOB_SPAWNER.recipeBuilder(GTTrueSteam.id(recipeName))
                // Sword: not consumed (durability handled by MobSpawnerRecipeLogic)
                .notConsumable(Tags.Items.TOOLS_SWORDS)
                // Catalyst: consumed
                .inputItems(mob.getCatalyst())
                // Output: mob essence fluid
                .outputFluids(mob.getEssenceMaterial().getFluid(BASE_ESSENCE_AMOUNT))
                // Mob type condition: restricts recipe to matching spawner
                .addCondition(new SpawnerMobCondition(mob.getEntityType()))
                .EUt(BASE_EU)
                .duration(BASE_DURATION);

        // Nether mobs: also consume distilled water and output hellish water
        if (mob.isNether()) {
            builder.inputFluids(DistilledWater.getFluid(DISTILLED_WATER_AMOUNT));
            builder.outputFluids(TrueSteamMaterials.HellishWater.getFluid(HELLISH_WATER_AMOUNT));
        }

        // Special mobs: also produce an extra fluid (positive loop)
        if (mob.getExtraFluid() != null) {
            builder.outputFluids(mob.getExtraFluid().getFluid(EXTRA_FLUID_AMOUNT));
        }

        builder.save(provider);
    }

    private static void registerDistilleryRecipe(Consumer<FinishedRecipe> provider, SupportedMob mob) {
        String recipeName = "distill_" + sanitize(mob.getEntityType().toString()) + "_essence";
        var builder = DISTILLERY_RECIPES.recipeBuilder(GTTrueSteam.id(recipeName))
                .inputFluids(mob.getEssenceMaterial().getFluid(DISTILLERY_ESSENCE_INPUT))
                .EUt(BASE_EU)
                .duration(200);

        // Add all loot entries
        for (LootEntry loot : mob.getLoot()) {
            if (loot.chance() >= 10000) {
                builder.outputItems(loot.item());
            } else {
                builder.chancedOutput(loot.item(), loot.chance(), loot.maxChance());
            }
        }

        builder.save(provider);
    }

    /** Converts entity type string (e.g. "minecraft:zombie") to a safe recipe ID component. */
    private static String sanitize(String entityTypeId) {
        return entityTypeId.replace(':', '_').replace('/', '_');
    }
}
