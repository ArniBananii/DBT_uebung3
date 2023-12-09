package de.htwberlin.dao;

import de.htwberlin.exceptions.CoolingSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class PlaceDao {
    Connection connection = null;
    Logger L = LoggerFactory.getLogger(PlaceDao.class);

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection useConnection() {
        if (connection == null) {
            throw new CoolingSystemException("Service hat keine Connection");
        }
        return connection;
    }

    public void createPlace(Tray tray,Integer placeNo, Sample sample) {
        try (PreparedStatement prepStateForPlaceInsert = useConnection().prepareStatement("INSERT INTO PLACE VALUES (?,?,?)")) {
            prepStateForPlaceInsert.setInt(1, tray.getTrayID());
            prepStateForPlaceInsert.setInt(2, placeNo);
            prepStateForPlaceInsert.setInt(3, sample.getSampleID());
            prepStateForPlaceInsert.executeUpdate();
        } catch (SQLException e) {
            L.error("Excpetion in der Datenbank query: " + e.getMessage());
        }
    }
}
