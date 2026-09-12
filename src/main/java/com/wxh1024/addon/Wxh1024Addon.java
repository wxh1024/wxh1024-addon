package com.wxh1024.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.wxh1024.addon.commands.C3uCommand;
import com.wxh1024.addon.commands.TpCommand;
import com.wxh1024.addon.commands.TpaCommand;
import com.wxh1024.addon.modules.BadPacketsFix;
import com.wxh1024.addon.modules.ControlifyCompat;
import com.wxh1024.addon.modules.DropTest;
import com.wxh1024.addon.modules.ElytraExtra;
import com.wxh1024.addon.modules.ElytraFlight;
import com.wxh1024.addon.modules.ElytraFlightLegit;
import com.wxh1024.addon.modules.ElytraGrimAccelerate;
import com.wxh1024.addon.modules.ElytraJump;
import com.wxh1024.addon.modules.ElytraSlowFall;
import com.wxh1024.addon.modules.ItemFrameChestOpener;
import com.wxh1024.addon.modules.Home;
import me.matl114.SlimefunHelper;

public class Wxh1024Addon extends MeteorAddon {
    public static final String MOD_ID = "wxh1024-addon";
    public static final Category WXH1024 = new Category("wxh1024");

    @Override
    public void onInitialize() {
        SlimefunHelper.initialize();

        Modules.get().add(new Home());
        Modules.get().add(new ItemFrameChestOpener());
        Modules.get().add(new ControlifyCompat());
        Modules.get().add(new DropTest());
        Modules.get().add(new ElytraFlight());
        Modules.get().add(new ElytraExtra());
        Modules.get().add(new ElytraGrimAccelerate());
        Modules.get().add(new ElytraFlightLegit());
        Modules.get().add(new ElytraJump());
        Modules.get().add(new ElytraSlowFall());
        Modules.get().add(new BadPacketsFix());

        Commands.add(new C3uCommand());
        Commands.add(new TpCommand());
        Commands.add(new TpaCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(WXH1024);
    }

    @Override
    public String getPackage() {
        return "com.wxh1024.addon";
    }
}
