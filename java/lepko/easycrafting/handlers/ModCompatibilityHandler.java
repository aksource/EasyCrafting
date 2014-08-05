package lepko.easycrafting.handlers;

import lepko.easycrafting.helpers.EasyLog;
import lepko.easycrafting.modcompat.ModCompat;
import lepko.easycrafting.modcompat.ModCompatCS2;
import lepko.easycrafting.modcompat.ModCompatIC2;
import net.minecraft.item.crafting.IRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModCompatibilityHandler {

    public static Map<String, ModCompat> modules = new HashMap<String, ModCompat>();

    public static void load() {
        EasyLog.log("[ModCompat] Loading mod compatibility modules.");

//        new ModCompatEE3();
        new ModCompatCS2();
        new ModCompatIC2();

        for (Map.Entry<String, ModCompat> entry : modules.entrySet()) {
            entry.getValue().load();
        }

        EasyLog.log("[ModCompat] Finished.");
    }

    public static void scanRecipes(List<IRecipe> recipes) {
        for (Map.Entry<String, ModCompat> entry : modules.entrySet()) {
            if (entry.getValue().isModLoaded) {
                entry.getValue().scanRecipes(recipes);
            }
        }
    }
}
