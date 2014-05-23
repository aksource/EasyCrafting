package lepko.easycrafting.modcompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lepko.easycrafting.helpers.EasyLog;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class ModCompatEE3 extends ModCompat {

    public ModCompatEE3() {
        super("EE3");
    }

    @Override
    public void scanRecipes(List<IRecipe> recipes) {
        try {
            //
            Iterator<IRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                ArrayList<Object> ingredients = RecipeHelper.getIngredientList(r);
                if (RecipeHelper.registeredRecipes.contains(r)) {
                    iterator.remove();
                } else if (ingredients != null) {
                    for (Object o : ingredients) {
                        if (o instanceof ItemStack && isTransmutationStone((ItemStack) o)) {
                            RecipeHelper.registeredRecipes.add(r);
                            iterator.remove();
                        }
                    }
                }
            }
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [" + modID + "] Exception while scanning recipes.", e);
        }
    }

    private static Class<?> transmutationStoneInterface;

    public static boolean isTransmutationStone(ItemStack itemstack) {
        if (transmutationStoneInterface == null) {
            try {
                transmutationStoneInterface = Class.forName("com.pahimar.ee3.item.ITransmutationStone");
            } catch (Exception e) {
                EasyLog.warning("Exception when trying to get transmutation stone interface.", e);
                return false;
            }
        }

        if (itemstack.getItem().getClass().getName().startsWith("com.pahimar")) {
            if (transmutationStoneInterface.isInstance(itemstack.getItem())) {
                return true;
            }
        }

        return false;
    }
}
