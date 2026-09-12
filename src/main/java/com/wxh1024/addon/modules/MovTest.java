package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class MovTest extends Module { public MovTest(){super(Wxh1024Addon.WXH1024,"mov-test","Movement diagnostics.");} @Override public String getInfoString(){return mc.player==null?"":String.format("%.2f",mc.player.getVelocity().length());}}
