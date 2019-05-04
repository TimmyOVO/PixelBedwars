package com.github.timmyovo.pixelbedwars.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerQuickShopEntryModel extends BaseModel {
    private UUID owner;
    @Id
    private int slotId;
    private String shopItem;

    public static void setPlayerQuickShopEntry(UUID owner, int slotId, String shopItem) {
        PlayerQuickShopEntryModel playerQuickShopEntryModel = new PlayerQuickShopEntryModel(owner, slotId, shopItem);
        try {
            playerQuickShopEntryModel.save();
        } catch (Exception e) {
            playerQuickShopEntryModel.update();
        }
    }
}
