package kubatech.api.utils;

import java.util.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemID {
    private final Item item;
    private final int count;
    private final int meta;
    private final NBTTagCompound tag;
    private final boolean ignorecount;
    private final boolean ignoremeta;
    private final boolean ignorenbt;

    public static ItemID create(ItemStack stack) {
        return new ItemID(stack, true, true, true, true); // ignore count by default
    }

    public static ItemID create(ItemStack stack, boolean ignorecount) {
        return new ItemID(stack, ignorecount, false, false, true);
    }

    public static ItemID create(ItemStack stack, boolean ignorecount, boolean ignoremeta) {
        return new ItemID(stack, ignorecount, ignoremeta, false, true);
    }

    public static ItemID create(ItemStack stack, boolean ignorecount, boolean ignoremeta, boolean ignorenbt) {
        return new ItemID(stack, ignorecount, ignoremeta, ignorenbt, true);
    }

    public static ItemID create_NoCopy(ItemStack stack) {
        return new ItemID(stack, true, false, false, false); // ignore count by default
    }

    public static ItemID create_NoCopy(ItemStack stack, boolean ignorecount) {
        return new ItemID(stack, ignorecount, false, false, false);
    }

    public static ItemID create_NoCopy(ItemStack stack, boolean ignorecount, boolean ignoremeta) {
        return new ItemID(stack, ignorecount, ignoremeta, false, false);
    }

    public static ItemID create_NoCopy(ItemStack stack, boolean ignorecount, boolean ignoremeta, boolean ignorenbt) {
        return new ItemID(stack, ignorecount, ignoremeta, ignorenbt, false);
    }

    private ItemID(ItemStack stack, boolean ignorecount, boolean ignoremeta, boolean ignorenbt, boolean createcopy) {
        this.ignorecount = ignorecount;
        this.ignoremeta = ignoremeta;
        this.ignorenbt = ignorenbt;
        item = stack.getItem();
        count = ignorecount ? 0 : stack.stackSize;
        meta = ignoremeta ? 0 : stack.getItemDamage();
        tag = ignorenbt ? null : (createcopy ? (NBTTagCompound) stack.stackTagCompound.copy() : stack.stackTagCompound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, count, meta, tag);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof ItemID) return obj.hashCode() == this.hashCode();
        if (obj instanceof ItemStack) {
            if (!item.equals(((ItemStack) obj).getItem())) return false;
            if (!ignorecount) if (count != ((ItemStack) obj).stackSize) return false;
            if (!ignoremeta) if (meta != ((ItemStack) obj).getItemDamage()) return false;
            if (!ignorenbt) {
                if (tag == null) return ((ItemStack) obj).stackTagCompound == null;
                if (!tag.equals(((ItemStack) obj).stackTagCompound)) return false;
            }
            return true;
        }
        return false;
    }
}