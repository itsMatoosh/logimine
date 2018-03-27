package com.logitow.logimine.networking;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.Charset;

/**
 * Message indicating a succesful saving of a structure.
 */
public class LogitowSavedStructureMessage implements IMessage {

    public boolean success;
    public String deviceUuid;

    public LogitowSavedStructureMessage(boolean success, String uuid) {
        this.success = success;
        this.deviceUuid = uuid;
    }
    public LogitowSavedStructureMessage(){}

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.success = byteBuf.readBoolean();
        int uuidLength = byteBuf.readInt();
        byte[] data = new byte[uuidLength];
        byteBuf.readBytes(data);
        this.deviceUuid = new String(data, Charset.forName("UTF-8"));
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeBoolean(this.success);
        byte[] uuid = this.deviceUuid.getBytes(Charset.forName("UTF-8"));
        byteBuf.writeInt(uuid.length);
        byteBuf.writeBytes(uuid);
    }
}

