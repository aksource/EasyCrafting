package lepko.easycrafting.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import lepko.easycrafting.network.packet.MessageEasyCrafting;

/**
 * Created by A.K. on 14/06/01.
 */
public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("easycrafting");

    public static void init() {
        INSTANCE.registerMessage(MessageEasyCrafting.class, MessageEasyCrafting.class, 0, Side.SERVER);
    }

}
