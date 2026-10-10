package net.shune.musae.client.screen;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoopingMusic extends AbstractTickableSoundInstance {

    private final float targetVolume;
    private float currentVolume = 0f;
    private boolean fadingOut = false;

    public LoopingMusic(SoundEvent event, float targetVolume) {
        super(event, SoundSource.MUSIC, RandomSource.create());
        this.targetVolume = targetVolume;
        this.looping = true;
        this.delay = 0;
        this.relative = true;
        this.volume = 0f;
        this.pitch = 1.0f;
    }

    public void fadeOut() {
        this.fadingOut = true;
    }

    @Override
    public void tick() {
        if (fadingOut) {
            currentVolume = Math.max(0f, currentVolume - 0.025f);
            if (currentVolume <= 0f) {
                this.stop();
            }
        } else {
            currentVolume = Math.min(targetVolume, currentVolume + 0.012f);
        }
        this.volume = currentVolume;
    }
}