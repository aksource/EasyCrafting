package lepko.easycrafting.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lepko.easycrafting.config.ConfigHandler;
import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Created by A.K. on 14/06/01.
 */
public class MessageEasyCrafting implements IMessage, IMessageHandler<MessageEasyCrafting, IMessage> {

    private EasyItemStack result;
    private ItemStack[] ingredients;
    private boolean isRightClick = false;

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

    @Override
    public IMessage onMessage(MessageEasyCrafting message, MessageContext ctx) {
        if (ctx.getServerHandler().playerEntity != null) {
            EntityPlayer sender = ctx.getServerHandler().playerEntity;
            EasyRecipe recipe = RecipeHelper.getValidRecipe(message.result, message.ingredients);
            if (recipe == null) {
                return null;
            }

            ItemStack stack_in_hand = sender.inventory.getItemStack();
            ItemStack return_stack = null;
            int return_size = 0;

            if (stack_in_hand == null) {
                return_stack = recipe.getResult().toItemStack();
                return_size = recipe.getResult().getSize();
            } else if (recipe.getResult().equalsItemStack(stack_in_hand, true) && stack_in_hand.getMaxStackSize() >= recipe.getResult().getSize() + stack_in_hand.stackSize && EasyItemStack.areStackTagsEqual(recipe.getResult(), stack_in_hand)) {
                return_stack = recipe.getResult().toItemStack();
                return_size = recipe.getResult().getSize() + stack_in_hand.stackSize;
            }

            if (return_stack != null) {
                if (!message.isRightClick) {
                    if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
                        return_stack.stackSize = return_size;
                        //Test
                        return_stack.onCrafting(sender.worldObj, sender, 1);
                        sender.inventory.setItemStack(return_stack);
                    }
                } else {
                    int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
                    int timesCrafted = RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
                    if (timesCrafted > 0) {
                        return_stack.stackSize = return_size + (timesCrafted - 1) * recipe.getResult().getSize();
                        //Test
                        return_stack.onCrafting(sender.worldObj, sender, timesCrafted);
                        sender.inventory.setItemStack(return_stack);
                    }
                }
            }
        }
        return null;
    }
}
