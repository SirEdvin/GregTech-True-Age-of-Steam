package site.siredvin.gttruesteam.mixins;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Invoker("dropFromLootTable")
    void callDropFromLootTable(DamageSource damageSource, boolean hitByPlayer);

    @Invoker("dropCustomDeathLoot")
    void callDropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer);
}
