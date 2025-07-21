package me.corruptionhades.jimcver.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FabricMeta {

    private static final Gson gson = new Gson();

    public static @Nullable String getNewestStableVersion() {
        // https://meta.fabricmc.net/v2/versions/game
        /*
        [
          {
            "version": "1.21.4-rc3",
            "stable": false
          },
          {
            "version": "1.21.4-rc2",
            "stable": false
          },
          ...
          ]
         */

        @Nullable String data = apiCall("https://meta.fabricmc.net/v2/versions/game");

        if(data == null) {
            throw new RuntimeException("Failed to get data from meta.fabricmc.net");
        }

        JsonArray obj = gson.fromJson(data, JsonArray.class);

        for(int i = 0; i < obj.size(); i++) {
            JsonObject version = obj.get(i).getAsJsonObject();
            if(version.get("stable").getAsBoolean()) {
                return version.get("version").getAsString();
            }
        }

        return null;
    }

    public static @Nullable String getMappingLink(String mcVersion) {
        // https://meta.fabricmc.net/v1/versions/mappings/1.21.3

        JsonArray obj = gson.fromJson(apiCall("https://meta.fabricmc.net/v1/versions/mappings/" + mcVersion), JsonArray.class);

        // maven: "net.fabricmc:yarn:1.21.3+build.2"
        // maven: https://maven.fabricmc.net/net/fabricmc/yarn/1.21.3%2Bbuild.2/

        JsonObject first = obj.get(0).getAsJsonObject();

        if(first == null) {
            return null;
        }

        // https://maven.fabricmc.net/net/fabricmc/yarn/1.21.3+build.2/yarn-1.21.3%2Bbuild.2.jar

        return "https://maven.fabricmc.net/net/fabricmc/yarn/" + first.get("version").getAsString() + "/yarn-" + first.get("version").getAsString() + ".jar";

    }

    public static @Nullable String getClientLink(String version) {
        // https://launchermeta.mojang.com/mc/game/version_manifest.json

        String response = apiCall("https://launchermeta.mojang.com/mc/game/version_manifest.json");

        if(response == null) {
            throw new RuntimeException("Failed to get data from launchermeta.mojang.com");
        }

        JsonObject obj = gson.fromJson(response, JsonObject.class);
        JsonArray versions = obj.getAsJsonArray("versions");

        String versionJsonlink = null;

        for(int i = 0; i < versions.size(); i++) {
            JsonObject versionObj = versions.get(i).getAsJsonObject();
            if(versionObj.get("id").getAsString().equals(version)) {
                versionJsonlink = versionObj.get("url").getAsString();
                break;
            }
        }

        if(versionJsonlink == null) {
            return null;
        }

        response = apiCall(versionJsonlink);

        if(response == null) {
            throw new RuntimeException("Failed to get data from " + versionJsonlink);
        }

        obj = gson.fromJson(response, JsonObject.class);

        JsonObject downloads = obj.getAsJsonObject("downloads");

        if(downloads == null) {
            return null;
        }

        JsonObject client = downloads.getAsJsonObject("client");

        if(client == null) {
            return null;
        }

        return client.get("url").getAsString();
    }

    private static @Nullable String apiCall(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // get response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
