package me.justahuman.pk_hackathon.util;

import me.justahuman.pk_hackathon.PKHackathon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class FreezeHandler extends BukkitRunnable {
    private static final int VANILLA_MAX = 140;
    private static final Map<Integer, FreezeHandler> HANDLERS = new HashMap<>();

    private final LivingEntity entity;
    private int freezeTicks;

    public FreezeHandler(LivingEntity entity, int freezeTicks) {
        this.entity = entity;
        this.freezeTicks = freezeTicks;

        FreezeHandler existingHandler = HANDLERS.get(entity.getEntityId());
        if (existingHandler != null) {
            existingHandler.freezeTicks += freezeTicks;
            return;
        }

        entity.setFreezeTicks(VANILLA_MAX);
        HANDLERS.put(entity.getEntityId(), this);
        runTaskTimer(PKHackathon.instance, 0L, 1L);
    }

    @Override
    public void run() {
        if (!entity.isValid() || --freezeTicks <= 0) {
            cancel();
        } else {
            entity.setFreezeTicks(VANILLA_MAX);
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        HANDLERS.remove(entity.getEntityId());
        super.cancel();
    }
}
