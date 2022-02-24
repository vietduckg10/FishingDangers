package com.ducvn.fishingdangersmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FishingDangersConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> dangers_appear_chance;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> dangers_list;
    public static final ForgeConfigSpec.ConfigValue<List<? extends Double>> chance_list;

    static {
        BUILDER.push("Fishing Dangers Config");

        dangers_appear_chance = BUILDER.comment("Danger spawn chance, default:0.1")
                .define("Danger spawn chance", 0.1);
        dangers_list = BUILDER.comment("Dangers list. Example: [\"minecraft:tnt\",\"minecraft:drowned\",\"minecraft:fireball\",\"minecraft:anvil\",\"minecraft:player\"]")
                .defineList("Dangers List", Arrays.asList("minecraft:tnt","minecraft:drowned","minecraft:squid","minecraft:guardian","minecraft:elder_guardian"), (p)
                -> {return p instanceof String;});
        chance_list = BUILDER.comment("Chance of each entity in Dangers list, match with Dangers list order \nFirst number will be the spawn chance of the first entity in danger list \nTotal chance should be between 0 and 1. \nExample: [0.5,0.3,0.15,0.05]")
                .defineList("Chance List", Arrays.asList(0.5,0.3,0.05,0.1,0.05), (p)
                -> {return p instanceof Double;});

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
