package com.ducvn.fishingdangersmod.events;

import com.ducvn.fishingdangersmod.FishingDangersMod;
import com.ducvn.fishingdangersmod.config.FishingDangersConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
        Level world = event.getLevel();
        Player playerEntity = event.getEntity();
        if (!world.isClientSide){
            if (playerEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem
            || playerEntity.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof FishingRodItem){
                if (playerEntity.fishing != null && playerEntity.fishing.biting){
                    Random random = new Random();
                    if (random.nextDouble() < FishingDangersConfig.dangers_appear_chance.get()){
                        FishingHook fishingBobberEntity = playerEntity.fishing;
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
                                if (dangerName.equals("minecraft:anvil") || dangerName.equals("minecraft:player") || dangerName.equals("minecraft:lightning")){
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
                                    playerEntity.sendSystemMessage(Component.literal("You just pulled something heavy out of water, better watch your head"));
                                    world.setBlock(new BlockPos(playerEntity.getX(), playerEntity.getY() + 5D, playerEntity.getZ()),
                                            Blocks.DAMAGED_ANVIL.defaultBlockState(),1);
                                }
                                else {
                                    if (dangerName.equals("minecraft:player")){
                                        Random roll = new Random();
                                        List<Player> playerList = new ArrayList<>(world.players());
                                        playerList.remove(event.getEntity());
                                        if (playerList.size() > 0) {
                                            Player unluckyPlayer = playerList.get(roll.nextInt(playerList.size()));
                                            while (unluckyPlayer.level != event.getEntity().level) {
                                                unluckyPlayer = playerList.get(roll.nextInt(playerList.size()));
                                            }
                                            if (unluckyPlayer.isAlive() && !unluckyPlayer.isCreative() && !unluckyPlayer.isSpectator()){
                                                unluckyPlayer.teleportTo(fishingBobberEntity.getX(), fishingBobberEntity.getY(), fishingBobberEntity.getZ());
                                                Component name = Component.literal(unluckyPlayer.getName().getString()).withStyle(
                                                        Style.EMPTY.withColor(TextColor.fromRgb(5636095))
                                                );
                                                playerEntity.sendSystemMessage(Component.literal("Look like ")
                                                                .append(name)
                                                                .append(" like your bait"));
                                                ((ServerLevel) world).addDuringTeleport(unluckyPlayer);
                                            }
                                        }
                                    }
                                    else {
                                        if (dangerName.equals("minecraft:lightning")){
                                            playerEntity.sendSystemMessage(Component.literal("You just took the wrath of Poseidon"));
                                            LightningBolt lightningBoltEntity = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                                            lightningBoltEntity.setPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
                                            world.addFreshEntity(lightningBoltEntity);
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
