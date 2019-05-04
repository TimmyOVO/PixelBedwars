package com.github.timmyovo.pixelbedwars.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "quick_shop")
public class PlayerQuickShopEntryModel extends BaseModel {
    @Id
    private int slotId;
    private UUID owner;
    @Column(columnDefinition = "TEXT")
    private String shopItem;

    public static void setPlayerQuickShopEntry(UUID owner, int slotId, String shopItem) {
        PlayerQuickShopEntryModel playerQuickShopEntryModel = new PlayerQuickShopEntryModel(slotId, owner, shopItem);
        if (db().find(PlayerQuickShopEntryModel.class)
                .where()
                .eq("owner", owner)
                .eq("slotId", slotId)
                .findOne() != null) {
            playerQuickShopEntryModel.update();
        } else {
            playerQuickShopEntryModel.save();
        }
    }
}
