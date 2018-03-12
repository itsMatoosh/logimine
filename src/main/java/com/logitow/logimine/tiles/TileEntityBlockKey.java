package com.logitow.logimine.tiles;

import com.logitow.bridge.build.Structure;
import com.logitow.bridge.build.Vec3;
import com.logitow.bridge.build.block.BlockOperation;
import com.logitow.bridge.build.block.BlockOperationType;
import com.logitow.bridge.build.block.BlockType;
import com.logitow.bridge.communication.Device;
import com.logitow.bridge.event.device.block.BlockOperationEvent;
import com.logitow.logimine.LogiMine;
import com.logitow.logimine.blocks.BlockBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.UUID;

/**
 * Tile entity of the key block.
 */
public class TileEntityBlockKey extends TileEntity {

    /**
     * The player assigned to this key block.
     */
    private EntityPlayer assignedPlayer;
    /**
     * The device assigned to this key block.
     */
    private Device assignedDevice;
    /**
     * The structure currently assigned to this key block.
     */
    private Structure assignedStructure;

    private static Logger logger = LogManager.getLogger(TileEntityBlockKey.class);

    /**
     * Registering the tile entity with the active key blocks.
     */
    public TileEntityBlockKey() {
        //Remove duplicates.
        TileEntityBlockKey duplicate = null;
        for (TileEntityBlockKey key :
                LogiMine.activeKeyBlocks) {
            if (key.getPos().equals(this.getPos())) {
                duplicate = key;
                break;
            }
        }
        if(duplicate != null) {
            LogiMine.activeKeyBlocks.remove(duplicate);
        }
        //Adding the block to the keyblock list.
        LogiMine.activeKeyBlocks.add(this);
    }

    /**
     * Writes the vars from this object to nbt.
     * @param compound
     * @return
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        logger.info("Saving NBT for key block: {}", getPos());

        super.writeToNBT(compound);
        NBTTagCompound logitowTag = new NBTTagCompound();
        if(assignedStructure != null) { //Assigned structure
            logitowTag.setUniqueId("structure", assignedStructure.uuid);
        } else {
            logitowTag.removeTag("structure");
        }
        if(assignedPlayer != null) { //Assigned player
            logitowTag.setUniqueId("player", assignedPlayer.getUniqueID());
        } else {
            logitowTag.removeTag("player");
        }

        compound.setTag("LOGITOW", logitowTag);
        return compound;
    }

    /**
     * Reads vars from nbt to this object.
     * @param compound
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("LOGITOW")) {
            logger.info("Loading NBT for key block: {}", getPos());
            
            NBTTagCompound logitowTag = compound.getCompoundTag("LOGITOW");

            //Structure
            if(this.assignedDevice == null) {
                if(logitowTag.hasKey("structure")) {
                    UUID uuid = logitowTag.getUniqueId("structure");
                    if(uuid == null) {
                        this.assignedStructure = null;
                    } else {
                        try {
                            this.assignedStructure = Structure.loadByUuid(uuid.toString());
                        } catch (IOException e) {
                            this.assignedStructure = null;
                        } finally {
                            if (this.assignedStructure != null) {
                                logger.info("Loaded structure from NBT: {}", this.assignedStructure);
                            } else {
                                this.markDirty();
                            }
                        }
                    }
                } else {
                    this.assignedStructure = null;
                }
            } else if(this.assignedStructure != this.assignedDevice.currentStructure) {
                //Assign the current device's structure.
                this.assignedStructure = this.assignedDevice.currentStructure;
                this.markDirty();
            }

            //Player
            if(logitowTag.hasKey("player")) {
                UUID uuid = logitowTag.getUniqueId("player");
                if(uuid == null) {
                    this.assignedPlayer = null;
                } else {
                    this.assignedPlayer = getWorld().getPlayerEntityByUUID(uuid);
                    if(this.assignedPlayer == null) {
                        this.markDirty();
                    }
                }
            } else {
                this.assignedPlayer = null;
            }

            super.readFromNBT(compound);
        }
    }

    /**
     * Gets the device assigned to this block.
     * NULL if device not connected or nothing assigned.
     * @return
     */
    public Device getAssignedDevice() {
        return assignedDevice;
    }

