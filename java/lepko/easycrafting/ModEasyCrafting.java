package lepko.easycrafting;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import lepko.easycrafting.block.BlockEasyCraftingTable;
import lepko.easycrafting.block.TileEntityEasyCrafting;
import lepko.easycrafting.config.ConfigHandler;
import lepko.easycrafting.core.CommandEasyCrafting;
import lepko.easycrafting.handlers.EventHandler;
import lepko.easycrafting.handlers.GuiHandler;
import lepko.easycrafting.handlers.ModCompatibilityHandler;
import lepko.easycrafting.handlers.TickHandlerClient;
import lepko.easycrafting.helpers.EasyLog;
import lepko.easycrafting.helpers.RecipeHelper;
import lepko.easycrafting.helpers.VersionHelper;
import lepko.easycrafting.network.PacketHandler;
import lepko.easycrafting.proxy.Proxy;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

//@Mod(modid = VersionHelper.MOD_ID, name = VersionHelper.MOD_NAME, version = VersionHelper.VERSION, dependencies = "required-after:FML")
@Mod(modid = "EasyCrafting", name = "Easy Crafting", version = "@MOD_VERSION@", dependencies = "required-after:Forge@[10.12.1.1090,)", useMetadata = true)
public class ModEasyCrafting {

    @Mod.Instance("EasyCrafting")
    public static ModEasyCrafting instance/* = new ModEasyCrafting()*/;
    @SidedProxy(clientSide = "lepko.easycrafting.proxy.ProxyClient", serverSide = "lepko.easycrafting.proxy.Proxy")
    public static Proxy proxy;
    // Blocks
    public static Block blockEasyCraftingTable;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EasyLog.log("Loading " + VersionHelper.MOD_NAME + " version " + VersionHelper.VERSION + ".");
        ConfigHandler.initialize(event.getSuggestedConfigurationFile());
        VersionHelper.performCheck();
        blockEasyCraftingTable = new BlockEasyCraftingTable(/*ConfigHandler.EASYCRAFTINGTABLE_ID*/);
        GameRegistry.registerBlock(blockEasyCraftingTable, "blockEasyCraftingTable");

        PacketHandler.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

//        LanguageRegistry.addName(blockEasyCraftingTable, "Easy Crafting Table");
        GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "tileEntityEasyCrafting");

        // Recipe from config
        String recipeItems = ConfigHandler.CUSTOM_RECIPE_INGREDIENTS;
        String[] items = recipeItems.split(",");
        Object[] array = new Object[items.length];
        String[] modIdName;
        Item item;
        for (int i = 0; i < items.length; i++) {
            try {
                modIdName = items[i].split(":");
                item = GameRegistry.findItem(modIdName[0], modIdName[1]);
                array[i] = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
            } catch (NumberFormatException nfe) {
                EasyLog.warning("customRecipeItems: '" + recipeItems + "' is not valid; Using default!");
                array = new Object[] { Blocks.crafting_table, Items.book, Items.redstone };
                break;
            }
        }
        GameRegistry.addShapelessRecipe(new ItemStack(blockEasyCraftingTable, 1), array);
        proxy.onLoad();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        FMLCommonHandler.instance().bus().register(new TickHandlerClient());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandEasyCrafting());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModCompatibilityHandler.load();
        RecipeHelper.checkForNewRecipes();
    }
}
