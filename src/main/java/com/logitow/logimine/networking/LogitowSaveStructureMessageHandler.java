package com.logitow.logimine.networking;

import com.logitow.bridge.build.Structure;
import com.logitow.bridge.communication.Device;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.tiles.TileEntityBlockKey;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

/**
 * Handles the save structures messages.
 */
public class LogitowSaveStructureMessageHandler implements IMessageHandler<LogitowSaveStructureMessage, LogitowSavedStructureMessage> {

    final ITextComponent TEXT_KEYBLOCK_DOESNT_EXIST = new TextComponentTranslation("logitow.savestructuremanager.keyblockdoesntexist");
    final ITextComponent TEXT_NO_ATTACHED_STRUCTURE = new TextComponentTranslation("logitow.savestructuremanager.nostructureattached");
    final ITextComponent TEXT_SAVING_ERROR = new TextComponentTranslation("logitow.savestructuremanager.savingerror");
    final String TEXT_SAVING_SUCCESFUL_KEY = "logitow.savestructuremanager.savingsuccessful";

    public LogitowSaveStructureMessageHandler(){}

    @Override
    public LogitowSavedStructureMessage onMessage(LogitowSaveStructureMessage message, MessageContext ctx) {
        //Null checks.
        if(message.name == null || message.name == "") {
            return null;
        }
        if(message.keyBlock == null) {
            return null;
        }

        //Getting the sending player.
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

        //Getting the keyblock entity.
        TileEntityBlockKey usedKeyBlock = null;
        for (TileEntityBlockKey keyBlock :
                LogiMine.activeKeyBlocks) {
            if(keyBlock.getWorld() != null && !keyBlock.getWorld().isRemote && keyBlock.getPos().equals(message.keyBlock)) {
                usedKeyBlock = keyBlock;
                break;
            }
        }

        //checking
        if(usedKeyBlock == null) {
            serverPlayer.sendMessage(TEXT_KEYBLOCK_DOESNT_EXIST);
            return new LogitowSavedStructureMessage(false, "");
        }
        if(usedKeyBlock.getAssignedStructure() == null) {
            serverPlayer.sendMessage(TEXT_NO_ATTACHED_STRUCTURE);
            return new LogitowSavedStructureMessage(false, "");
        }

        //adding player uuid to the name.
        String strucName = message.name;
        strucName = strucName.replace('^','v');
        strucName = strucName + "^" + serverPlayer.getUniqueID().toString();

        //deleting old file
        Structure.removeFile(usedKeyBlock.getAssignedStructure());

        //deassociating from the device structure.
        Device device = usedKeyBlock.getAssignedDevice();
        usedKeyBlock.assignDevice(null, null);
        usedKeyBlock.assignStructure(device.currentStructure.clone());

        //setting custom name
        usedKeyBlock.getAssignedStructure().customName = strucName;

        //saving structures
        try {
            usedKeyBlock.getAssignedStructure().saveToFile();
        } catch (IOException e) {
            serverPlayer.sendMessage(TEXT_SAVING_ERROR);
            return new LogitowSavedStructureMessage(false, "");
        }

        //Saved successfully
        serverPlayer.sendMessage(new TextComponentTranslation(TEXT_SAVING_SUCCESFUL_KEY, message.name));

        //No reply
        if(usedKeyBlock.getAssignedDevice() != null) {
            return new LogitowSavedStructureMessage(true, usedKeyBlock.getAssignedDevice().info.uuid);
        } else {
            return new LogitowSavedStructureMessage(true, "");
        }
    }
}
