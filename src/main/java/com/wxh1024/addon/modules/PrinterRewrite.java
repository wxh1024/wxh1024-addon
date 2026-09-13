package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
public class PrinterRewrite extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Integer> radius=sg.add(new IntSetting.Builder().name("radius").defaultValue(2).min(1).sliderMax(5).build()); private final Setting<Integer> delay=sg.add(new IntSetting.Builder().name("delay").defaultValue(2).min(1).sliderMax(10).build()); private int ticks;
 public PrinterRewrite(){super(Wxh1024Addon.WXH1024,"printer-rewrite","Prints blocks into replaceable positions ahead of the player.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null||++ticks<delay.get())return;ticks=0;int slot=-1;for(int i=0;i<9;i++)if(mc.player.getInventory().getStack(i).getItem() instanceof BlockItem){slot=i;break;}if(slot<0)return;BlockPos origin=mc.player.getBlockPos();int old=mc.player.getInventory().getSelectedSlot();mc.player.getInventory().setSelectedSlot(slot);for(int x=-radius.get();x<=radius.get();x++)for(int z=-radius.get();z<=radius.get();z++){BlockPos p=origin.add(x,-1,z);if(!mc.world.getBlockState(p).isReplaceable())continue;BlockPos support=p.down();if(mc.world.getBlockState(support).isReplaceable())continue;mc.interactionManager.interactBlock(mc.player,Hand.MAIN_HAND,new BlockHitResult(support.toCenterPos(),Direction.UP,support,false));mc.player.getInventory().setSelectedSlot(old);return;}mc.player.getInventory().setSelectedSlot(old);}
}
