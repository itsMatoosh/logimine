package com.logitow.logimine.networking;

import com.logitow.bridge.communication.Device;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.tiles.TileEntityBlockKey;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LogitowSavedStructureMessageHandler implements IMessageHandler<LogitowSavedStructureMessage, IMessage> {

    public LogitowSavedStructureMessageHandler(){}

    @Override
    public IMessage onMessage(LogitowSavedStructureMessage logitowSavedStructureMessage, MessageContext messageContext) {
        if(logitowSavedStructureMessage.success) {
            if(logitowSavedStructureMessage.deviceUuid != null && logitowSavedStructureMessage.deviceUuid != "") {
                //Disconnecting the device.
                for (TileEntityBlockKey keyBlock :
                        LogiMine.activeKeyBlocks) {
                    if (keyBlock.getWorld().isRemote && keyBlock.getAssignedDevice() != null && keyBlock.getAssignedDevice().info.uuid.equals(logitowSavedStructureMessage.deviceUuid)) {
                        //LogiMine.networkWrapper.sendToServer(new LogitowDeviceAssignMessage(keyBlock.getPos(), null));
                        Device device = keyBlock.getAssignedDevice();
                        keyBlock.assignDevice(null, null);
                        keyBlock.assignStructure(device.currentStructure.clone());
                    }
                }
            }
        }

        return null;
    }
}
