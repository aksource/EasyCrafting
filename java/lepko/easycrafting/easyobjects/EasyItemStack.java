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

    private String id;
    private int damage;
    private int size;
    private int charge;
    private NBTTagCompound stackTagCompound;

    public EasyItemStack(String id, int damage, int size, int charge) {
        this.id = id;
        this.damage = damage;
        this.size = size;
        this.charge = charge;
    }

    public EasyItemStack(String id, int damage, int size) {
        this(id, damage, size, 0);
    }

    public EasyItemStack(String id, int damage) {
        this(id, damage, 1, 0);
    }

    public EasyItemStack(String id) {
        this(id, 0, 1, 0);
    }

    public String getID() {
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

    public int getInternalID() { return Item.getIdFromItem(getItemFromUniqueString(id));}

    public ItemStack toItemStack() {
        if (getItemFromUniqueString(id) == null) return null;
        ItemStack is = new ItemStack(getItemFromUniqueString(id), size, damage);
        is.setTagCompound(stackTagCompound);
        if (charge > 0) {
            ModCompatIC2.discharge(is, 0x7fffffff, 0x7fffffff, true, false);
            ModCompatIC2.charge(is, charge, 0x7fffffff, true, false);
        }
        return is;
    }
    public static ItemStack toItemStack(EasyItemStack eis) {
        if (getItemFromUniqueString(eis.id) == null) return null;
        ItemStack is = new ItemStack(getItemFromUniqueString(eis.id), eis.size, eis.damage);
        is.setTagCompound(eis.stackTagCompound);
        return is;
    }
    public static EasyItemStack fromItemStack(ItemStack is) {
        int charge = ModCompatIC2.discharge(is, 0x7fffffff, 0x7fffffff, true, true);
        EasyItemStack eis = new EasyItemStack(getUniqueStrings(is.getItem()), is.getItemDamage(), is.stackSize, charge);
        eis.stackTagCompound = is.getTagCompound();
        return eis;
    }

    public static boolean areStackTagsEqual(EasyItemStack is0, ItemStack is1) {
        return (is0 == null && is1 == null) || (is0 != null && is1 != null && (is0.stackTagCompound != null || is1.stackTagCompound == null) && (is0.stackTagCompound == null || is0.stackTagCompound.equals(is1.stackTagCompound)));
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
        return id.equals(other.id) && (damage == other.damage || damage == OreDictionary.WILDCARD_VALUE || other.damage == OreDictionary.WILDCARD_VALUE || ModCompatIC2.isElectricItem(getItemFromUniqueString(id))) &&(ignoreSize || size == other.size);
    }

    public boolean equalsItemStack(ItemStack is) {
        return equalsItemStack(is, false);
    }

    public boolean equalsItemStack(ItemStack is, boolean ignoreSize) {
        return is != null && id.equals(getUniqueStrings(is.getItem())) && (damage == is.getItemDamage() || damage == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == OreDictionary.WILDCARD_VALUE || !is.getHasSubtypes() || ModCompatIC2.isElectricItem(getItemFromUniqueString(id))) && (ignoreSize || size == is.stackSize );
    }

    // TODO: separate charge and usedIngredient methods
    public void setCharge(List<ItemStack> usedIngredients) {
        int outputCharge = 0;

        if (usedIngredients != null) {
            for (ItemStack ingredient :  usedIngredients) {
                outputCharge += ModCompatIC2.discharge(ingredient, 0x7fffffff, 0x7fffffff, true, true);
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
        return (uId == null) ? "" : uId.toString();

    }
    public static Item getItemFromUniqueString(String str) {
        if (str.equals("")) return null;
        String modId;
        String modName;
        String[] split = str.split(":");
        modId = split[0];
        modName = split[1];
        return GameRegistry.findItem(modId, modName);
    }
}
