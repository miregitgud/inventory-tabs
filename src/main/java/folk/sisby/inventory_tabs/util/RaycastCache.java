package folk.sisby.inventory_tabs.util;

import net.minecraft.world.phys.Vec3;

public class RaycastCache {
    public int ticksInvalid = 0;
    public boolean validThisTick = true;
    public Vec3 lastValidOffset = null;

    public void tick() {
        if (!validThisTick) ticksInvalid++;
        validThisTick = false;
    }

    public void hit(Vec3 offset) {
        lastValidOffset = offset;
        validThisTick = true;
        ticksInvalid = 0;
    }
}
