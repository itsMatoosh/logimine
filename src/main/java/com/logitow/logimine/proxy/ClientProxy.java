package com.logitow.logimine.proxy;

import com.google.gson.JsonObject;
import com.logitow.bridge.communication.BluetoothState;
import com.logitow.bridge.communication.Device;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.client.gui.*;
import com.logitow.logimine.event.LogiMineUpdateCheckEvent;
import com.logitow.logimine.event.LogitowBridgeClientEventHandler;
import com.logitow.logimine.networking.LogitowEventForwarder;
import com.logitow.logimine.tiles.TileEntityBlockKey;
import com.logitow.logimine.update.Updater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Created by James on 14/12/2017.
 */
public class ClientProxy extends ServerProxy {
    private static boolean checkedUpdate = false;
    private static EntityPlayer currPlay = null;

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(LogiMine.modId + ":" + id, "inventory"));
    }

    /**
     * Calls the events locally but also forwards them to the server.
     */
    @Override
    public void registerLogitowEvents() {
        //Registering the mod side bridge event.
        MinecraftForge.EVENT_BUS.register(LogitowBridgeClientEventHandler.class);
        MinecraftForge.EVENT_BUS.register(LogitowEventForwarder.class);
        MinecraftForge.EVENT_BUS.register(ClientProxy.class);
    }

    public void setSelectedKeyBlock(BlockPos blockKey) {
        if(blockKey != null && HubGui.instance != null) {
            HubGui.setSelectedKeyBlock(blockKey);
        }
    }

    public void showClientGui(int type) {
        switch (type) {
            case 0:
                Minecraft.getMinecraft().displayGuiScreen(new HubGui());
                break;
            case 1:
                Minecraft.getMinecraft().displayGuiScreen(new DeviceManagerGui());
                break;
            case 2:
                Minecraft.getMinecraft().displayGuiScreen(new LoadStructureGui());
                break;
        }
    }

    public void showSaveStructureGui(TileEntityBlockKey blockKey) {
        if(blockKey != null) {
            Minecraft.getMinecraft().displayGuiScreen(new SaveStructureGui(blockKey));
        }
    }
    public void hideSaveStructureGui() {
        if(SaveStructureGui.open) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }
    public void showBluetoothDialogGui(BluetoothState state) {
        Minecraft.getMinecraft().displayGuiScreen(new BluetoothDialogGui(state));
    }
    public void closeManagersWhenDestroyed(BlockPos destroyed) {
        if(HubGui.instance != null && HubGui.getSelectedKeyBlock() != null && HubGui.getSelectedKeyBlock().getPos().equals(destroyed)) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    public void showConnectNotification(Device device) {
        NotificationToast.showConnect(device);
    }
    public void showDisconnectNotification(Device device) {
        NotificationToast.showDisconnect(device);
    }
    public void notifySavedStructuresPageLoaded(StructuresPage page) {
        //Passing the received page to the gui.
        if(LoadStructureGui.instance != null) {
            LoadStructureGui.instance.onPageLoaded(page);
        }
    }
    public void showUnassignDialog(Device device, TileEntityBlockKey keyBlock) {
        Minecraft.getMinecraft().displayGuiScreen(new UnassignDialogGui(device, keyBlock));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (checkedUpdate) return;
        if(event.player.getEntityWorld().isRemote) return;

        currPlay = event.player;

        //Will check for updates of the mod.
        Updater.checkUpdates();

        checkedUpdate = true;
    }

    /**
     * Called when updates for the mod have been fetched.
     * @param updateCheckEvent
     */
    @SubscribeEvent
    public static void onUpdateCheck(LogiMineUpdateCheckEvent updateCheckEvent) {
        if(updateCheckEvent.updateCheckResult == Updater.UpdateCheckResult.NEW_VERSION_AVAILABLE) {
            JsonObject promoObject = updateCheckEvent.updateInfo.get("promos").getAsJsonObject();

            String versionKey = Minecraft.getMinecraft().getVersion() + "-latest";
            String latestVersionString = promoObject.get(versionKey).getAsString();
            String homePage = updateCheckEvent.updateInfo.get("homepage").getAsString();

            TextComponentString updateMessage = new TextComponentString("LogiMine version: " + latestVersionString + " available! Click to download!");
            updateMessage.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, homePage));

            currPlay.sendMessage(updateMessage);

        } else if (updateCheckEvent.updateCheckResult == Updater.UpdateCheckResult.UP_TO_DATE){
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("LogiMine is up to date!"));
        } else {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Error checking updates for LogiMine!"));
        }
    }
}
