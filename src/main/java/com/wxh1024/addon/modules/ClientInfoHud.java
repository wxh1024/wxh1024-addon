package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class ClientInfoHud extends Module {
    public ClientInfoHud() { super(Wxh1024Addon.WXH1024, "client-info-hud", "Display safe client, position and server information."); }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (mc.player == null || mc.world == null) return;
        String server = mc.getCurrentServerEntry() == null ? "singleplayer" : mc.getCurrentServerEntry().address;
        String text = String.format("XYZ %.1f %.1f %.1f | %s | %d entities",
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), server, mc.world.getOtherEntities(mc.player,
                        mc.player.getBoundingBox().expand(32)).size());
        event.drawContext.drawTextWithShadow(mc.textRenderer, text, 4, 4, 0xFFFFFFFF);
    }
}
