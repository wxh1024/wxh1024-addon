package com.wxh1024.addon.modules;

import com.wxh1024.addon.Wxh1024Addon;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ControlifyCompat extends Module {
    public ControlifyCompat() {
        super(Wxh1024Addon.WXH1024, "手柄输入兼容",
            "将 Controlify 手柄输入桥接到 SlimefunHelper 的输入检测系统。");
    }
}
