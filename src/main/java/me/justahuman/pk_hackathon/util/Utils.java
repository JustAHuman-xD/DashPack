package me.justahuman.pk_hackathon.util;

import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final List<BlockFace> AXIS = new ArrayList<>(List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST));

    public static BlockFace yawToFace(float yaw) {
        return AXIS.get(Math.round(yaw / 90f) & 0x3);
    }

    public static List<BlockFace> getAxis() {
        return AXIS;
    }
}
