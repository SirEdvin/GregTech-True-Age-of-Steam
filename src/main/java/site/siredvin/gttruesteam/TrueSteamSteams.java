package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import site.siredvin.gttruesteam.common.SteamRecord;

import java.util.ArrayList;
import java.util.List;

public class TrueSteamSteams {

    public static List<SteamRecord> STEAMS = new ArrayList<>();

    public static SteamRecord SUPERHOT = register(new SteamRecord.Builder()
            .baseName("superhot").criticalName("supercritical").water(GTMaterials.DistilledWater)
            .density(2.1).compression(130, 100).build());

    public static SteamRecord HELLISH = register(new SteamRecord.Builder()
            .baseName("hellish").criticalName("most_hellish").water(TrueSteamMaterials.HellishWater)
            .density(2.5).compression(130, 100).build());

    public static SteamRecord register(SteamRecord record) {
        STEAMS.add(record);
        return record;
    }

    public static void sayHi() {}
}
