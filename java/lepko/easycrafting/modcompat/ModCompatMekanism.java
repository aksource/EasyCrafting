package lepko.easycrafting.modcompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.helpers.EasyLog;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatMekanism extends ModCompat {

    public ModCompatMekanism() {
        super("Mekanism");
    }

    @Override
    public void scanRecipes(List<IRecipe> recipes) {
        try {
            Class mekanismRecipe;
            Method getInput;
            Iterator<IRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                String className = r.getClass().getName();
                if (className.equals("mekanism.common.recipe.MekanismRecipe")) {
                	mekanismRecipe = Class.forName(className);
                	getInput = mekanismRecipe.getMethod("getInput", (Class[])null);
//                    Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
                	Object[] input = (Object[]) getInput.invoke(r, (Object[])null);
                    ArrayList<Object> ingredients = new ArrayList<Object>(Arrays.asList(input));
                    RecipeHelper.scannedRecipes.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                    RecipeHelper.registeredRecipes.add(r);
                    iterator.remove();
                }
            }
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [" + modID + "] Exception while scanning recipes.", e);
            return;
        }
    }
}