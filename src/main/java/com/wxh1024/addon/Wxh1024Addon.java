package com.wxh1024.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.wxh1024.addon.modules.ControlifyCompat;
import com.wxh1024.addon.modules.ItemFrameChestOpener;
import com.wxh1024.addon.modules.Home;

public class Wxh1024Addon extends MeteorAddon {
    public static final String MOD_ID = "wxh1024-addon";
    public static final Category WXH1024 = new Category("wxh1024");

    @Override
    public void onInitialize() {
        Modules.get().add(new Home());
        Modules.get().add(new ItemFrameChestOpener());
        Modules.get().add(new ControlifyCompat());
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
