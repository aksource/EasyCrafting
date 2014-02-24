package lepko.easycrafting.easyobjects;

import cpw.mods.fml.common.registry.GameRegistry;
//import ic2.api.item.IElectricItem;

import java.util.List;

import lepko.easycrafting.modcompat.ModCompatIC2;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class EasyItemStack {

    private int id;
    private int damage;
    private int size;
    private int charge;
    private NBTTagCompound stackTagCompound;

    public EasyItemStack(int id, int damage, int size, int charge) {
        this.id = id;
        this.damage = damage;
        this.size = size;
        this.charge = charge;
    }

    public EasyItemStack(int id, int damage, int size) {
        this(id, damage, size, 0);
    }

    public EasyItemStack(int id, int damage) {
        this(id, damage, 1, 0);
    }

    public EasyItemStack(int id) {
        this(id, 0, 1, 0);
    }

    public int getID() {
        return id;
    }

    public int getDamage() {
        return damage;
    }

    public int getSize() {
        return size;
    }

    public int getCharge() {
        return charge;
    }

    public int getInternalID() { return Item.getIdFromItem(toItemStack().getItem());}

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(Item.getItemById(id)/*getItemFromUniqueString(id)*/, size, damage);
        is.setTagCompound(stackTagCompound);
        if (charge > 0) {
//            ModCompatIC2.discharge(is, 0x7fffffff, 0x7fffffff, true, false);
//            ModCompatIC2.charge(is, charge, 0x7fffffff, true, false);
        }
        return is;
    }
    public static ItemStack toItemStack(EasyItemStack eis) {
        ItemStack is = new ItemStack(Item.getItemById(eis.id)/*getItemFromUniqueString(eis.id)*/, eis.size, eis.damage);
        is.setTagCompound(eis.stackTagCompound);
        return is;
    }
    public static EasyItemStack fromItemStack(ItemStack is) {
        int charge = 0;/*ModCompatIC2.discharge(is, 0x7fffffff, 0x7fffffff, true, true)*/;
        EasyItemStack eis = new EasyItemStack(Item.getIdFromItem(is.getItem())/*getUniqueStrings(is.getItem())*/, is.getItemDamage(), is.stackSize, charge);
        eis.stackTagCompound = is.getTagCompound();
        return eis;
    }

    public static boolean areStackTagsEqual(EasyItemStack is0, ItemStack is1) {
        if (is0 == null && is1 == null) {
            return true;
        } else {
            if (is0 != null && is1 != null) {
                if (is0.stackTagCompound == null && is1.stackTagCompound != null) {
                    return false;
                } else {
                    return is0.stackTagCompound == null || is0.stackTagCompound.equals(is1.stackTagCompound);
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return "EasyItemStack [id=" + id + ", damage=" + damage + ", size=" + size + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return equals(obj, false);
    }

    public boolean equals(Object obj, boolean ignoreSize) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EasyItemStack other = (EasyItemStack) obj;
        if (id != other.id/*!id.equals(other.id)*/) {
            return false;
        }
        if (damage != other.damage && damage != OreDictionary.WILDCARD_VALUE && other.damage != OreDictionary.WILDCARD_VALUE/* && !(getItemFromUniqueString(id) instanceof IElectricItem)*/) {
            return false;
        }
        if (!ignoreSize && size != other.size) {
            return false;
        }
        return true;
    }

    public boolean equalsItemStack(ItemStack is) {
        return equalsItemStack(is, false);
    }

    public boolean equalsItemStack(ItemStack is, boolean ignoreSize) {
        if (is == null) {
            return false;
        }
        if (id != Item.getIdFromItem(is.getItem())/*!id.equals(getUniqueStrings(is.getItem()))*/) {
            return false;
        }
        if (damage != is.getItemDamage() && damage != OreDictionary.WILDCARD_VALUE && is.getItemDamage() != OreDictionary.WILDCARD_VALUE && is.getHasSubtypes()/* && !(getItemFromUniqueString(id) instanceof IElectricItem)*/) {
            return false;
        }
        if (!ignoreSize && size != is.stackSize) {
            return false;
        }
        return true;
    }

    // TODO: separate charge and usedIngredient methods
    public void setCharge(List<ItemStack> usedIngredients) {
        int outputCharge = 0;

        if (usedIngredients != null) {
            for (int i = 0; i < usedIngredients.size(); i++) {
                ItemStack ingredient = usedIngredients.get(i);
//                outputCharge += ModCompatIC2.discharge(ingredient, 0x7fffffff, 0x7fffffff, true, true);
            }
        }

        charge = outputCharge;
        this.usedIngredients = usedIngredients;
    }

    // TODO: move this to EasyRecipe?
    public List<ItemStack> usedIngredients;
    public static String getUniqueStrings(Object obj)
    {
        GameRegistry.UniqueIdentifier uId;
        if(obj instanceof Block) {
            uId = GameRegistry.findUniqueIdentifierFor((Block) obj);
        }else {
            uId = GameRegistry.findUniqueIdentifierFor((Item) obj);
        }
        return uId.modId + ":" + uId.name;

    }
    public static Item getItemFromUniqueString(String str) {
        String modId;
        String modName;
        String[] sprit = str.split(":");
        modId = sprit[0];
        modName = sprit[1];
        Item item = GameRegistry.findItem(modId, modName);
        return item;
    }
}
