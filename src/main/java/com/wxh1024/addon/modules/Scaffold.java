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
public class Scaffold extends Module {
 private final SettingGroup sg=settings.getDefaultGroup(); private final Setting<Integer> delay=sg.add(new IntSetting.Builder().name("delay").defaultValue(1).min(1).sliderMax(10).build()); private final Setting<Integer> range=sg.add(new IntSetting.Builder().name("range").defaultValue(2).min(1).sliderMax(4).build()); private int tick;
 public Scaffold(){super(Wxh1024Addon.WXH1024,"scaffold","Places held blocks beneath and around the player.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null||mc.interactionManager==null||++tick<delay.get())return;tick=0;BlockPos base=mc.player.getBlockPos().down();if(!mc.world.getBlockState(base).isReplaceable())return;int slot=-1;for(int i=0;i<9;i++)if(mc.player.getInventory().getStack(i).getItem() instanceof BlockItem){slot=i;break;}if(slot<0)return;int old=mc.player.getInventory().getSelectedSlot();mc.player.getInventory().setSelectedSlot(slot);for(Direction d:Direction.values()){BlockPos support=base.offset(d);if(!mc.world.getBlockState(support).isReplaceable()){var hit=new BlockHitResult(support.toCenterPos(),d.getOpposite(),support,false);mc.interactionManager.interactBlock(mc.player,Hand.MAIN_HAND,hit);break;}}mc.player.getInventory().setSelectedSlot(old);}
}
