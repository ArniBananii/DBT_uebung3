package de.htwberlin.dao;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.utils.IDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;


public class TrayDao implements IDao<List<Tray>, Integer> {
    Connection connection;
    Logger L = LoggerFactory.getLogger(TrayDao.class);

    List<Tray> trayList;
    List<Place> placesFound;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection useConnection() {
        if (connection == null) {
            throw new CoolingSystemException("Service hat keine Connection");
        }
        return connection;
    }

    @Override
    public List<Tray> findBy(Integer diameter) {
        String sql = String.join(" ",
                "SELECT TRAYID",
                "FROM TRAY",
                "WHERE DIAMETERINCM = ?"
        );
        try (PreparedStatement stmt = useConnection().prepareStatement(sql)) {
            stmt.setInt(1, diameter);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new CoolingSystemException("No tablet with diameter " + diameter + " exists");
                }
                while (rs.next()) {
                    Tray tray = new Tray();
                    tray.setTrayID(rs.getInt("TRAYID"));
                    tray.setDiameterInCm(rs.getInt("DIAMETERINCM"));
                    tray.setCapacity(rs.getInt("CAPACITY"));
                    tray.setExpirationDate(rs.getDate("EXPIRATIONDATE"));
                    trayList.add(tray);
                }
            }
        } catch (SQLException e) {
            L.error("Excpetion in der Verbindung zur Datenbank: " + e.getMessage());
        }
        return trayList;
    }
    //TODO: auch checken das sampleID nciht bereits in place drin ist!
// davor: IST PLATZ FREI-> trayList checken -> prep statement tray in tray liste quey schreiben -> select * from tray where trayID = ? and capacity > (select count(*) from place where trayID = ?; dann darain rs.next() in tray setzen
    // dannach nimmm von place = tray wo placeNo muss kleinste sein -> alle places vom tray selecten und nach placeNo sortieren und schaune ist ein place frei = sampleID = null auch checken das
    public Tray findBy(Sample sampleToCheck, List<Tray> trayList) {
       Tray trayFound =  trayList.stream().sorted(Comparator.comparing(Tray::getExpirationDate)).filter((tray) -> tray.getExpirationDate().after(sampleToCheck.getExpirationDate())).findFirst().get();
        L.error("something something");
        return trayFound;
    }

    public void findPlaceForSample(Tray tray, Sample sample)throws SQLException{
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLACE INNER JOIN TRAY ON TRAY.TRAYID = PLACE.TRAYID WHERE TRAY.TRAYID = ? AND PLACE.SAMPLEID IS NULL")) {
            preparedStatement.setInt(1, tray.getTrayID());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new CoolingSystemException("Sample with ID:"+ sample.getSampleID() + "does not exist!");
                }while(resultSet.next()) {
                    Place place = new Place();
                    place.setPlaceID(resultSet.getInt("PLACEID"));
                    place.setTrayID(resultSet.getInt("TRAYID"));
                    place.setPlanceNo(resultSet.getInt("PLACENO"));
                    placesFound.add(place);
                }
            }catch (Exception e){
                L.error("something something");
            }
        }
        if(placesFound == null){

        }
        for (Place place : placesFound) {


        }
        }

    }

