package lepko.easycrafting.helpers;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.log4j.Priority;
//
//import java.util.logging.Level;

public class EasyLog {

//    private static final Logger logger;
//    static {
//        logger = Logger.getLogger(VersionHelper.MOD_ID);
//        logger.setParent(FMLLog.getLogger());
//    }

    public static void log(String msg) {
        FMLLog.info(msg + " == T:" + Thread.currentThread().getName());
    }

    public static void warning(String msg) {
        FMLLog.warning(msg + " == T:" + Thread.currentThread().getName());
    }

    public static void warning(String msg, Throwable throwable) {
        FMLLog.log(Level.WARN, throwable,msg + " == T:" + Thread.currentThread().getName());
    }

    public static void severe(String msg) {
        FMLLog.severe(msg + " == T:" + Thread.currentThread().getName());
    }
}
