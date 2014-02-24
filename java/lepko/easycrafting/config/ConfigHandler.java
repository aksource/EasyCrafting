package lepko.easycrafting.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler {

    /**
     * The {@link Configuration} instance.
     */
    private static Configuration config;

    // Properties
    public static int EASYCRAFTINGTABLE_ID;
    public static int EASYCRAFTINGTABLE_ID_DEFAULT = 2500;

    public static boolean UPDATE_CHECK_ENABLED;
    public static boolean UPDATE_CHECK_ENABLED_DEFAULT = true;
    public static String UPDATE_CHECK_ENABLED_COMMENT = "Whether or not to check and display when a new version of this mod is available.";

    public static int MAX_RECURSION;
    public static int MAX_RECURSION_DEFAULT = 10;
    public static String MAX_RECURSION_COMMENT = "How deep to check for ingredients in multi level crafting, higher values can cause lag; default: 10";

    public static String CUSTOM_RECIPE_INGREDIENTS;
    public static String CUSTOM_RECIPE_INGREDIENTS_DEFAULT = "minecraft:crafting_table,minecraft:redstone,minecraft:book";
    public static String CUSTOM_RECIPE_INGREDIENTS_COMMENT = "Block and item IDs to use in crafting recipe; default: 58,331,340";

    public static void initialize(File file) {
        config = new Configuration(file);

        config.load();

//        EASYCRAFTINGTABLE_ID = config.getBlock("EasyCraftingTable", EASYCRAFTINGTABLE_ID_DEFAULT).getInt(EASYCRAFTINGTABLE_ID_DEFAULT);

        UPDATE_CHECK_ENABLED = config.get(Configuration.CATEGORY_GENERAL, "checkForUpdates", UPDATE_CHECK_ENABLED_DEFAULT, UPDATE_CHECK_ENABLED_COMMENT).getBoolean(UPDATE_CHECK_ENABLED_DEFAULT);
        MAX_RECURSION = config.get(Configuration.CATEGORY_GENERAL, "recipeRecursion", MAX_RECURSION_DEFAULT, MAX_RECURSION_COMMENT).getInt(MAX_RECURSION_DEFAULT);

        CUSTOM_RECIPE_INGREDIENTS = config.get(Configuration.CATEGORY_GENERAL, "customRecipeItems", CUSTOM_RECIPE_INGREDIENTS_DEFAULT, CUSTOM_RECIPE_INGREDIENTS_COMMENT).getString();

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void setRecursion(int value) {
        MAX_RECURSION = value;
        config.getCategory(Configuration.CATEGORY_GENERAL).get("recipeRecursion").set(value);
        config.save();
    }
}
