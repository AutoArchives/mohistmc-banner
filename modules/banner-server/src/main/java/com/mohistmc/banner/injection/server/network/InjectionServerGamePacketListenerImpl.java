package com.mohistmc.banner.injection.server.network;

import java.util.Set;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.slf4j.Logger;

public interface InjectionServerGamePacketListenerImpl {

    default CraftPlayer getCraftPlayer() {
        return null;
    }

    default void disconnect(String s) {
    }

    default void chat(String s, PlayerChatMessage original, boolean async) {
    }

    default void handleCommand(String s) {
    }

    default boolean isDisconnected() {
        return false;
    }

    default boolean checkLimit(long timestamp) {
        return false;
    }

    default boolean bridge$processedDisconnect() {
        return false;
    }

    default void setProcessedDisconnect(boolean processedDisconnect) {
    }

    default CraftServer bridge$craftServer() {
        return null;
    }

    default Logger bridge$logger() {
        return null;
    }

    default void detectRateSpam(String s) {

    }
}
