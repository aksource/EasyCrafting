package lepko.easycrafting.inventory.gui;

import java.util.ArrayList;
import java.util.List;

import lepko.easycrafting.ModEasyCrafting;
import lepko.easycrafting.block.TileEntityEasyCrafting;
import lepko.easycrafting.easyobjects.EasyRecipe;
import lepko.easycrafting.handlers.TickHandlerClient;
import lepko.easycrafting.helpers.RecipeHelper;
import lepko.easycrafting.helpers.RecipeWorker;
import lepko.easycrafting.helpers.VersionHelper;
import lepko.easycrafting.inventory.ContainerEasyCrafting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ImmutableList;

public class GuiEasyCrafting extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(VersionHelper.MOD_ASSETS, "textures/gui/easycraftinggui.png");

    private static final int TABINDEX_CRAFTING = 0;
    private static final int TABINDEX_SEARCH = 1;

    private static int selectedTabIndex = TABINDEX_CRAFTING;
    private static String lastSearch = "";

    public int currentScroll = 0;
    private int maxScroll = 0;
    private float scrollbarOffset = 0;
    public ImmutableList<EasyRecipe> renderList;
    public ImmutableList<EasyRecipe> craftableList;
    private boolean[] canCraftCache;
    private boolean wasClicking = false;
    private boolean isScrolling = false;
    private ItemStack[] tabIcons = { new ItemStack(ModEasyCrafting.blockEasyCraftingTable), new ItemStack(Items.compass) };
    private String[] tabDescriptions = { "Available Recipes", "Search Recipes" };
    private GuiTextField searchField;

    public GuiEasyCrafting(InventoryPlayer player_inventory, TileEntityEasyCrafting tile_entity) {
        super(new ContainerEasyCrafting(tile_entity, player_inventory));

        if (inventorySlots != null && inventorySlots instanceof ContainerEasyCrafting) {
            ((ContainerEasyCrafting) inventorySlots).gui = this;
        }

        ySize = 235;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(fontRendererObj, guiLeft + 82, guiTop + 6, 89, fontRendererObj.FONT_HEIGHT);
        searchField.setMaxStringLength(15);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setVisible(false);
        searchField.setTextColor(0xFFFFFF);
        switchToTab(selectedTabIndex);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        int offsetX = 0;
        if (selectedTabIndex != TABINDEX_SEARCH) {
            fontRendererObj.drawString("Easy Crafting Table", 8, 6, 0x404040);
            offsetX = 159;
        } else {
            fontRendererObj.drawString("Search:", 8, 6, 0x404040);
            offsetX = 70;
        }

        if (RecipeWorker.lock.isLocked()) {
            fontRendererObj.drawString(EnumChatFormatting.OBFUSCATED + "x", offsetX, 6, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.enableGUIStandardItemLighting();
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        // Tabs
        drawTabs();
        // Main GUI
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        // Search field and output slot backgrounds
        if (selectedTabIndex == TABINDEX_SEARCH) {
            int xSearchTex = xSize - 90 - 7;
            drawTexturedModalRect(guiLeft + xSearchTex, y + 4, xSearchTex, 256 - 12, 90, 12);
            searchField.drawTextBox();

            if (canCraftCache != null) {
                int offset = currentScroll * 8;
                for (int k = 0; k < 40 && k + offset < canCraftCache.length; k++) {
                    renderSlotBackColor(inventorySlots.getSlot(k), canCraftCache[k + offset]);
                }
            }
        }
        // Storage slots background
        for (int l = 0; l < 18; l++) {
            renderSlotBackColor(inventorySlots.getSlot(l + 40), false);
        }
        // Scrollbar
        mc.renderEngine.bindTexture(GUI_TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int xTex = maxScroll == 0 ? 12 : 0;
        drawTexturedModalRect(x + 156, y + 17 + (int) (scrollbarOffset * 73.0F), xTex, 240, 12, 16);
        // Selected tab
        drawTab(selectedTabIndex);
    }

    private void drawTabs() {
        for (int i = 0; i < 2; i++) {
            if (i == selectedTabIndex) {
                continue;
            }
            drawTab(i);
        }
    }

    private void drawTab(int i) {
        int width = 32;
        int height = 28;
        int texLeft = 256 - width;
        int texTop = i * height;
        int x = guiLeft - 28 - 2;
        int y = guiTop + i * (height + 1);

        if (i == selectedTabIndex) {
            texLeft -= width;
            x += 2;
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        drawTexturedModalRect(x, y, texLeft, texTop, width, height);
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        x += 10 + (i == selectedTabIndex ? -1 : 1);
        y += 6;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        ItemStack iconItemStack = tabIcons[i];
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, iconItemStack, x, y);
        GL11.glDisable(GL11.GL_LIGHTING);
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        if (selectedTabIndex != TABINDEX_SEARCH) {
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindChat.getKeyCode())) {
                switchToTab(TABINDEX_SEARCH);
            } else {
                super.keyTyped(par1, par2);
            }
        } else {
            if (!checkHotbarKeys(par2)) {
                if (searchField.textboxKeyTyped(par1, par2)) {
                    updateSearch();
                } else {
                    super.keyTyped(par1, par2);
                }
            }
        }
    }

    @Override
    public void handleMouseInput() {
        // Handle mouse scroll
        int delta = Mouse.getEventDWheel();
        if (delta == 0) {
            // Fix NEI auto clicking slots when mouse is being scrolled; only call super when mouse is not scrolling
            super.handleMouseInput();
        } else {
            setScrollPosition(currentScroll + (delta > 0 ? -1 : 1));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3) {
        // Handle scrollbar dragging
        boolean leftMouseDown = Mouse.isButtonDown(0);
        int left = guiLeft + 155;
        int top = guiTop + 18;
        int right = left + 14;
        int bottom = top + 89;

        if (!wasClicking && leftMouseDown && mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom) {
            isScrolling = maxScroll > 0;
        } else if (!leftMouseDown) {
            isScrolling = false;
        }

        wasClicking = leftMouseDown;

        if (isScrolling) {
            setScrollPosition((mouseY - top - 7.5F) / (bottom - top - 15.0F));
        }

        super.drawScreen(mouseX, mouseY, par3);

        // Handle tab hover text
        for (int i = 0; i < tabDescriptions.length; i++) {
            if (isOverTab(i, mouseX, mouseY)) {
                drawCreativeTabHoveringText(tabDescriptions[i], mouseX, mouseY);
            }
        }

        RenderHelper.enableStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        // Handle tab changing
        if (button == 0) {
            for (int i = 0; i < tabDescriptions.length; i++) {
                if (i != selectedTabIndex && isOverTab(i, x, y)) {
                    switchToTab(i);
                    return;
                }
            }
        }

        super.mouseClicked(x, y, button);
    }

    private boolean isOverTab(int tabIndex, int x, int y) {
        int width = 32;
        int height = 28;
        int tabX = guiLeft - 28 - 2;
        int tabY = guiTop + tabIndex * (height + 1);
        return x > tabX && x < tabX + width && y > tabY && y < tabY + height;
    }

    private void switchToTab(int tabIndex) {
        if (searchField != null) {
            if (tabIndex == TABINDEX_SEARCH) {
                searchField.setVisible(true);
                searchField.setCanLoseFocus(false);
                searchField.setFocused(true);
                searchField.setText(lastSearch);
            } else {
                searchField.setVisible(false);
                searchField.setCanLoseFocus(true);
                searchField.setFocused(false);
            }
        }
        GuiEasyCrafting.selectedTabIndex = tabIndex;
        updateSearch();
    }

    public void renderSlotBackColor(Slot slot, boolean canCraft) {
        int x = guiLeft + slot.xDisplayPosition;
        int y = guiTop + slot.yDisplayPosition;
        int w = 16;
        int h = 16;
        int color = canCraft ? 0x8000A000 : 0x80A00000;
        Gui.drawRect(x, y, x + w, y + h, color);
    }

    @SuppressWarnings("unchecked")
    private void updateSearch() {
        if (selectedTabIndex == TABINDEX_SEARCH) {
            ImmutableList<EasyRecipe> all = RecipeHelper.getAllRecipes();
            ArrayList<EasyRecipe> list = new ArrayList<EasyRecipe>();
            lastSearch = searchField.getText().toLowerCase();

            recipeLoop: for (EasyRecipe r : all) {
                try {
                    List<String> itemProps = r.getResult().toItemStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);
                    for (String string : itemProps) {
                        if (string.toLowerCase().contains(lastSearch)) {
                            list.add(r);
                            continue recipeLoop;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            renderList = ImmutableList.copyOf(list);
        }
        currentScroll = 0;
        scrollbarOffset = 0.0F;
        TickHandlerClient.updateEasyCraftingOutput();
    }

    public void refreshCraftingOutput() {
        craftableList = RecipeWorker.instance().getCraftableRecipes();
        if (selectedTabIndex == TABINDEX_CRAFTING) {
            renderList = craftableList;
        } else if (selectedTabIndex == TABINDEX_SEARCH) {
            updateSlotBackgroundCache();
        }

        maxScroll = (int) (Math.ceil(renderList.size() / 8.0D) - 5);
        if (maxScroll < 0) {
            maxScroll = 0;
        }

        if (currentScroll > maxScroll) {
            setScrollPosition(maxScroll);
        } else {
            ContainerEasyCrafting c = (ContainerEasyCrafting) inventorySlots;
            c.scrollTo(currentScroll, renderList);
        }
    }

    private void updateSlotBackgroundCache() {
        canCraftCache = new boolean[renderList.size()];
        for (int i = 0; i < renderList.size(); i++) {
            if (craftableList.contains(renderList.get(i))) {
                canCraftCache[i] = true;
            } else {
                canCraftCache[i] = false;
            }
        }
    }

    private void setScrollPosition(int scroll) {
        if (scroll < 0) {
            scroll = 0;
        } else if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        currentScroll = scroll;

        scrollbarOffset = (float) currentScroll / (float) maxScroll;
        if (scrollbarOffset < 0.0F || Float.isNaN(scrollbarOffset)) {
            scrollbarOffset = 0.0F;
        } else if (scrollbarOffset > 1.0F) {
            scrollbarOffset = 1.0F;
        }

        ContainerEasyCrafting c = (ContainerEasyCrafting) inventorySlots;
        c.scrollTo(currentScroll, renderList);
    }

    private void setScrollPosition(float scrollOffset) {
        if (scrollOffset < 0.0F || Float.isNaN(scrollOffset)) {
            scrollOffset = 0.0F;
        } else if (scrollOffset > 1.0F) {
            scrollOffset = 1.0F;
        }

        if (scrollbarOffset == scrollOffset) {
            return;
        }
        scrollbarOffset = scrollOffset;

        currentScroll = (int) (scrollbarOffset * maxScroll);
        if (currentScroll < 0) {
            currentScroll = 0;
        } else if (currentScroll > maxScroll) {
            currentScroll = maxScroll;
        }

        ContainerEasyCrafting c = (ContainerEasyCrafting) inventorySlots;
        c.scrollTo(currentScroll, renderList);
    }

    protected void drawIngredientTooltip(int slotIndex, int mouseX, int mouseY, boolean leftSide) {

        EasyRecipe recipe = null;

        int recipe_index = slotIndex + currentScroll * 8;
        if (recipe_index >= 0 && renderList != null && recipe_index < renderList.size()) {
            EasyRecipe r = renderList.get(recipe_index);
            if (r.getResult().equalsItemStack(inventorySlots.getSlot(slotIndex).getStack())) {
                recipe = r;
            }
        }

        if (recipe == null) {
            return;
        }

        ArrayList<ItemStack> ingredientList = recipe.getCompactIngredientList();

        if (ingredientList != null && !ingredientList.isEmpty()) {
            int width = 16;
            int height = 16;
            int xPos = mouseX + 12;
            int yPos = mouseY - 12 + 14;

            if (ingredientList.size() > 1) {
                width += (ingredientList.size() - 1) * (width + 2);
            }

            if (leftSide) {
                xPos -= 28 + width;
            }

            int bgColor = 0xF0100010;
            int borderColor = 0x5000A700;// red: 0x50FF0000;// green: 0x5000A700;// vanilla purple: 0x505000FF;
            int borderColorDark = (borderColor & 0xFEFEFE) >> 1 | borderColor & 0xFF000000;

            zLevel = 300.0F;
            itemRender.zLevel = 300.0F;

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);

            drawGradientRect(xPos - 3, yPos - 4, xPos + width + 3, yPos - 3, bgColor, bgColor);
            drawGradientRect(xPos - 3, yPos + height + 3, xPos + width + 3, yPos + height + 4, bgColor, bgColor);
            drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos + height + 3, bgColor, bgColor);
            drawGradientRect(xPos - 4, yPos - 3, xPos - 3, yPos + height + 3, bgColor, bgColor);
            drawGradientRect(xPos + width + 3, yPos - 3, xPos + width + 4, yPos + height + 3, bgColor, bgColor);

            drawGradientRect(xPos - 3, yPos - 3 + 1, xPos - 3 + 1, yPos + height + 3 - 1, borderColor, borderColorDark);
            drawGradientRect(xPos + width + 2, yPos - 3 + 1, xPos + width + 3, yPos + height + 3 - 1, borderColor, borderColorDark);
            drawGradientRect(xPos - 3, yPos - 3, xPos + width + 3, yPos - 3 + 1, borderColor, borderColor);
            drawGradientRect(xPos - 3, yPos + height + 2, xPos + width + 3, yPos + height + 3, borderColorDark, borderColorDark);

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);

            for (ItemStack is : ingredientList) {
                if (is.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                    ItemStack is2 = is.copy();
                    is2.setItemDamage(0);

                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, is2, xPos, yPos);
                    itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, is2, xPos, yPos);
                } else {
                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, is, xPos, yPos);
                    itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, is, xPos, yPos);
                }

                xPos += 18;
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);

            zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
    }

    @Override
    protected void renderToolTip(ItemStack stack, int mouseX, int mouseY) {
        if (isCtrlKeyDown()) {
            for (int j = 0; j < 40; j++) {
                Slot slot = inventorySlots.getSlot(j);
                if (func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
                    List<String> list = new ArrayList<String>();
                    String itemName = (String) stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips).get(0);
                    list.add("\u00a7" + Integer.toHexString(stack.getRarity().rarityColor.getFormattingCode()) + itemName);

                    FontRenderer font = stack.getItem().getFontRenderer(stack);
                    drawHoveringText(list, mouseX, mouseY, (font == null ? fontRendererObj : font));

                    boolean leftSide = (mouseX + 12 + fontRendererObj.getStringWidth(itemName) > this.width);
                    drawIngredientTooltip(j, mouseX, mouseY, leftSide);
                    return;
                }
            }
        }
        super.renderToolTip(stack, mouseX, mouseY);
    }
}