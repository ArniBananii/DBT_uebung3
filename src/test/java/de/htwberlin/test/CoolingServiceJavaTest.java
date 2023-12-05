package de.htwberlin.test;

import java.net.URL;
import java.sql.SQLException;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.service.CoolingService;
import de.htwberlin.service.ICoolingService;
import de.htwberlin.test.utils.DbUnitUtils;
import de.htwberlin.utils.DbCred;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CoolingServiceJavaTest {
  private static final Logger L = LoggerFactory.getLogger(CoolingServiceJavaTest.class);
  private static IDatabaseTester dbTester;
  private static IDatabaseConnection dbTesterCon = null;
  private static String dataDirPath = "de/htwberlin/test/data/service/";
  private static URL dataFeedUrl = ClassLoader.getSystemResource(dataDirPath);
  private static IDataSet feedDataSet = null;

  private static ICoolingService cs = new CoolingService();

  @BeforeClass
  public static void setUp() {
    L.debug("setUp: start");
    try {
      dbTester = new JdbcDatabaseTester(DbCred.driverClass, DbCred.url, DbCred.user, DbCred.password, DbCred.schema);
      dbTesterCon = dbTester.getConnection();
      feedDataSet = new CsvURLDataSet(dataFeedUrl);
      dbTester.setDataSet(feedDataSet);
      DatabaseOperation.CLEAN_INSERT.execute(dbTesterCon, feedDataSet);
      cs.setConnection(dbTesterCon.getConnection());
    } catch (Exception e) {
      DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
      throw new RuntimeException(e);
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    L.debug("tearDown: start");
    DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
  }

  /**
   * SampleID existiert nicht.
   */
  @org.junit.Test(expected = CoolingSystemException.class)
  public void testCoolingService01() {
    Integer nonexistentSampleId = 999;
    Integer validDiameter = 1;
    cs.transferSample(nonexistentSampleId, validDiameter);
  }

  /**
   * Kein Tablett mit passenden Durchmesser vorhanden.
   */
  @org.junit.Test(expected = CoolingSystemException.class)
  public void testCoolingService02() {
    Integer validSampleId = 1;
    Integer diameterTooLarge = 999;
    cs.transferSample(validSampleId, diameterTooLarge);
  }

  /**
   * Ablaufdatum Sample zu gross. Kein freies Tablett richtiger Größe vorhanden.
   */
  @org.junit.Test(expected = CoolingSystemException.class)
  public void testCoolingService03() {
    Integer sampleIdWithExpirationToFarInFuture = 1;
    Integer validDiameter = 1;
    cs.transferSample(sampleIdWithExpirationToFarInFuture, validDiameter);
  }

  /**
   * Ablaufdatum Probe nicht zu gross. Alle passenden Tabletts voll. Kein freies
   * Tablett richtiger Groesse vorhanden
   */
  @org.junit.Test(expected = CoolingSystemException.class)
  public void testCoolingService04() {
    Integer sampleIdWithExpirationOk = 2;
    Integer diameterPossible = 1;
    cs.transferSample(sampleIdWithExpirationOk, diameterPossible);
  }

  /**
   * Ablaufdatum Probe zu gross. Freies Tablett richtiger Groesse vorhanden
   * @throws SQLException 
   * @throws DatabaseUnitException 
   */
  @org.junit.Test
  public void testCoolingService05() throws SQLException, DatabaseUnitException {
    Integer sampleIdWithExpirationToFarInFuture = 3;
    Integer validDiameter = 2;
    cs.transferSample(sampleIdWithExpirationToFarInFuture, validDiameter);

    // Lade tatsaechliche Daten aus der Datenbank
    QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
    String sql1 = "select * from Tray where TrayID=2 order by TrayId";
    databaseDataSet.addTable("Tray", sql1);
    String sql2 = "select * from Place where TrayID=2 order by TrayId, PlaceNo";
    databaseDataSet.addTable("Place", sql2);
    ITable actualTableTray = databaseDataSet.getTable("Tray");
    ITable actualTablePlace = databaseDataSet.getTable("Place");

    // Lade erwartete Daten
    URL url = ClassLoader.getSystemResource(dataDirPath + "post01/");
    IDataSet expectedDataSet = new CsvURLDataSet(url);
    ITable expectedTableTray = expectedDataSet.getTable("Tray");
    ITable expectedTablePlace = expectedDataSet.getTable("Place");

    Assertion.assertEquals(expectedTableTray, actualTableTray);
    Assertion.assertEquals(expectedTablePlace, actualTablePlace);
  }  
  
  /**
   * Ablaufdatum Probe ok. Alle passenden Tabletts voll. Freies Tablett
   * richtiger Größe vorhanden.
   */
  @org.junit.Test
  public void testCoolingService06() throws SQLException, DatabaseUnitException {
    Integer validSampleId = 5;
    Integer validDiameter = 3;
    cs.transferSample(validSampleId, validDiameter);

    // Lade tatsaechliche Daten aus der Datenbank
    QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
    String sql1 = "select * from Tray where TrayID in (3,4) order by TrayId";
    databaseDataSet.addTable("Tray", sql1);
    String sql2 = "select * from Place where TrayID in (3,4) order by TrayId, PlaceNo";
    databaseDataSet.addTable("Place", sql2);
    ITable actualTableTray = databaseDataSet.getTable("Tray");
    ITable actualTablePlace = databaseDataSet.getTable("Place");

    // Lade erwartete Daten
    URL url = ClassLoader.getSystemResource(dataDirPath + "post02/");
    IDataSet expectedDataSet = new CsvURLDataSet(url);
    ITable expectedTableTray = expectedDataSet.getTable("Tray");
    ITable expectedTablePlace = expectedDataSet.getTable("Place");

    Assertion.assertEquals(expectedTableTray, actualTableTray);
    Assertion.assertEquals(expectedTablePlace, actualTablePlace);
  }

  /**
   * Ablaufdatum fuer 2 Proben ok. Platz am Ende frei.
   * @throws DatabaseUnitException 
   */
  @org.junit.Test
  public void testCoolingService07() throws DatabaseUnitException {
    Integer validSampleId1 = 7;
    Integer validSampleId2 = 8;
    Integer validDiameter = 4;
    cs.transferSample(validSampleId1, validDiameter);
    cs.transferSample(validSampleId2, validDiameter);

    // Lade tatsaechliche Daten aus der Datenbank
    QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
    String sql1 = "select * from Tray where TrayID=5 order by TrayId";
    databaseDataSet.addTable("Tray", sql1);
    String sql2 = "select * from Place where TrayID=5 order by TrayId, PlaceNo";
    databaseDataSet.addTable("Place", sql2);
    ITable actualTableTray = databaseDataSet.getTable("Tray");
    ITable actualTablePlace = databaseDataSet.getTable("Place");

    // Lade erwartete Daten
    URL url = ClassLoader.getSystemResource(dataDirPath + "post03/");
    IDataSet expectedDataSet = new CsvURLDataSet(url);
    ITable expectedTableTray = expectedDataSet.getTable("Tray");
    ITable expectedTablePlace = expectedDataSet.getTable("Place");

    Assertion.assertEquals(expectedTableTray, actualTableTray);
    Assertion.assertEquals(expectedTablePlace, actualTablePlace);
  }

  /**
   * Ablaufdatum fuer 2 Proben ok. Platz zwischendrin frei.
   * @throws DatabaseUnitException 
   */
  @org.junit.Test
  public void testCoolingService08() throws DatabaseUnitException {
    Integer validSampleId1 = 11;
    Integer validSampleId2 = 12;
    Integer validDiameter = 5;
    cs.transferSample(validSampleId1, validDiameter);
    cs.transferSample(validSampleId2, validDiameter);

    // Lade tatsaechliche Daten aus der Datenbank
    QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
    String sql1 = "select * from Tray where TrayID=6 order by TrayId";
    databaseDataSet.addTable("Tray", sql1);
    String sql2 = "select * from Place where TrayID=6 order by TrayId, PlaceNo";
    databaseDataSet.addTable("Place", sql2);
    ITable actualTableTray = databaseDataSet.getTable("Tray");
    ITable actualTablePlace = databaseDataSet.getTable("Place");

    // Lade erwartete Daten
    URL url = ClassLoader.getSystemResource(dataDirPath + "post04/");
    IDataSet expectedDataSet = new CsvURLDataSet(url);
    ITable expectedTableTray = expectedDataSet.getTable("Tray");
    ITable expectedTablePlace = expectedDataSet.getTable("Place");

    Assertion.assertEquals(expectedTableTray, actualTableTray);
    Assertion.assertEquals(expectedTablePlace, actualTablePlace);
  }

}
