package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.htwberlin.exceptions.CoolingSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.DataException;

public class CoolingService implements ICoolingService {

  // Test commit

  private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
  private Connection connection;

  @Override
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  @SuppressWarnings("unused")
  private Connection useConnection() {
    if (connection == null) {
      throw new DataException("Connection not set");
    }
    return connection;
  }

  @Override
  public void transferSample(Integer sampleId, Integer diameterInCM) {
    L.info("transferSample: sampleId: " + sampleId + ", diameterInCM: " + diameterInCM);

    // Checks if Sample exists
    checkSample(sampleId);

    // Checks if there is a tablet with the given diameter
    checkTabletsWithDiameter(diameterInCM);


    try {

    } catch (Exception e) {
      L.info("Excpetion in der Verbindung zur Datenbank: " + e.getMessage());
    }

  }

  private void checkSample(Integer sampleId) {

    String sql = String.join(" ",
            "SELECT SAMPLEID",
            "FROM SAMPLE",
            "WHERE SAMPLEID = ?"
    );

    try(PreparedStatement stmt = useConnection().prepareStatement(sql)) {
      stmt.setInt(1, sampleId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new CoolingSystemException("Sample with id " + sampleId + " does not exist");
        }
      }
    } catch (Exception e) {
      throw new CoolingSystemException("Sample with id " + sampleId + " does not exist");
    }
  }

  private void checkTabletsWithDiameter(Integer diameterInCM) {
    String sql = String.join(" ",
            "SELECT TRAYID",
            "FROM TRAY",
            "WHERE DIAMETERINCM = ?"
    );

    try(PreparedStatement stmt = useConnection().prepareStatement(sql)) {
      stmt.setInt(1, diameterInCM);
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          throw new CoolingSystemException("No tablet with diameter " + diameterInCM + " exists");
        }
      }
    } catch (SQLException e) {
      L.info("Excpetion in der Verbindung zur Datenbank: " + e.getMessage());
    }
  }

}


























