package com.logitow.logimine.update;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.event.LogiMineUpdateCheckEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the updating of the mod.
 */
public class Updater {
    /**
     * URL of the update.json file.
     */
    private static String updateInfoUrl = "https://raw.githubusercontent.com/itsMatoosh/logimine/master/update.json";

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Checks update for the mod.
     */
    public static void checkUpdates() {
        System.out.println("Checking LogiMine updates");
        executorService.submit(() -> {
            try {
                JsonObject updateCheck = downloadUpdateInfo();
                UpdateCheckResult result = determineUpdateCheck(updateCheck);

                MinecraftForge.EVENT_BUS.post(new LogiMineUpdateCheckEvent(updateCheck, result));
                System.out.println("Logimine update check result: " + result);
            } catch (IOException e) {
                MinecraftForge.EVENT_BUS.post(new LogiMineUpdateCheckEvent(null, UpdateCheckResult.ERROR));
                System.out.println("Logimine update check result: " + UpdateCheckResult.ERROR);
            }
        });
    }

    /**
     * Determines the update check result.
     * @param updateObject
     * @return
     */
    private static UpdateCheckResult determineUpdateCheck(JsonObject updateObject) {
        if(updateObject == null) {
            return UpdateCheckResult.ERROR;
        }
        JsonObject promoObject = updateObject.get("promos").getAsJsonObject();
        String versionKey = Minecraft.getMinecraft().getVersion() + "-latest";
        String latestVersionString = promoObject.get(versionKey).getAsString();

        //Comparing the versions.
        Version currentVerion = Version.valueOf(LogiMine.version);
        Version latestVersion = Version.valueOf(latestVersionString);

        if(latestVersion.greaterThan(currentVerion)) {
            return UpdateCheckResult.NEW_VERSION_AVAILABLE;
        } else {
            return UpdateCheckResult.UP_TO_DATE;
        }
    }

    /**
     * Downloads the update info.
     */
    private static JsonObject downloadUpdateInfo() throws IOException {
        File updateFile = File.createTempFile("update", ".json");
        FileUtils.copyURLToFile(new URL(updateInfoUrl), updateFile);
        System.out.println("Copied update check to file " + updateFile.getAbsolutePath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(updateFile));
        return new JsonParser().parse(bufferedReader).getAsJsonObject();
    }

    /**
     * Receives update updates.
     */
    public interface IUpdateNotifee {
        /**
         * Called when update has been checked.
         * @param checkResult
         * @param updateInfo
         */
        public void onUpdateChecked(UpdateCheckResult checkResult, JsonObject updateInfo);
    }

    /**
     * Different results of an update check.
     */
    public enum UpdateCheckResult {
        NEW_VERSION_AVAILABLE,
        UP_TO_DATE,
        ERROR
    }
}
