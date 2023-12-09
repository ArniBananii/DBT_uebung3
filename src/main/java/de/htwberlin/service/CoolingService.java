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


    //my check for now
    trayDao = new TrayDao();
    sampleDao = new SampleDao();
//    placeDao = new PlaceDao();

//    placeDao.setConnection(useConnection());
    sampleDao.setConnection(useConnection());
    trayDao.setConnection(useConnection());

    sample = sampleDao.findBy(sampleId);
    if(sample == null){
      throw new CoolingSystemException("Sample with id " + sampleId + " does not exist");
    }
    trayList = trayDao.findByDiameterAndCapacity(diameterInCM, sample);
    if(trayList == null || trayList.isEmpty()){
      throw new CoolingSystemException("Tray with diameter " + diameterInCM + " does not exist");
    }

//    trayList = trayDao.findTraysWithFittingExperation(sample,trayList);
    trayList.forEach((tray1 -> L.info(tray1.getTrayID().toString())));


  }


}


























