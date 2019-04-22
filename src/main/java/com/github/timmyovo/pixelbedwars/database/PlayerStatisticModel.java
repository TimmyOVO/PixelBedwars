package com.github.timmyovo.pixelbedwars.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Data
@Table(name = "battle_player_statistic")
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticModel {
    @Id
    private UUID uuid;
    private int kills;
    private int death;
    private int win;
    private int fail;
}
