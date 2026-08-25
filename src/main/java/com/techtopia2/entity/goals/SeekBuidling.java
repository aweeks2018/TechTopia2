package com.techtopia2.entity.goals;

import java.util.Optional;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.techtopia2.VillageData;
import com.techtopia2.VillageData.Building;
import com.techtopia2.VillageData.Village;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class SeekBuidling extends Goal
{
    private int delayCounter = 0;
    
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Mob mob;
    //protected final Building building;
    

    public SeekBuidling(Mob mob)
    {
        this.mob = mob;
        //this.building = building;
    }

    @Override
    public boolean canUse() 
    {
        // If there are no Villages or Town_halls resgitered, this goal can not be used

        // Else it can be used
        return true;
    }

    @Override
    public void start() 
    {
        this.delayCounter = 0;
    }

    @Override
    public void tick() 
    {
        if (--this.delayCounter <= 0) 
        {
            this.delayCounter = 20; // Re-calculate path once every 20 ticks (1 second)

            ServerLevel serverLevel = (ServerLevel)this.mob.level();
            Vec3i mobPosVec3i = new Vec3i(this.mob.getBlockX(), this.mob.getBlockY(), this.mob.getBlockZ());
            Optional<Village> nearestVillage = VillageData.get(serverLevel).findVillage(mobPosVec3i, serverLevel);
            if (nearestVillage.isPresent())
            {
                LOGGER.info("SeekBuilding Tick");

                Building townHall = nearestVillage.get().townHall();
                BlockPos targetPos = townHall.position();
                var navigation = this.mob.getNavigation();

                navigation.moveTo(
                    targetPos.getX(), 
                    targetPos.getY(), 
                    targetPos.getZ(), 
                    1.0
                );
            }
            else
            {
                LOGGER.info("No nearest Village Present"); 
                LOGGER.info("Block Position for Villager: " + mobPosVec3i.toString());
            }
        }
    }

    @Override
    public void stop()
    {
        // Reset pathfinding when the goal stops or gets interrupted
        this.mob.getNavigation().moveTo(0, 0, 0, 0); // Stops the mob in its tracks
    }
}
