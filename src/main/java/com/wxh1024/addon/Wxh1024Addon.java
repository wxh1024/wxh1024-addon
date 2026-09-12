package com.wxh1024.addon;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import com.wxh1024.addon.commands.C3uCommand;
import com.wxh1024.addon.commands.TpCommand;
import com.wxh1024.addon.commands.TpaCommand;
import com.wxh1024.addon.commands.SlimefunCommand;
import com.wxh1024.addon.commands.SlimefunHelperCommand;
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
import com.wxh1024.addon.modules.SlimefunItemInspector;
import com.wxh1024.addon.modules.SlimefunTaskScheduler;
import com.wxh1024.addon.modules.Home;
import com.wxh1024.addon.modules.EntitySearchNotifier;
import com.wxh1024.addon.modules.TotemSafetyNotifier;
import com.wxh1024.addon.modules.ChatFilter;
import com.wxh1024.addon.modules.EntityBoxESP;
import com.wxh1024.addon.modules.ServerInfoNotifier;
import com.wxh1024.addon.modules.ClientInfoHud;
import com.wxh1024.addon.modules.ChatReceiveFilter;
import com.wxh1024.addon.modules.AutoLogoutSafety;
import com.wxh1024.addon.modules.Sprint;
import com.wxh1024.addon.modules.SwimControl;
import com.wxh1024.addon.modules.StepHeight;
import com.wxh1024.addon.modules.NoSlowDown;
import com.wxh1024.addon.modules.NoFall;
import com.wxh1024.addon.modules.GuiMove;
import com.wxh1024.addon.modules.MoveTimer;

public class Wxh1024Addon extends MeteorAddon {
    public static final String MOD_ID = "wxh1024-addon";
    public static final Category WXH1024 = new Category("wxh1024");

    @Override
    public void onInitialize() {
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
        Modules.get().add(new SlimefunItemInspector());
        Modules.get().add(new SlimefunTaskScheduler());
        Modules.get().add(new EntitySearchNotifier());
        Modules.get().add(new TotemSafetyNotifier());
        Modules.get().add(new ChatFilter());
        Modules.get().add(new EntityBoxESP());
        Modules.get().add(new ServerInfoNotifier());
        Modules.get().add(new ClientInfoHud());
        Modules.get().add(new ChatReceiveFilter());
        Modules.get().add(new AutoLogoutSafety());
        Modules.get().add(new Sprint());
        Modules.get().add(new SwimControl());
        Modules.get().add(new StepHeight());
        Modules.get().add(new NoSlowDown());
        Modules.get().add(new NoFall());
        Modules.get().add(new GuiMove());
        Modules.get().add(new MoveTimer());

        Commands.add(new C3uCommand());
        Commands.add(new TpCommand());
        Commands.add(new TpaCommand());
        Commands.add(new SlimefunCommand());
        Commands.add(new SlimefunHelperCommand());
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
