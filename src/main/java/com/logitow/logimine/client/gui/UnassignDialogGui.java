package com.logitow.logimine.client.gui;

import com.logitow.bridge.communication.Device;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.networking.LogitowDeviceAssignMessage;
import com.logitow.logimine.tiles.TileEntityBlockKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * Represents an unassign device gui dialog.
 */
@SideOnly(Side.CLIENT)
public class UnassignDialogGui extends GuiScreen {
    /**
     * The location of the gui texture.
     */
    final public ResourceLocation guiTexture = new ResourceLocation(LogiMine.modId, "gui/unassign-dialog.png");

    /**
     * The height of the container graphic.
     */
    final int containerHeight = 71;
    /**
     * The width of the container graphic.
     */
    final int containerWidth = 210;

    private Device selectedDevice;
    private TileEntityBlockKey selectedKeyBlock;

    /**
     * ID of the unassign device button.
     */
    final int UNASSIGN_DEVICE_BUTTON_ID = 0;
    /**
     * ID of the disconnect device button.
     */
    final int DISCONNECT_DEVICE_BUTTON_ID = 1;

    public static final ITextComponent TEXT_UNASSIGN_DIALOG_TITLE = new TextComponentTranslation("logitow.unassigndialog.title");
    public static final String TEXT_UNASSIGN_DIALOG_INFO_KEY = "logitow.unassigndialog.info";
    public static final ITextComponent TEXT_UNASSIGN_DIALOG_DISCONNECT = new TextComponentTranslation("logitow.unassigndialog.disconnect");
    public static final ITextComponent TEXT_UNASSIGN_DIALOG_UNASSIGN = new TextComponentTranslation("logitow.unassigndialog.unassign");
    public static final String TEXT_DEVICE_MANAGER_DISCONNECTING = "logitow.devicemanager.disconnecting";
    public static final String TEXT_DEVICE_MANAGER_NOTCONNECTED = "logitow.devicemanager.notconnected";

    public UnassignDialogGui(Device device, TileEntityBlockKey keyBlock) {
        this.selectedDevice = device;
        this.selectedKeyBlock = keyBlock;
    }

    @Override
    protected void actionPerformed(GuiButton p_actionPerformed_1_) throws IOException {
        //Unassign
        Device device = selectedKeyBlock.getAssignedDevice();
        selectedKeyBlock.assignDevice(null, null);
        selectedKeyBlock.assignStructure(device.currentStructure.clone());
        LogiMine.networkWrapper.sendToServer(new LogitowDeviceAssignMessage(selectedKeyBlock.getPos(), null));

        if(p_actionPerformed_1_.id == DISCONNECT_DEVICE_BUTTON_ID) {
            //Disconnecting the device.
            Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(TEXT_DEVICE_MANAGER_DISCONNECTING, selectedDevice));
            if(!selectedDevice.disconnect()) {
                //Not connected
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(TEXT_DEVICE_MANAGER_NOTCONNECTED, selectedDevice));
            }
        }

        //Closing the dialog.
        Minecraft.getMinecraft().displayGuiScreen(null);

        super.actionPerformed(p_actionPerformed_1_);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        drawDefaultBackground();
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);

        //Getting the center coords.
        int centerX = (width/2) - containerWidth/2;
        int centerY = (height/2) - containerHeight/2;

        drawTexturedModalRect(centerX,centerY,0,0,containerWidth,containerHeight);

        //Text depending on bluetooth state.
        drawString(fontRenderer, TEXT_UNASSIGN_DIALOG_TITLE.getFormattedText(), (width/2) - fontRenderer.getStringWidth(TEXT_UNASSIGN_DIALOG_TITLE.getFormattedText())/2, (height/2) - containerHeight/2 + 10, 0xff0055);

        //Drawing each line.
        int lineOffset = 0;
        for (String line :
                new TextComponentTranslation(TEXT_UNASSIGN_DIALOG_INFO_KEY, selectedDevice).getFormattedText().split("/")) {
            drawString(fontRenderer, line, (width/2) - fontRenderer.getStringWidth(line)/2, (height/2) -13 + lineOffset, 0xffffff);
            lineOffset += 11;
        }

        super.drawScreen(p_drawScreen_1_,p_drawScreen_2_,p_drawScreen_3_);
    }

    @Override
    public void initGui() {
        //Checking if a device is selected.
        if(selectedDevice == null || selectedKeyBlock == null) {
            System.out.println("No device assigned!");
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }

        buttonList.clear();
        buttonList.add(new GuiButton(UNASSIGN_DEVICE_BUTTON_ID, width/2 - 40 + 50, height/2 + 11, 80, 20, TEXT_UNASSIGN_DIALOG_UNASSIGN.getFormattedText()));
        buttonList.add(new GuiButton(DISCONNECT_DEVICE_BUTTON_ID, width/2 - 40 - 50, height/2 + 11, 80, 20, TEXT_UNASSIGN_DIALOG_DISCONNECT.getFormattedText()));
        super.initGui();
    }
}
