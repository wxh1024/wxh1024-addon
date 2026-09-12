package com.wxh1024.addon.modules;
import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
public class PrinterRewrite extends Module { public PrinterRewrite(){super(Wxh1024Addon.WXH1024,"printer-rewrite","Build printer compatibility module.");} @EventHandler private void tick(TickEvent.Post e){} }
