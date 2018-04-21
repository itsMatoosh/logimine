package com.logitow.logimine.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import com.logitow.logimine.LogiMine;

/**
 * Created by James on 14/12/2017.
 * Modified by itsMatoosh
 */
public class ModBlocks {
    public static BlockKey key_lblock = new BlockKey("key_lblock").setCreativeTab(LogiMine.tab);

    //Below blocks will only appear as a part of a structures. They will not be available in creative.
    public static BlockBase white_lblock = new BlockBase(Material.CAKE,"white_lblock");
    public static BlockBase blue_lblock = new BlockBase(Material.CAKE,"blue_lblock");
    public static BlockBase black_lblock = new BlockBase(Material.CAKE,"black_lblock");
    public static BlockBase pink_lblock = new BlockBase(Material.CAKE,"pink_lblock");
    public static BlockBase green_lblock = new BlockBase(Material.CAKE,"green_lblock");
    public static BlockBase purple_lblock = new BlockBase(Material.CAKE,"purple_lblock");
    public static BlockBase yellow_lblock = new BlockBase(Material.CAKE,"yellow_lblock");
    public static BlockBase indigo_lblock = new BlockBase(Material.CAKE,"indigo_lblock");
    public static BlockBase orange_lblock = new BlockBase(Material.CAKE,"orange_lblock");
    public static BlockBase red_lblock = new BlockBase(Material.CAKE,"red_lblock");
    public static BlockBase end_lblock = new BlockBase(Material.CAKE,"end_lblock");



    public static void register(IForgeRegistry<Block> registry) {
        registry.registerAll(
                key_lblock,white_lblock,blue_lblock,black_lblock,pink_lblock,green_lblock,purple_lblock,yellow_lblock, indigo_lblock, orange_lblock, red_lblock, end_lblock
        );

    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        registry.registerAll(
                key_lblock.createItemBlock(),white_lblock.createItemBlock(),blue_lblock.createItemBlock(),black_lblock.createItemBlock(),
                pink_lblock.createItemBlock(), green_lblock.createItemBlock(),purple_lblock.createItemBlock(),yellow_lblock.createItemBlock(), indigo_lblock.createItemBlock(), orange_lblock.createItemBlock(), red_lblock.createItemBlock(), end_lblock.createItemBlock()
        );

    }

    public static void registerModels() {
        key_lblock.registerItemModel(Item.getItemFromBlock(key_lblock));
        white_lblock.registerItemModel(Item.getItemFromBlock(white_lblock));
        blue_lblock.registerItemModel(Item.getItemFromBlock(blue_lblock));
        black_lblock.registerItemModel(Item.getItemFromBlock(black_lblock));
        pink_lblock.registerItemModel(Item.getItemFromBlock(pink_lblock));
        green_lblock.registerItemModel(Item.getItemFromBlock(green_lblock));
        purple_lblock.registerItemModel(Item.getItemFromBlock(purple_lblock));
        yellow_lblock.registerItemModel(Item.getItemFromBlock(yellow_lblock));
        indigo_lblock.registerItemModel(Item.getItemFromBlock(indigo_lblock));
        orange_lblock.registerItemModel(Item.getItemFromBlock(orange_lblock));
        red_lblock.registerItemModel(Item.getItemFromBlock(red_lblock));
        end_lblock.registerItemModel(Item.getItemFromBlock(end_lblock));
    }
}
