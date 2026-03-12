package com.xinyihl.functionalstoragelegacy.common.integration.top;

import com.xinyihl.functionalstoragelegacy.misc.Configurations;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class TheOneProbeCompat {

    public static void register() {
        if (Configurations.COMPATIBILITY.enableTOPCompatibility && Loader.isModLoaded("theoneprobe")) {
            FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", TOPIntegration.class.getName());
        }
    }
}
