package com.github.timmyovo.pixelbedwars.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "bedwars_rejoin")
public class PlayerRejoinModel extends BaseModel {
    @Id
    private UUID uuid;
    private String serverName;
}