    /**
     * Gets the player assigned to this key block.
     * NULL if device not connected or nothing assigned.
     * @return
     */
    public EntityPlayer getAssignedPlayer() {
        return assignedPlayer;
    }

    /**
     * Gets the structure assigned to this key block.
     * @return
     */
    public Structure getAssignedStructure() {
        return assignedStructure;
    }

    /**
     * Assigns a LOGITOW device to this key block.
     * @param player
     * @param device
     */
    public void assignDevice(EntityPlayer player,Device device) {
        if(player != null && device != null) {
            if(this.assignedDevice == null || !this.assignedDevice.equals(device) || this.assignedPlayer == null || !this.assignedPlayer.equals(player) || this.assignedStructure == null || !this.assignedStructure.equals(device.currentStructure)) {
                this.assignedDevice = device;
                this.assignedPlayer = player;
                this.assignedStructure = device.currentStructure;
                rebuildStructure();
                logger.info("Assigned device: {} to key block at: {}", device, this.getPos());
                this.markDirty();
            }
        } else {
            this.assignedDevice = null;
            this.assignedPlayer = null;
            clearStructure();
            this.assignedStructure = null;
            logger.info("Unassigned device from key block at: {}", this.getPos());
            this.markDirty();
        }
    }

    /**
     * Assigns the given structure to this key block.
     * @param structure
     */
    public void assignStructure(Structure structure) {
        if(structure != null) {
            if(assignedDevice != null || assignedPlayer != null) {
                assignDevice(null, null);
            }
            this.assignedStructure = structure;
            rebuildStructure();
        } else {
            clearStructure();
            this.assignedStructure = null;
        }

        this.markDirty();
    }

    /**
     * Rotates the structure assigned to this key block.
     */
    public boolean rotateStructure(EntityPlayer player, EnumFacing facing)
    {
        if (getWorld().isRemote)return false;

        //Getting the current structure.
        Structure current = getAssignedStructure();
        if(current == null) {
            player.sendMessage(new TextComponentString("Can't rotate, no structure attached!"));
            return false;
        }

        //Getting the rotation to apply.
        Vec3 rotation = Vec3.zero();
        switch(facing) {
            case UP:
                rotation = new Vec3(0,90,0);
                break;
            case DOWN:
                rotation = new Vec3(0,-90,0);
                break;
            case NORTH:
                rotation = new Vec3(0,0,90);
                break;
            case SOUTH:
                rotation = new Vec3(0,0,-90);
                break;
            case EAST:
                rotation = new Vec3(90,0,0);
                break;
            case WEST:
                rotation = new Vec3(-90,0,0);
                break;
        }

        System.out.println("Rotating LOGITOW base block: " + this.getPos() + " by " + rotation);

        //TODO: Position second base block.
        /*switch(currentRotation)
        {
            case 0:
                world.setBlockToAir(blockpos.up());
                world.setBlockState(blockpos.down(),ModBlocks.white_lblock.getDefaultState());
                break;
            case 1:
                world.setBlockToAir(blockpos.down());
                world.setBlockState(blockpos.east(),ModBlocks.white_lblock.getDefaultState());
                break;
            case 2:
                world.setBlockToAir(blockpos.east());
                world.setBlockState(blockpos.west(),ModBlocks.white_lblock.getDefaultState());
                break;
            case 3:
                world.setBlockToAir(blockpos.west());
                world.setBlockState(blockpos.north(),ModBlocks.white_lblock.getDefaultState());
                break;
            case 4:
                world.setBlockToAir(blockpos.north());
                world.setBlockState(blockpos.south(),ModBlocks.white_lblock.getDefaultState());
                break;
            case 5:
                world.setBlockToAir(blockpos.south());
                world.setBlockState(blockpos.up(),ModBlocks.white_lblock.getDefaultState());
                break;

        }*/

        clearStructure();
        assignedStructure.rotate(rotation);
        rebuildStructure();
        player.sendMessage(new TextComponentString("Rotated structure by: " + rotation));

        return true;
    }
    /**
     * Called when the structure data is updated from the assigned device.
     * Called on both client and server.
     * @param event
     */
    public void onStructureUpdate(BlockOperationEvent event) {
        logger.info("Handling block update on key block: {} ", getPos());
        BlockOperation operation = event.operation;

        //No need to recreate the structure each time. Just adding the one updated block.
        //Getting the affected position.
        BlockPos affpos = getPos().add(operation.blockB.coordinate.getX(),operation.blockB.coordinate.getY(),operation.blockB.coordinate.getZ());

        if(getWorld().isRemote) {
            if(operation.operationType == BlockOperationType.BLOCK_ADD) {
                //Block added.
                Block colour = BlockBase.getBlockFromName("logimine:"+operation.blockB.getBlockType().name().toLowerCase()+"_lblock");
                Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(affpos, colour.getDefaultState());
            } else {
                //Block removed.
                IBlockState state = getWorld().getBlockState(affpos);
                Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(affpos, state);
            }
        } else {
            if(operation.operationType == BlockOperationType.BLOCK_ADD) {
                //Block added.
                Block colour = BlockBase.getBlockFromName("logimine:"+operation.blockB.getBlockType().name().toLowerCase()+"_lblock");
                getWorld().setBlockState(affpos, colour.getDefaultState());
            } else {
                //Block removed.
                getWorld().setBlockToAir(affpos);
            }
        }


    }

