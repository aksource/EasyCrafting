package lepko.easycrafting.block;

import lepko.easycrafting.handlers.TickHandlerClient;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotInterceptor extends Slot {

    public SlotInterceptor(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        TickHandlerClient.updateEasyCraftingOutput();
    }
}
