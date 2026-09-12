package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
public class NameTag extends Module {
 public NameTag(){super(Wxh1024Addon.WXH1024,"name-tag","Highlight nearby player name-tag targets.");}
 @EventHandler private void render(Render3DEvent e){if(mc.world==null||mc.player==null)return;for(PlayerEntity p:mc.world.getPlayers())if(p!=mc.player)e.renderer.box(p.getBoundingBox(),Color.YELLOW,Color.YELLOW,ShapeMode.Lines,0);}
}