    /**
     * Clears the current structure.
     */
    public void clearStructure() {
        if(getWorld() == null) return;
        if (getWorld().isRemote) return;

        logger.info("Clearing structure: {} on: {}", assignedStructure, getPos());

        //Removing all the old blocks.
        for (int i = 0; i < this.assignedStructure.blocks.size(); i++) {
            com.logitow.bridge.build.block.Block b = this.assignedStructure.blocks.get(i);
            if(b==null) continue;
            if(b.getBlockType() == BlockType.BASE) continue;

            BlockPos removePosition = this.getPos().add(b.coordinate.getX(), b.coordinate.getY(), b.coordinate.getZ());

            getWorld().setBlockToAir(removePosition);
        }
    }
    /**
     * Rebuilds the current structure.
     */
    public void rebuildStructure() {
        if (getWorld().isRemote)return;

        logger.info("Rebuilding structure: {} on: {}", assignedStructure, getPos());

        //Placing all the new blocks.
        for (com.logitow.bridge.build.block.Block b :
                assignedStructure.blocks) {
            //Block added.
            if(b.getBlockType() != BlockType.BASE) {
                //Getting the affected position.
                BlockPos affpos = this.getPos().add(b.coordinate.getX(),b.coordinate.getY(),b.coordinate.getZ());
                System.out.println("Placing block: " + b + " at: " + affpos);

                Block colour = BlockBase.getBlockFromName("logimine:"+b.getBlockType().name().toLowerCase()+"_lblock");
                getWorld().setBlockState(affpos,colour.getDefaultState());
            }
        }
    }
    /**
     * Checks whether the given player has permissions to this key block.
     * @param player
     * @return
     */
    public boolean checkPermissions(EntityPlayer player) {
        if(getAssignedPlayer() == null || player.getUniqueID() == getAssignedPlayer().getUniqueID()) {
            return true;
        }
        return false;
    }

    /**
     * Called when the world is being saved.
     */
    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save saveEvent) {
        if(saveEvent.getWorld().isRemote) return;

        logger.info("Saving the current structures...");

        for (TileEntityBlockKey keyBlock :
                LogiMine.activeKeyBlocks) {
            //Saving the current structure to file.
            try {
                if(keyBlock.getWorld() != saveEvent.getWorld()) continue;
                if(keyBlock.assignedStructure != null) {
                    keyBlock.assignedStructure.saveToFile();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }
}