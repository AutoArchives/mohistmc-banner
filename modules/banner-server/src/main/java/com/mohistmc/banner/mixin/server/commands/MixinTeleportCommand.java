package com.mohistmc.banner.mixin.server.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TeleportCommand.class)
public class MixinTeleportCommand {

    @Shadow @Final private static SimpleCommandExceptionType INVALID_POSITION;
}
