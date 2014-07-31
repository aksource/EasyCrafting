package lepko.easycrafting.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import lepko.easycrafting.config.ConfigHandler;
import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.helpers.RecipeHelper;
import lepko.easycrafting.network.packet.MessageEasyCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by A.K. on 14/08/01.
 */
public class MessageEasyCraftingHandler implements IMessageHandler<MessageEasyCrafting, IMessage> {
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
