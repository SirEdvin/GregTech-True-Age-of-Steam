package site.siredvin.gttruesteam;

import net.minecraft.advancements.CriteriaTriggers;

import site.siredvin.gttruesteam.criteria.InfernalMaintainceCriterion;
import site.siredvin.gttruesteam.criteria.PerfectConditionCriterion;

public class TrueSteamCriteria {

    public static final PerfectConditionCriterion PERFECT_CONDITION = CriteriaTriggers.register(
            new PerfectConditionCriterion());
    public static final InfernalMaintainceCriterion INFERNAL_MAINTENANCE = CriteriaTriggers.register(
            new InfernalMaintainceCriterion());

    public static void init() {}
}
