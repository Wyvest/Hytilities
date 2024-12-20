/*
 * Hytils Reborn - Hypixel focused Quality of Life mod.
 * Copyright (C) 2020, 2021, 2022, 2023  Polyfrost, Sk1er LLC and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.polyfrost.hytils.handlers.cache;

import net.hypixel.data.type.GameType;
import org.polyfrost.oneconfig.api.event.v1.events.HypixelLocationEvent;
import org.polyfrost.oneconfig.api.event.v1.invoke.EventHandler;
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils;
import org.polyfrost.oneconfig.utils.v1.JsonUtils;
import org.polyfrost.oneconfig.utils.v1.Multithreading;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Objects;

public class BedLocationHandler {
    public static final BedLocationHandler INSTANCE = new BedLocationHandler();
    private static final HashMap<String, Integer> COLORS = new HashMap<String, Integer>() {{
        put("yellow", 0);
        put("aqua", 1);
        put("white", 2);
        put("pink", 3);
        put("gray", 4);
        put("red", 5);
        put("blue", 6);
        put("green", 7);
    }};
    public static final HashMap<Integer, String> COLORS_REVERSE = new HashMap<Integer, String>() {{
        put(0, "yellow");
        put(1, "aqua");
        put(2, "white");
        put(3, "pink");
        put(4, "gray");
        put(5, "red");
        put(6, "blue");
        put(7, "green");
    }};
    private int[] defaultBedLocations = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    private boolean setDefault = false;
    private int[] bedLocations = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    private JsonObject locations = null;
    private String lastServer = null;

    private BedLocationHandler() {
        EventHandler.of(HypixelLocationEvent.class, (event) -> {
            HypixelUtils.Location location = event.getLocation();
            String serverId = location.getServerName().orElse(null);
            if (Objects.equals(lastServer, serverId)) {
                return;
            }
            lastServer = serverId;

            if (!location.inGame() || location.getGameType().orElse(null) != GameType.BEDWARS) {
                return;
            }

            bedLocations = null;
            if (getBedLocations() != null) {
                Minecraft.getMinecraft().renderGlobal.loadRenderers();
            }
        }).register();
    }

    public void initialize() {
        Multithreading.submit(() -> {
            JsonElement maybeBedLocations = JsonUtils.parseFromUrl("https://data.woverflow.cc/bed_locations.json");
            if (maybeBedLocations == null) {
                return;
            }
            locations = maybeBedLocations.getAsJsonObject();
        });
    }

    private int[] processColors(JsonArray array) {
        int[] colors = new int[8];
        int i = -1;
        for (JsonElement element : array) {
            i++;
            JsonPrimitive value = element.getAsJsonPrimitive();
            colors[i] = value.isString() ? COLORS.getOrDefault(value.getAsString(), 5) : value.getAsInt();
        }
        return colors;
    }

    public int[] getBedLocations() {
        HypixelUtils.Location location = HypixelUtils.getLocation();
        if (locations == null || !location.inGame() || location.getGameType().orElse(null) != GameType.BEDWARS || "BEDWARS_PRACTICE".equals(location.getMode().orElse(null))) {
            return (this.bedLocations = null);
        }

        if (!setDefault) {
            setDefault = true;
            COLORS_REVERSE.clear();
            int i = -1;
            for (JsonElement entry : locations.get("default").getAsJsonArray()) {
                i++;
                COLORS.put(entry.getAsString(), i);
                COLORS_REVERSE.put(i, entry.getAsString());
            }
            defaultBedLocations = COLORS_REVERSE.keySet().stream().mapToInt(Integer::intValue).toArray();
        }
        if (this.bedLocations != null) {
            return this.bedLocations;
        }
        JsonArray overrides = locations.getAsJsonArray("overrides");
        for (JsonElement override : overrides) {
            JsonObject overrideObject = override.getAsJsonObject();
            if (overrideObject.has("maps") && location.getMapName().isPresent()) {
                JsonArray maps = overrideObject.getAsJsonArray("maps");
                for (JsonElement map : maps) {
                    if (map.getAsString().equalsIgnoreCase(location.getMapName().get())) {
                        return (this.bedLocations = processColors(overrideObject.getAsJsonArray("locations")));
                    }
                }
            }
            if (overrideObject.has("modes") && location.getMode().isPresent()) {
                JsonArray modes = overrideObject.getAsJsonArray("modes");
                for (JsonElement mode : modes) {
                    if (mode.getAsString().equalsIgnoreCase(location.getMode().get())) {
                        this.bedLocations = processColors(overrideObject.getAsJsonArray("locations"));
                        return (this.bedLocations = processColors(overrideObject.getAsJsonArray("locations")));
                    }
                }
            }
        }
        return (this.bedLocations = defaultBedLocations);
    }
}
