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

package org.polyfrost.hytils.handlers.chat.modules.blockers;

import net.hypixel.data.type.GameType;
import org.polyfrost.hytils.config.HytilsConfig;
import org.polyfrost.hytils.handlers.chat.ChatReceiveModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.polyfrost.oneconfig.api.event.v1.events.ChatReceiveEvent;
import org.jetbrains.annotations.NotNull;
import org.polyfrost.oneconfig.api.hypixel.v1.HypixelUtils;

public class BedwarsAdvertisementsRemover implements ChatReceiveModule {
    @Override
    public void onMessageReceived(@NotNull ChatReceiveEvent event) {
        String message = event.getFullyUnformattedMessage();
        if ((message.startsWith("-") && message.endsWith("-")) || (message.startsWith("▬") && message.endsWith("▬")) || (message.startsWith("≡") && message.endsWith("≡")) || (!message.contains(": ")) || (message.contains(Minecraft.getMinecraft().getSession().getUsername().toLowerCase()))) return;
        HypixelUtils.Location location = HypixelUtils.getLocation();
        if (location.getGameType().orElse(null) == GameType.BEDWARS && !location.inGame()
            && getLanguage().chatCleanerBedwarsPartyAdvertisementRegex.matcher(message).find()) {
            event.cancelled = true;
        }
    }

    @Override
    public boolean isEnabled() {
        return HytilsConfig.bedwarsAdvertisements;
    }

    @Override
    public int getPriority() {
        return -1;
    }
}
