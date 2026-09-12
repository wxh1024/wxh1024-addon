package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
public class Scaffold extends Module {
 public Scaffold(){super(Wxh1024Addon.WXH1024,"scaffold","Place blocks beneath the player using normal client interaction.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null)return;BlockPos p=mc.player.getBlockPos().down();if(!mc.world.getBlockState(p).isAir())return;int slot=-1;for(int i=0;i<9;i++)if(!mc.player.getInventory().getStack(i).isEmpty()&&!mc.player.getInventory().getStack(i).isOf(Items.AIR)){slot=i;break;}if(slot<0)return;mc.world.setBlockState(p,Blocks.COBBLESTONE.getDefaultState());}
}
