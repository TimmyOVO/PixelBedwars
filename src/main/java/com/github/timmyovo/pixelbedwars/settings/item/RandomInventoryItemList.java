package com.github.timmyovo.pixelbedwars.settings.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RandomInventoryItemList {
    private String name;
    private int chance;
    private List<RandomInventoryItem> randomInventoryItemList;

    public void add(RandomInventoryItem randomInventoryItem) {
        this.randomInventoryItemList.add(randomInventoryItem);
    }
}
