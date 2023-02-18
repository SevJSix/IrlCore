package me.txmc.gradlepluginbase.common;

import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTReadLimiter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagEnd;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;

public class NBTTools {

    private static Method loadM;
    private static Method createTagM;

    static {
        try {
            createTagM = NBTBase.class.getDeclaredMethod("createTag", byte.class);
            createTagM.setAccessible(true);
            loadM = NBTBase.class.getDeclaredMethod("load", DataInput.class, int.class, NBTReadLimiter.class);
            loadM.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static NBTBase readBaseFromInput(DataInput input) throws Throwable {
        byte typeId = input.readByte();
        if (typeId == 0) return NBTTagEnd.class.newInstance();
        NBTBase base = (NBTBase) createTagM.invoke(NBTBase.class, typeId);
        input.readUTF();
        loadM.invoke(base, input, 0, NBTReadLimiter.a);
        return base;
    }

    @SneakyThrows
    public static NBTTagCompound readNBT(DataInput input) {
        return (NBTTagCompound) readBaseFromInput(input);
    }

    @SneakyThrows
    public static void writeNBT(NBTTagCompound compound, DataOutput output) {
        output.writeByte(compound.getTypeId());
        if (compound.getTypeId() == 0) return;
        output.writeUTF("");
        Method writeM = NBTBase.class.getDeclaredMethod("write", DataOutput.class);
        writeM.setAccessible(true);
        writeM.invoke(compound, output);
    }
}
