package me.txmc.gradlepluginbase.utils;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import java.util.List;
import java.util.Random;

@Getter
public class RandomTagGenerator {

    @Getter
    private static RandomTagGenerator instance;
    private final List<NBTTagCompound> compounds;
    private final Random random;

    public RandomTagGenerator(List<NBTTagCompound> compounds) {
        instance = this;
        this.compounds = compounds;
        this.random = new Random();
    }

    public NBTTagCompound getRandomTag() {
        return compounds.get(random.nextInt(compounds.size()));
    }
}
