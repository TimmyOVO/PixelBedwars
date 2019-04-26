package com.github.timmyovo.pixelbedwars.shop.category;

import com.github.skystardust.ultracore.bukkit.models.InventoryItem;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.github.timmyovo.pixelbedwars.shop.item.ShopItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.github.timmyovo.pixelbedwars.shop.ShopGui.SHOP_ITEM_KEY;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShopCategory {
    private InventoryItem icon;
    private Map<Integer, ShopItem> shopItemMap;
    private String requirePermission;

    public static ShopCategory fromString(String string) {
        return FileUtils.GSON.fromJson(string, ShopCategory.class);
    }

    public void applyToInventory(Inventory inventory) {
        if (getShopItemMap() == null) {
            for (int i = 0; i < 18; i++) {
                inventory.setItem(i + 17, new ItemStack(Material.AIR));
            }
            return;
        }
        getShopItemMap().forEach((i1, shopItem) -> {
            ItemStack itemStack = shopItem.getIcon().toItemStack();
            net.minecraft.server.v1_8_R3.ItemStack asNMSCopy = CraftItemStack.asNMSCopy(itemStack);
            if (asNMSCopy == null) {
                inventory.setItem(i1, new ItemStack(Material.AIR));
                return;
            }
            if (asNMSCopy.getTag() == null) {
                asNMSCopy.setTag(new NBTTagCompound());
            }
            asNMSCopy.getTag().setString(SHOP_ITEM_KEY, shopItem.toString());
            inventory.setItem(i1, CraftItemStack.asBukkitCopy(asNMSCopy));
        });
    }

    @Override
    public String toString() {
        return FileUtils.GSON.toJson(this);
    }
}
