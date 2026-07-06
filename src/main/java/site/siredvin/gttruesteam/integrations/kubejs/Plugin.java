package site.siredvin.gttruesteam.integrations.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class Plugin extends KubeJSPlugin {

    @Override
    public void init() {
        RegistryInfo.BLOCK.addType("gttruesteam:coil", CoolingCoilBlockBuilder.class, CoolingCoilBlockBuilder::new);
    }
}
