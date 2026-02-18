package site.siredvin.gttruesteam;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import site.siredvin.gttruesteam.client.CoatingShrineRenderer;

public class ModClientProxy extends ModCommonProxy {

    public ModClientProxy() {
        DynamicRenderManager.register(GTTrueSteam.id("coating_machine_renderer"), CoatingShrineRenderer.TYPE);
    }
}
