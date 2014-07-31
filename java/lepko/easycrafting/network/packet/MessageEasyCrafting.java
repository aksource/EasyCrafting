package lepko.easycrafting.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by A.K. on 14/06/01.
 */
public class MessageEasyCrafting implements IMessage {

    public EasyItemStack result;
    public ItemStack[] ingredients;
    public boolean isRightClick = false;

    public MessageEasyCrafting(){}

    public MessageEasyCrafting(EasyRecipe recipe, boolean isRightClick) {
        setRecipe(recipe);
        this.isRightClick = isRightClick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isRightClick = buf.readBoolean();

        String id = ByteBufUtils.readUTF8String(buf);
        int damage = buf.readInt();
        int size = buf.readByte();

        result = new EasyItemStack(id, damage, size);

        int length = buf.readByte();

        ingredients = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            ingredients[i] = ByteBufUtils.readItemStack(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isRightClick);

        ByteBufUtils.writeUTF8String(buf, result.getID());
        buf.writeInt(result.getDamage());
        buf.writeByte(result.getSize());

        buf.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            ByteBufUtils.writeItemStack(buf, is);
        }
    }

    private void setRecipe(EasyRecipe recipe) {

        result = recipe.getResult();
        ingredients = new ItemStack[recipe.getIngredientsSize()];

        for (int i = 0; i < recipe.getIngredientsSize(); i++) {
            if (recipe.getIngredient(i) instanceof EasyItemStack) {
                EasyItemStack eis = (EasyItemStack) recipe.getIngredient(i);
                ingredients[i] = EasyItemStack.toItemStack(eis);
            } else if (recipe.getIngredient(i) instanceof List) {
                @SuppressWarnings("rawtypes")
                List ingList = (List) recipe.getIngredient(i);
                if (!ingList.isEmpty() && ingList.get(0) instanceof ItemStack) {
                    ingredients[i] = ((ItemStack) ingList.get(0)).copy();
                } else {
                    ingredients[i] = new ItemStack(Blocks.air, 0, 1);
                }
            }
        }
    }
}
