package com.github.timmyovo.pixelbedwars.database;

import com.github.timmyovo.pixelbedwars.PixelBedwars;
import io.ebean.EbeanServer;
import io.ebean.bean.EntityBean;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseModel {

    public static EbeanServer db() {
        return PixelBedwars.getPixelBedwars().getDatabaseManagerBase().getEbeanServer();
    }

    public void markAsDirty() {
        db().markAsDirty(this);
    }

    public void markPropertyUnset(String propertyName) {
        ((EntityBean) this)._ebean_getIntercept().setPropertyLoaded(propertyName, false);
    }

    public void save() {
        db().save(this);
    }

    public void flush() {
        db().flush();
    }

    public void update() {
        db().update(this);
    }

    public void insert() {
        db().insert(this);
    }

    public boolean delete() {
        return db().delete(this);
    }

    public boolean deletePermanent() {
        return db().deletePermanent(this);
    }

    public void refresh() {
        db().refresh(this);
    }
}
