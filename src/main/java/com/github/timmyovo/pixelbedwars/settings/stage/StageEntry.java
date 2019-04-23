package com.github.timmyovo.pixelbedwars.settings.stage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StageEntry {
    private String stageName;
    private int stageCounter;
    private int flow;
    private String stageCommand;
    private transient int counter = -2;

    public void init() {
        counter = stageCounter;
    }

    public void tick() {
        if (counter < 0) {
            return;
        }
        if (counter == 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stageCommand);
        }
        this.counter -= 1;
    }
}
