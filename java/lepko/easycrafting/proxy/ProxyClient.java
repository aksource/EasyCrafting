package lepko.easycrafting.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import lepko.easycrafting.helpers.EasyLog;
import net.minecraft.util.ChatComponentTranslation;

//import cpw.mods.fml.common.registry.TickRegistry;

public class ProxyClient extends Proxy {

    @Override
    public void onLoad() {
        // Register Client Tick Handler
//        FMLCommonHandler.instance().bus().register(new TickHandlerClient());
    }

    @Override
    public void printMessageToChat(String msg) {
        if (msg != null) {
            if (FMLClientHandler.instance().getClient().ingameGUI != null) {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation(msg, new Object[]{}));
            } else {
                EasyLog.log("[CHAT] " + msg);
            }
        }
    }
}
