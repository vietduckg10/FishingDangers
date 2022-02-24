package com.ducvn.fishingdangersmod.events;

import com.ducvn.fishingdangersmod.FishingDangersMod;
import com.ducvn.fishingdangersmod.config.FishingDangersConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = FishingDangersMod.MODID)
public class FishingDangersEvents {

    @SubscribeEvent
    public static void FishingDangers(PlayerInteractEvent.RightClickItem event){
        World world = event.getWorld();
        PlayerEntity playerEntity = event.getPlayer();
        if (!world.isClientSide){
            if (playerEntity.getItemInHand(Hand.MAIN_HAND).getItem() instanceof FishingRodItem
            || playerEntity.getItemInHand(Hand.OFF_HAND).getItem() instanceof FishingRodItem){
                if (playerEntity.fishing != null && playerEntity.fishing.biting){
                    Random random = new Random();
                    if (random.nextDouble() < FishingDangersConfig.dangers_appear_chance.get()){
                        FishingBobberEntity fishingBobberEntity = playerEntity.fishing;
                        Entity entity = null;
                        List<? extends String> dangersList = FishingDangersConfig.dangers_list.get();
                        List<? extends Double> dangersChance = FishingDangersConfig.chance_list.get();
                        String dangerName = null;
                        Boolean specialType = false;
                        Double spawnNumber = random.nextDouble();
                        Double chanceTracker = 0D;
                        for (int i = 0 ; i < dangersList.size() ; i++){
                            chanceTracker = chanceTracker + dangersChance.get(i);
                            if (chanceTracker > spawnNumber){
                                dangerName = dangersList.get(i);
                                if (dangerName.equals("minecraft:anvil") || dangerName.equals("minecraft:player")){
                                    specialType = true;
                                    break;
                                }
                                entity = EntityType.byString(dangerName).get().create(world);
                                break;
                            }
                        }
                        if (entity != null && specialType == false) {
                            double randomY = random.nextDouble() * 0.5D;
                            double d0 = playerEntity.getX() - fishingBobberEntity.getX();
                            double d1 = playerEntity.getY() - fishingBobberEntity.getY() + randomY;
                            double d2 = playerEntity.getZ() - fishingBobberEntity.getZ();
                            entity.setPos(fishingBobberEntity.getX(), fishingBobberEntity.getY(), fishingBobberEntity.getZ());
                            entity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                            world.addFreshEntity(entity);
                        }
                        else {
                            if (dangerName != null){
                                if (dangerName.equals("minecraft:anvil")){
                                    playerEntity.sendMessage(new StringTextComponent("You just pulled something heavy out of water, better watch your head"), Util.NIL_UUID);
                                    world.setBlock(new BlockPos(playerEntity.getX(), playerEntity.getY() + 5D, playerEntity.getZ()),
                                            Blocks.DAMAGED_ANVIL.defaultBlockState(),1);
                                }
                                else {
                                    if (dangerName.equals("minecraft:player")){
                                        Random roll = new Random();
                                        List<PlayerEntity> playerList = new ArrayList<>(world.players());
                                        playerList.remove(event.getPlayer());
                                        if (playerList.size() > 0) {
                                            PlayerEntity unluckyPlayer = playerList.get(roll.nextInt(playerList.size()));
                                            while (unluckyPlayer.level != event.getPlayer().level) {
                                                unluckyPlayer = playerList.get(roll.nextInt(playerList.size()));
                                            }
                                            if (unluckyPlayer.isAlive() && !unluckyPlayer.isCreative() && !unluckyPlayer.isSpectator()){
                                                unluckyPlayer.teleportTo(fishingBobberEntity.getX(), fishingBobberEntity.getY(), fishingBobberEntity.getZ());
                                                StringTextComponent name = new StringTextComponent(unluckyPlayer.getName().getString());
                                                name.setStyle(Style.EMPTY.withColor(Color.fromRgb(5636095)));
                                                playerEntity.sendMessage((new StringTextComponent("Look like ")
                                                                .append(name)
                                                                .append(" like your bait")), Util.NIL_UUID);
                                                ((ServerWorld) world).updateChunkPos(unluckyPlayer);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
