package lepko.easycrafting.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import lepko.easycrafting.helpers.EasyLog;

public class Proxy {

//    @SidedProxy(clientSide = "lepko.easycrafting.proxy.ProxyClient", serverSide = "lepko.easycrafting.proxy.Proxy")
//    public static Proxy proxy;

    public void onLoad() {
    }

    public void printMessageToChat(String msg) {
        // Client only; print to console here
        if (msg != null) {
            EasyLog.log("[CHAT] " + msg);
        }
    }

    public boolean isClient() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return true;
        }
        return false;
    }
}
