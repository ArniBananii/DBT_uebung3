package de.htwberlin.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.htwberlin.dao.*;
import de.htwberlin.exceptions.CoolingSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.DataException;

public class CoolingService implements ICoolingService {
  private SampleDao sampleDao;
  private TrayDao trayDao;
  //private PlaceDao placeDao;
  private Sample sample;
  private Tray tray;
  private List<Tray> trayList;
  // Test commit

  private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
  private Connection connection;

  @Override
  public void setConnection(Connection connection) {
    this.connection = connection;
  }


  private Connection useConnection() {
    if (connection == null) {
      throw new DataException("Connection not set");
    }
    return connection;
  }

  @Override
  public void transferSample(Integer sampleId, Integer diameterInCM) {
    L.info("transferSample: sampleId: " + sampleId + ", diameterInCM: " + diameterInCM);

    trayDao = new TrayDao();
    sampleDao = new SampleDao();

    sampleDao.setConnection(useConnection());
    trayDao.setConnection(useConnection());

    // Initialisiert das Sample Objekt - wenn das Sample nicht existiert, dann wird eine CoolingSystemException geworfen
    sample = sampleDao.findBy(sampleId);
    if(sample == null){
      throw new CoolingSystemException("Sample with id " + sampleId + " does not exist");
    }

    // Diese TrayList enthält alle Trays, die noch freie Plätze haben und der Diameter passt
    trayList = trayDao.findByDiameterAndCapacity(diameterInCM, sample);
    L.info("Traylist wurde erstellt und das Logging für die TrayLists beginnen.");
    for (Tray tray : trayList) {
      L.info("Tray mit der ID: " +  tray.getTrayID() + "wurde zu der Tray List hinzugefügt");
    }
    if(trayList == null || trayList.isEmpty()){
      L.info("Kein Tray mit dem Durchmesser " + diameterInCM + " gefunden");
      throw new CoolingSystemException("Tray with diameter " + diameterInCM + " does not exist");
    }

    // Get final Tray

    Tray finalTray = trayDao.findFinalTray(sample,trayList);
    if (finalTray.getSetExpirationDateManually()) {
      L.info("Das ExpirationDate wird jetzt in die Datenbank geschrieben gesetzt");
      trayDao.updateExpirationDate(finalTray);
    }
    L.info("Es wurde ein final Tray gefunden mit der ID: " + finalTray.getTrayID());
    if(finalTray == null){
      L.info("Kein Final Tray gefunden");
      throw new CoolingSystemException("No Tray found");
    }

    trayDao.findPlace(finalTray, sample);




  }


}
























