package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
public class AutoSurround extends Module {
 public AutoSurround(){super(Wxh1024Addon.WXH1024,"auto-surround","Place obsidian around the player.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null)return;int slot=-1;for(int i=0;i<9;i++)if(mc.player.getInventory().getStack(i).isOf(Items.OBSIDIAN)){slot=i;break;}if(slot<0)return;int old=mc.player.getInventory().getSelectedSlot();mc.player.getInventory().setSelectedSlot(slot);BlockPos base=mc.player.getBlockPos();for(BlockPos p:new BlockPos[]{base.north(),base.south(),base.east(),base.west()})if(mc.world.getBlockState(p).isAir())mc.world.setBlockState(p,Blocks.OBSIDIAN.getDefaultState());mc.player.getInventory().setSelectedSlot(old);}
}
