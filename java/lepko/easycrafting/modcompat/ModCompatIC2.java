package lepko.easycrafting.modcompat;

import ic2.api.item.*;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.helpers.EasyLog;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Array;
import java.util.*;

public class ModCompatIC2 extends ModCompat {

    public ModCompatIC2() {
        super("IC2");
    }

    @Override
    public void scanRecipes(List<IRecipe> recipes) {
        try {
            //
            Iterator<IRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                IRecipe r = iterator.next();
                String className = r.getClass().getName();
                if (className.equals("ic2.core.AdvRecipe") || className.equals("ic2.core.AdvShapelessRecipe")) {
                    Object[] input = (Object[]) Class.forName(className).getField("input").get(r);
                    ArrayList<Object> ingredients = new ArrayList<Object>();
                    for (Object object : input) {
                        if (object instanceof IRecipeInput) {
                            ingredients.add(getInputFromIRecipeInput((IRecipeInput) object));
                        } else if (object instanceof ArrayList){
                            ArrayList<Object> listNew = new ArrayList<Object>();
                            for (Object object1 : (ArrayList)object) {
                                if (object1 instanceof IRecipeInput) {
//                                    listNew.add(getInputFromIRecipeInput((IRecipeInput) object1));
                                    listNew.addAll(((IRecipeInput)object1).getInputs());
                                } else {
                                    listNew.add(object1);
                                }
                            }
                            ingredients.add(listNew);
                        } else {
                            ingredients.add(object);
                        }
                    }
                    if (!ingredients.isEmpty()) {
                        ingredients.removeAll(Collections.singleton(null));
                    }
//                    ArrayList<Object> ingredients = new ArrayList<Object>(Arrays.asList(input));
                    //Test.Exception of Forge Hummer and Cable Cutter
                    if (!ingredients.contains("craftingToolForgeHammer") && !ingredients.contains("craftingToolWireCutter"))
                        RecipeHelper.scannedRecipes.add(new EasyRecipe(EasyItemStack.fromItemStack(r.getRecipeOutput()), ingredients));
                    RecipeHelper.registeredRecipes.add(r);
                    iterator.remove();
                }
            }
            //
        } catch (Exception e) {
            EasyLog.warning("[ModCompat] [" + modID + "] Exception while scanning recipes.", e);
        }
    }

    private Object getInputFromIRecipeInput(IRecipeInput iRecipeInput) {
        if (iRecipeInput instanceof RecipeInputItemStack) {
            return ((RecipeInputItemStack)iRecipeInput).input;
        } else if (iRecipeInput instanceof RecipeInputOreDict) {
            return ((RecipeInputOreDict)iRecipeInput).input;
        } else {
            return iRecipeInput.getInputs();
        }
    }

    public static boolean isElectricItemStack(ItemStack is) {
        return ModCompat.isLoaded("IC2") && is.getItem() instanceof IElectricItem;
    }

    public static boolean isElectricItem(Item is) {
        return ModCompat.isLoaded("IC2") && is instanceof IElectricItem;
    }

//    private static IElectricItemManager manager = null;

    private static IElectricItemManager getManager(ItemStack is) {
        if (is.getItem() instanceof ISpecialElectricItem) {
            return ((ISpecialElectricItem) is.getItem()).getManager(is);
        }

        // TODO: remove when not supported anymore
//        if (manager == null) {
//            manager = ElectricItem.manager;
//        }
        return ElectricItem.manager;
    }

    public static int charge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectricItemStack(is)) {
            return 0;
        }

        // TODO: remove when not supported anymore
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).charge(is, amount, tier, ignoreTransferLimit, simulate);
        } else if (!(is.getItem() instanceof ISpecialElectricItem)) {
            ElectricItem.manager.charge(is, amount, tier, ignoreTransferLimit, simulate);
        }

        IElectricItemManager manager = getManager(is);
        if (manager == null) {
            return 0;
        }
        return manager.charge(is, amount, tier, ignoreTransferLimit, simulate);
    }

    public static int discharge(ItemStack is, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!isElectricItemStack(is)) {
            return 0;
        }

        // TODO: remove when not supported anymore
        if (is.getItem() instanceof ICustomElectricItem) {
            return ((ICustomElectricItem) is.getItem()).discharge(is, amount, tier, ignoreTransferLimit, simulate);
        } else if (!(is.getItem() instanceof ISpecialElectricItem)) {
            ElectricItem.manager.discharge(is, amount, tier, ignoreTransferLimit, simulate);
        }

        IElectricItemManager manager = getManager(is);
        if (manager == null) {
            return 0;
        }
        return manager.discharge(is, amount, tier, ignoreTransferLimit, simulate);
    }
}
