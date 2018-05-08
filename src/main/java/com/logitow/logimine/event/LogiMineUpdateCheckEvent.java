package com.logitow.logimine.event;

import com.google.gson.JsonObject;
import com.logitow.logimine.update.Updater;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when the update check for the mod is completed.
 */
public class LogiMineUpdateCheckEvent extends Event {

    public JsonObject updateInfo;
    public Updater.UpdateCheckResult updateCheckResult;

    public LogiMineUpdateCheckEvent(JsonObject updateInfo, Updater.UpdateCheckResult updateCheckResult) {
        this.updateInfo = updateInfo;
        this.updateCheckResult = updateCheckResult;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
