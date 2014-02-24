package lepko.easycrafting.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lepko.easycrafting.helpers.RecipeHelper;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {

    private boolean isFirstWorldLoad = true;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (isFirstWorldLoad) {
            isFirstWorldLoad = false;
            RecipeHelper.checkForNewRecipes();
        }
    }
}
