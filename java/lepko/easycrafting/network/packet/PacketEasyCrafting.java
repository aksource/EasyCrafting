package lepko.easycrafting.network.packet;

import java.util.List;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import lepko.easycrafting.config.ConfigHandler;
import lepko.easycrafting.easyobjects.EasyItemStack;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class PacketEasyCrafting extends AbstractPacket {

    private EasyItemStack result;
    ItemStack[] ingredients;
    private boolean isRightClick = false;

    public PacketEasyCrafting() {}

    public PacketEasyCrafting(EasyRecipe recipe, boolean isRightClick) {
        setRecipe(recipe);
        this.isRightClick = isRightClick;
    }

    public void setRecipe(EasyRecipe recipe) {

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

//    @Override
//    public void run(Player player) {
//
//        EasyRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
//        if (recipe == null) {
//            return;
//        }
//
//        EntityPlayer sender = (EntityPlayer) player;
//        ItemStack stack_in_hand = sender.inventory.getItemStack();
//        ItemStack return_stack = null;
//        int return_size = 0;
//
//        if (stack_in_hand == null) {
//            return_stack = recipe.getResult().toItemStack();
//            return_size = recipe.getResult().getSize();
//        } else if (recipe.getResult().equalsItemStack(stack_in_hand, true) && stack_in_hand.getMaxStackSize() >= recipe.getResult().getSize() + stack_in_hand.stackSize && EasyItemStack.areStackTagsEqual(recipe.getResult(), stack_in_hand)) {
//            return_stack = recipe.getResult().toItemStack();
//            return_size = recipe.getResult().getSize() + stack_in_hand.stackSize;
//        }
//
//        if (return_stack != null) {
//            if (!isRightClick) {
//                if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
//                    return_stack.stackSize = return_size;
//                    sender.inventory.setItemStack(return_stack);
//                }
//            } else {
//                int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
//                int timesCrafted = RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
//                if (timesCrafted > 0) {
//                    return_stack.stackSize = return_size + (timesCrafted - 1) * recipe.getResult().getSize();
//                    sender.inventory.setItemStack(return_stack);
//                }
//            }
//        }
//    }

//    @Override
//    protected void readData(DataInputStream data) throws IOException {
//
//        isRightClick = data.readBoolean();
//
//        String id = data.readst();
//        int damage = data.readInt();
//        int size = data.readByte();
//
//        result = new EasyItemStack(id, damage, size);
//
//        int length = data.readByte();
//
//        ingredients = new ItemStack[length];
//
//        for (int i = 0; i < length; i++) {
//            int _id = data.readShort();
//            int _damage = data.readInt();
//            int _size = data.readByte();
//
//            ingredients[i] = new ItemStack(_id, _size, _damage);
//        }
//    }
//
//    @Override
//    protected void writeData(DataOutputStream data) throws IOException {
//
//        data.writeBoolean(isRightClick);
//
//        data.writeShort(result.getID());
//        data.writeInt(result.getDamage());
//        data.writeByte(result.getSize());
//
//        data.writeByte(ingredients.length);
//
//        for (ItemStack is : ingredients) {
//            data.writeShort(is.itemID);
//            data.writeInt(is.getItemDamage());
//            data.writeByte(is.stackSize);
//        }
//    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeBoolean(isRightClick);

//        ByteBufUtils.writeUTF8String(buffer, result.getID());
        buffer.writeInt(result.getID());
        buffer.writeInt(result.getDamage());
        buffer.writeByte(result.getSize());

        buffer.writeByte(ingredients.length);

        for (ItemStack is : ingredients) {
            ByteBufUtils.writeItemStack(buffer, is);
//            buffer.writeShort(is.itemID);
//            buffer.writeInt(is.getItemDamage());
//            buffer.writeByte(is.stackSize);
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        isRightClick = buffer.readBoolean();

//        String id = ByteBufUtils.readUTF8String(buffer);
        int id = buffer.readInt();
        int damage = buffer.readInt();
        int size = buffer.readByte();

        result = new EasyItemStack(id, damage, size);

        int length = buffer.readByte();

        ingredients = new ItemStack[length];

        for (int i = 0; i < length; i++) {
//            int _id = buffer.readShort();
//            int _damage = buffer.readInt();
//            int _size = buffer.readByte();

            ingredients[i] = ByteBufUtils.readItemStack(buffer);
        }
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer sender) {
        EasyRecipe recipe = RecipeHelper.getValidRecipe(result, ingredients);
        if (recipe == null) {
            return;
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
            if (!isRightClick) {
                if (RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, 1, ConfigHandler.MAX_RECURSION) > 0) {
                    return_stack.stackSize = return_size;
                    sender.inventory.setItemStack(return_stack);
                }
            } else {
                int maxTimes = RecipeHelper.calculateCraftingMultiplierUntilMaxStack(return_stack, stack_in_hand);
                int timesCrafted = RecipeHelper.canCraft(recipe, sender.inventory, RecipeHelper.getAllRecipes(), true, maxTimes, ConfigHandler.MAX_RECURSION);
                if (timesCrafted > 0) {
                    return_stack.stackSize = return_size + (timesCrafted - 1) * recipe.getResult().getSize();
                    sender.inventory.setItemStack(return_stack);
                }
            }
        }
    }
}
