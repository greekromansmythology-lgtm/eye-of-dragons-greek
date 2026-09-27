package com.life.dragoneyes;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.lang.reflect.Method;

public class ItemDragonEye extends Item {

    public final DragonType type;

    public ItemDragonEye(String name, DragonType type) {
        this.type = type;

        setRegistryName(new ResourceLocation(DragonEyesMod.MODID, name));
        setTranslationKey(DragonEyesMod.MODID + "." + name);
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(16);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {

            // Find nearest matching Ice&Fire dragon (no hard dependency; uses class name)
            Entity dragon = findNearestDragon(world, player, this.type);

            if (dragon == null) {
                player.sendMessage(new TextComponentString(
                        "[DragonEyes] No " + this.type.name() + " dragon found within 512 blocks."
                ));
                return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
            }

            int stage = getDragonStageSafe(dragon);

            // Target = dragon position
            BlockPos target = dragon.getPosition();

            double dx = (target.getX() + 0.5) - player.posX;
            double dz = (target.getZ() + 0.5) - player.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            // Normalize direction on XZ plane
            Vec3d dir = new Vec3d(dx, 0, dz).normalize();

            // Simple compass direction text
            String compass;
            double angle = Math.toDegrees(Math.atan2(-dz, dx));
            if (angle < 0) angle += 360;

            if (angle >= 337.5 || angle < 22.5) compass = "E";
            else if (angle < 67.5) compass = "NE";
            else if (angle < 112.5) compass = "N";
            else if (angle < 157.5) compass = "NW";
            else if (angle < 202.5) compass = "W";
            else if (angle < 247.5) compass = "SW";
            else if (angle < 292.5) compass = "S";
            else compass = "SE";

            if (stage >= 5) {
                player.sendMessage(new TextComponentString(
                        "[DragonEyes] Stage 5 " + this.type.name() +
                                " dragon detected! " +
                                compass + " (" + (int) dist + "m)"
                ));
            } else {
                player.sendMessage(new TextComponentString(
                        "[DragonEyes] " + this.type.name() +
                                " dragon detected. Stage " + stage +
                                " | " + compass + " (" + (int) dist + "m)"
                ));
            }

            // Particle "trail" for ~20 blocks in that direction (visible to client)
            if (world instanceof WorldServer) {
                WorldServer ws = (WorldServer) world;

                double startX = player.posX;
                double startY = player.posY + 1.2; // around head height
                double startZ = player.posZ;

                for (int i = 1; i <= 20; i++) {
                    double px = startX + dir.x * i;
                    double py = startY;
                    double pz = startZ + dir.z * i;

                    ws.spawnParticle(
                            EnumParticleTypes.PORTAL,
                            px, py, pz,
                            2,                 // count
                            0.05, 0.05, 0.05,  // spread
                            0.0                // speed
                    );
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private Entity findNearestDragon(World world, EntityPlayer player, DragonType type) {
        String wantEndsWith;

        switch (type) {
            case FIRE:
                wantEndsWith = "EntityFireDragon";
                break;
            case ICE:
                wantEndsWith = "EntityIceDragon";
                break;
            case LIGHTNING:
                wantEndsWith = "EntityLightningDragon";
                break;
            default:
                return null;
        }

        double maxRange = 512.0;
        double maxRangeSq = maxRange * maxRange;

        Entity nearestAnyDragon = null;
        double nearestAnyDistSq = Double.MAX_VALUE;

        Entity nearestStage5Dragon = null;
        double nearestStage5DistSq = Double.MAX_VALUE;

        for (Entity e : world.loadedEntityList) {
            if (e == null) continue;

            String className = e.getClass().getName();

            // Correct dragon element only
            if (!className.endsWith(wantEndsWith)) continue;

            // Ignore dead dragons
            if (!e.isEntityAlive()) continue;

            double dSq = e.getDistanceSq(player);

            // Ignore dragons farther than 512 blocks
            if (dSq > maxRangeSq) continue;

            int stage = getDragonStageSafe(e);

            // Remember nearest dragon of ANY stage
            if (dSq < nearestAnyDistSq) {
                nearestAnyDistSq = dSq;
                nearestAnyDragon = e;
            }

            // Stage 5 gets priority
            if (stage >= 5 && dSq < nearestStage5DistSq) {
                nearestStage5DistSq = dSq;
                nearestStage5Dragon = e;
            }
        }

        // Always prioritize Stage 5 if one exists
        if (nearestStage5Dragon != null) {
            return nearestStage5Dragon;
        }

        // Otherwise return nearest dragon of any stage
        return nearestAnyDragon;
    }

    private int getDragonStageSafe(Entity dragon) {
        try {
            Method m = dragon.getClass().getMethod("getDragonStage");
            Object out = m.invoke(dragon);

            if (out instanceof Integer) {
                return (Integer) out;
            }
        } catch (Exception ignored) {
        }

        return -1;
    }
}
