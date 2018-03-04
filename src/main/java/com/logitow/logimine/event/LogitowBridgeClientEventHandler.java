package com.logitow.logimine.event;

import com.logitow.bridge.communication.Device;
import com.logitow.bridge.communication.LogitowDeviceManager;
import com.logitow.bridge.event.Event;
import com.logitow.bridge.event.device.DeviceConnectedEvent;
import com.logitow.bridge.event.device.DeviceConnectionErrorEvent;
import com.logitow.bridge.event.device.DeviceDisconnectedEvent;
import com.logitow.bridge.event.device.battery.DeviceBatteryLowChargeEvent;
import com.logitow.bridge.event.device.battery.DeviceBatteryVoltageUpdateEvent;
import com.logitow.bridge.event.devicemanager.DeviceManagerDiscoveryStartedEvent;
import com.logitow.bridge.event.devicemanager.DeviceManagerDiscoveryStoppedEvent;
import com.logitow.bridge.event.devicemanager.DeviceManagerErrorEvent;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.networking.LogitowDeviceAssignMessage;
import com.logitow.logimine.tiles.TileEntityBlockKey;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles LOGITOW bridge events client side.
 *
 * Created by itsMatoosh
 */
public class LogitowBridgeClientEventHandler {
    @SubscribeEvent
    public static void logitowEventReceived(LogitowBridgeEvent event) {
        //Listening for different event.
        Event bridgeEvent = event.deviceEvent;

        if(bridgeEvent instanceof DeviceConnectedEvent) {
            //Called when a logitow device is connected.
            DeviceConnectedEvent deviceConnectedEvent = (DeviceConnectedEvent) bridgeEvent;

            //Show device connected notification.
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Device " + deviceConnectedEvent.device + " connected!"));

            //Send device assign message.
            for (TileEntityBlockKey keyBlock :
                    LogiMine.activeKeyBlocks) {
                if (keyBlock.getAssignedDevice() == deviceConnectedEvent.device) {
                    LogiMine.networkWrapper.sendToServer(new LogitowDeviceAssignMessage(keyBlock.getPos(), deviceConnectedEvent.device));
                }
            }
        } else if(bridgeEvent instanceof DeviceDisconnectedEvent) {
            //Called when a logitow device is disconnected
            DeviceDisconnectedEvent deviceDisconnectedEvent = (DeviceDisconnectedEvent) bridgeEvent;

            //Show device disconnected notification.
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Device " + deviceDisconnectedEvent.device + " disconnected!"));

            //Make sure all keyblocks are unassigned.
            for (TileEntityBlockKey keyBlock :
                    LogiMine.activeKeyBlocks) {
                if (keyBlock.getAssignedDevice() == deviceDisconnectedEvent.device) {
                    keyBlock.assignDevice(null, null);
                }
            }
        }
        else if (bridgeEvent instanceof DeviceConnectionErrorEvent) {
            //Called when there is a problem connecting to a device.
            DeviceConnectionErrorEvent errorEvent = (DeviceConnectionErrorEvent) bridgeEvent;

            //Show device conn error notification.
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Couldn't connect to device " + errorEvent.device));
        }
        else if(bridgeEvent instanceof DeviceManagerDiscoveryStartedEvent) {
            //Called when device discovery starts.
            DeviceManagerDiscoveryStartedEvent deviceManagerDiscoveryStartedEvent = (DeviceManagerDiscoveryStartedEvent) bridgeEvent;
        } else if(bridgeEvent instanceof DeviceManagerDiscoveryStoppedEvent) {
            //Called when device discovery is stopped.
            DeviceManagerDiscoveryStoppedEvent deviceManagerDiscoveryStoppedEvent = (DeviceManagerDiscoveryStoppedEvent) bridgeEvent;
        } else if(bridgeEvent instanceof DeviceManagerErrorEvent) {
            //Called when there's an error with the device manager.
            //TODO: Handle the error. -> Restart the bridge.
            DeviceManagerErrorEvent deviceManagerErrorEvent = (DeviceManagerErrorEvent) bridgeEvent;
        } else if (bridgeEvent instanceof DeviceBatteryVoltageUpdateEvent) {
            //Called when the device battery voltage is updated.
            //TODO: Show battery state on screen.
            DeviceBatteryVoltageUpdateEvent deviceBatteryVoltageUpdateEvent = (DeviceBatteryVoltageUpdateEvent) bridgeEvent;
        } else if (bridgeEvent instanceof DeviceBatteryLowChargeEvent) {
            //Called when the device battery is below 10% charge.
            //TODO: Show battery low alert.
            DeviceBatteryLowChargeEvent deviceBatteryLowChargeEvent = (DeviceBatteryLowChargeEvent) bridgeEvent;
        }
    }

    /**
     * Called when the world is unloaded.
     * Disconnects all the devices.
     * @param unloadEvent
     */
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload unloadEvent) {
        //Disconnecting all devices.
        for (Device d :
                LogitowDeviceManager.current.connectedDevices) {
            d.disconnect();
        }
    }
}
