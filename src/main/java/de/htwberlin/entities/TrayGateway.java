package de.htwberlin.entities;

import de.htwberlin.utils.IDao;

import java.sql.Connection;

public class TrayGateway implements IDao<Tray,Integer> {

    @Override
    public void setConnection(Connection connection) {

    }

    @Override
    public Connection useConnection() {
        return null;
    }

    @Override
    public Tray findBy(Integer diameter) {
        return null;
    }
}
