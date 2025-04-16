package io.lumine.mythic.lib.gui.editable.item;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.gui.editable.GeneratedInventory;
import io.lumine.mythic.lib.gui.editable.placeholder.Placeholders;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PhysicalItem<T extends GeneratedInventory> extends InventoryItem<T> {
    private final String id, texture;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int customModelDataInt;
    private final String customModelDataString;
    private final NamespacedKey itemModel;
    private final boolean hideFlags, hideTooltip;

    protected final DecimalFormat ONE_DIGIT = new DecimalFormat("0.#");

    public PhysicalItem(@NotNull ConfigurationSection config) {
        this(null, config);
    }

    public PhysicalItem(@Nullable InventoryItem<T> parent, @NotNull ConfigurationSection config) {
        super(parent, config);

        this.id = config.getName();
        this.material = config.getString("item") != null ? Material.valueOf(UtilityMethods.enumName(config.getString("item"))) : Material.DIRT;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.hideTooltip = config.getBoolean("hide-tooltip");
        this.customModelDataInt = config.getInt("custom-model-data");
        this.customModelDataString = config.contains("custom-model-data-string") ? config.getString("custom-model-data-string") : null;
        this.itemModel = config.contains("item-model") ? NamespacedKey.fromString(config.getString("item-model")) : null;
        this.texture = config.getString("texture");
    }
/*
    @Deprecated
    public String getId() {
        return id;
    }

    @Deprecated
    public Material getMaterial() {
        return material;
    }

    @Deprecated
    public boolean hideFlags() {
        return hideFlags;
    }

    @Deprecated
    public boolean hasName() {
        return name != null;
    }

    @Deprecated
    public String getName() {
        return name;
    }

    @Deprecated
    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    @Deprecated
    public List<String> getLore() {
        return lore;
    }

    @Deprecated
    public int getCustomModelDataInt() {
        return customModelDataInt;
    }

    @Deprecated
    public String getCustomModelDataString() {
        return customModelDataString;
    }
 */

    @Nullable
    public ItemStack getDisplayedItem(@NotNull T inv, int n) {
        return getDisplayedItem(inv, ItemOptions.index(n));
    }


    /**
     * @param inv     Generated inventory being opened by a player
     * @param options Options when generating the item
     * @return Item that will be displayed in the generated inventory
     */
    @Nullable
    public ItemStack getDisplayedItem(T inv, ItemOptions options) {
        Placeholders placeholders = getPlaceholders(inv, options.index());
        OfflinePlayer effectivePlayer = getEffectivePlayer(inv, options.index());
        ItemStack item = new ItemStack(options.material(material));

        // Meta can sometimes be null with AIR for instance)
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // Display name
            if (name != null)
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', placeholders.apply(effectivePlayer, name)));

            // Hide flags
            if (hideFlags) meta.addItemFlags(ItemFlag.values());
            if (hideTooltip) meta.setHideTooltip(true);

            // Lore
            if (lore != null) {
                List<String> lore = new ArrayList<>();
                for (String line : this.lore) {
                    //Enables to have placeholders for a list of item. Color codes for the placeholders also (e.g player can introduce color codes in their input).
                    String[] parsed = ChatColor.translateAlternateColorCodes('&', placeholders.apply(effectivePlayer, line)).split("\n");
                    for (String str : parsed) {
                        lore.add(ChatColor.GRAY + str);
                    }
                }
                meta.setLore(lore);
            }

            // Custom model data integer
            int customModelDataInt = options.customModelData(this.customModelDataInt);
            if (customModelDataInt != 0) meta.setCustomModelData(customModelDataInt);

            // Custom model data string
            String customModelDataComponent = options.customModelDataString(customModelDataString);
            if (customModelDataComponent != null) {
                CustomModelDataComponent comp = meta.getCustomModelDataComponent();
                comp.setStrings(Collections.singletonList(customModelDataComponent));
                meta.setCustomModelDataComponent(comp);
            }

            // Item model
            if (itemModel != null) meta.setItemModel(itemModel);

            // Skull texture
            if (texture != null && meta instanceof SkullMeta) UtilityMethods.setTextureValue((SkullMeta) meta, texture);

            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    public abstract Placeholders getPlaceholders(T inv, int n);
}
