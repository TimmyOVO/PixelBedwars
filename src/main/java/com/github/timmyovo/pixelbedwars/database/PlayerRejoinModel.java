package com.github.timmyovo.pixelbedwars.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "bedwars_rejoin")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerRejoinModel extends BaseModel {
    @Id
    private UUID uuid;
    private String serverName;
}
