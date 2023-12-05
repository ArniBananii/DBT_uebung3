package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.htwberlin.entities.Sample;
import de.htwberlin.entities.SampleGateway;
import de.htwberlin.exceptions.CoolingSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.DataException;

public class CoolingService implements ICoolingService {
private SampleGateway sampleGateway;
private Sample sample;
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


    //my check for now
    sampleGateway = new SampleGateway();
    sampleGateway.setConnection(this.connection);
    sample = sampleGateway.findBy(sampleId);
    if(sample == null){
      throw new CoolingSystemException("Sample with id " + sampleId + " does not exist");
    }

    // Checks if Sample exists
//    checkSample(sampleId);

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

  private Map<Integer, List<Integer>> getTrays() {

    Map<Integer, List<Integer>> trays = new HashMap<>();

    String sql = String.join(" ",
            "SELECT * ",
            "FROM TRAY"
    );

    try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while(rs.next()) {
          int trayId = rs.getInt("TRAYID");
          int capacity = rs.getInt("CAPACITY");
          int diameterInCM = rs.getInt("DIAMETERINCM");
          int expirationDate = rs.getInt("EXPIRATIONDATE");
          trays.put(trayId, List.of(diameterInCM, capacity, expirationDate));
        }
      }
    } catch (SQLException e) {
      L.info("Excpetion in der Verbindung zur Datenbank: " + e.getMessage());
    }
    return trays;
  }

}


























