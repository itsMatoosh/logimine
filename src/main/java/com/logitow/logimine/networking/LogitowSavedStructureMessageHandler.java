package com.logitow.logimine.networking;

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
                    if (keyBlock.getAssignedDevice() != null && keyBlock.getAssignedDevice().info.uuid.equals(logitowSavedStructureMessage.deviceUuid)) {
                        keyBlock.getAssignedDevice().disconnect();
                    }
                }
            }
        }

        return null;
    }
}
