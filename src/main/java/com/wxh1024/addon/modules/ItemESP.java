package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.ItemEntity;
public class ItemESP extends Module {
 public ItemESP(){super(Wxh1024Addon.WXH1024,"item-esp","Highlight dropped item entities.");}
 @EventHandler private void render(Render3DEvent e){if(mc.world==null||mc.player==null)return;for(ItemEntity item:mc.world.getEntitiesByClass(ItemEntity.class,mc.player.getBoundingBox().expand(32),x->true))e.renderer.box(item.getBoundingBox(),Color.GREEN,Color.GREEN,ShapeMode.Lines,0);}
}
