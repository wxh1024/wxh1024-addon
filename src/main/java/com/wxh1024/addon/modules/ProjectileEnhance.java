package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class ProjectileEnhance extends Module { public ProjectileEnhance(){super(Wxh1024Addon.WXH1024,"projectile-enhance","Keep ordinary projectile use responsive.");} @EventHandler private void tick(TickEvent.Post e){} }
