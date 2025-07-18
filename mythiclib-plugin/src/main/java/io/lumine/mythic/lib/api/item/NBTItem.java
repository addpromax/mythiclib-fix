package io.lumine.mythic.lib.api.item;

import io.lumine.mythic.lib.version.wrapper.VersionWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class NBTItem {
    protected final ItemStack item;

    public NBTItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public abstract Object get(String path);

    public abstract String getString(String path);

    public abstract boolean hasTag(String path);

    public abstract boolean getBoolean(String path);

    public abstract double getDouble(String path);

    public abstract int getInteger(String path);

    public abstract NBTItem setInteger(String path, int value);

    public abstract NBTItem setDouble(String path, double value);

    public abstract NBTItem setString(String path, @NotNull String value);

    public abstract NBTItem setBoolean(String path, boolean value);

    public abstract NBTCompound getNBTCompound(String path);

    @Deprecated
    public abstract NBTItem addTag(List<ItemTag> tags);

    public abstract NBTItem removeTag(String... paths);

    public abstract Set<String> getTags();

    public abstract ItemStack toItem();

    public abstract int getTypeId(String path);

    public NBTItem addTag(ItemTag... tags) {
        return addTag(Arrays.asList(tags));
    }

    public double getStat(String stat) {
        return getDouble("MMOITEMS_" + stat);
    }

    public boolean hasType() {
        return hasTag("MMOITEMS_ITEM_TYPE");
    }

    public abstract void setCanMine(Collection<Material> blocks);

    public String getType() {
        String tag = getString("MMOITEMS_ITEM_TYPE");
        return !tag.equals("") ? tag : null;
    }

    public static NBTItem get(ItemStack item) {
        return VersionWrapper.get().getNBTItem(item);
    }
}

