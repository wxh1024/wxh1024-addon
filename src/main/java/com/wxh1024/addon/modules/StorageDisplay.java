package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
public class StorageDisplay extends Module {
 public StorageDisplay(){super(Wxh1024Addon.WXH1024,"storage-display","Highlight nearby chest storage blocks.");}
 @EventHandler private void render(Render3DEvent e){if(mc.world==null||mc.player==null)return;for(BlockEntity b:mc.world.getBlockEntities())if(b instanceof ChestBlockEntity&&b.getPos().getSquaredDistance(mc.player.getX(),mc.player.getY(),mc.player.getZ())<1024)e.renderer.box(b.getPos(),Color.ORANGE,Color.ORANGE,ShapeMode.Lines,0);}
}
