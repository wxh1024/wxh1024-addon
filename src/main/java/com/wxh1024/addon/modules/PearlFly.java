package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
public class PearlFly extends Module {
 private final SettingGroup sg=settings.getDefaultGroup();
 private final Setting<Double> boost=sg.add(new DoubleSetting.Builder().name("boost").defaultValue(.08).min(0).sliderMax(1).build());
 private final Setting<Boolean> whileUsing=sg.add(new BoolSetting.Builder().name("while-using").defaultValue(true).build());
 public PearlFly(){super(Wxh1024Addon.WXH1024,"pearl-fly","Maintains controlled vertical motion during pearl phase.");}
 @EventHandler private void tick(TickEvent.Post e){if(mc.player==null||mc.world==null)return;if(whileUsing.get()&&(!mc.player.isUsingItem()||!mc.player.getActiveItem().isOf(Items.ENDER_PEARL)))return;var v=mc.player.getVelocity();mc.player.setVelocity(v.x,v.y+boost.get(),v.z);}
}
