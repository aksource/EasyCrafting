package lepko.easycrafting.modcompat;

import cpw.mods.fml.common.Loader;
import lepko.easycrafting.handlers.ModCompatibilityHandler;
import lepko.easycrafting.helpers.EasyLog;
import net.minecraft.item.crafting.IRecipe;

import java.util.List;

public abstract class ModCompat {

    public boolean isModLoaded = false;
    public String modID;

    public ModCompat(String modID) {
        this.modID = modID;
        ModCompatibilityHandler.modules.put(modID, this);
    }

    public void load() {
        log("Checking for mod...");
        if (!Loader.isModLoaded(modID)) {
            log("Mod not found.");
            return;
        }
        log("Mod found.");
        isModLoaded = true;
    }

    public abstract void scanRecipes(List<IRecipe> recipes);

    protected void log(String msg) {
        EasyLog.log("[ModCompat] [" + modID + "] " + msg);
    }

    public static final boolean isLoaded(String modID) {
        if (ModCompatibilityHandler.modules.get(modID) != null) {
            return ModCompatibilityHandler.modules.get(modID).isModLoaded;
        }
        return false;
    }
}
