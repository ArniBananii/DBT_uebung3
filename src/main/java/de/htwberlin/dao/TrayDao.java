package de.htwberlin.dao;

import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.exceptions.DataException;
import de.htwberlin.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class TrayDao  {
    Connection connection;
    Logger L = LoggerFactory.getLogger(TrayDao.class);
    List<Place> placesFound;
    PlaceDao placeDao = new PlaceDao();


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection useConnection() {
        if (connection == null) {
            throw new CoolingSystemException("Service hat keine Connection");
        }
        return connection;
    }


    public List<Tray> findByDiameterAndCapacity(Integer diameter,Sample sample)throws  CoolingSystemException, DataException {
        List<Tray> trayList= new ArrayList<>();
        placeDao.setConnection(useConnection());
        String sql = String.join(" ",
                "SELECT t.TRAYID, t.CAPACITY, t.EXPIRATIONDATE, t.DIAMETERINCM, COUNT(p.PLACENO)",
                "FROM TRAY t" ,
                "LEFT JOIN PLACE p ON t.TRAYID = p.TRAYID",
                "WHERE t.DIAMETERINCM = ?",
                "GROUP BY t.TRAYID, t.CAPACITY, t.EXPIRATIONDATE, t.DIAMETERINCM",
                "HAVING COUNT(p.PLACENO) < t.CAPACITY"
        );
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, diameter);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Tray tray = new Tray();

                    tray.setTrayID(rs.getInt("TRAYID"));
                    tray.setDiameterInCm(rs.getInt("DIAMETERINCM"));
                    tray.setCapacity(rs.getInt("CAPACITY"));
                    if (tray.getExpirationDate() == null) {
                      tray.setExpirationDate(DateUtils.localDate2SqlDate(DateUtils.sqlDate2LocalDate(sample.getExpirationDate()).plusDays(30)));
                        updateTrayExpiration(tray);
                        placeDao.createPlace(tray, 1,sample);
                    }else{
                        tray.setExpirationDate(rs.getDate("EXPIRATIONDATE"));
                    }
                    trayList.add(tray);
                }
            }catch(SQLException e){
                throw new CoolingSystemException(e.getMessage());
            }
        } catch (SQLException e) {
          throw new DataException(e.getMessage());
        }
        return trayList;
    }

    // dannach nimmm von place = tray wo placeNo muss kleinste sein -> alle places vom tray selecten und nach placeNo sortieren und schaune ist ein place frei = sampleID = null auch checken das
    public List<Tray> findTraysWithFittingExperation(Sample sampleToCheck, List<Tray> trayList)throws CoolingSystemException {
        List<Tray>filteredList = trayList.stream().filter(tray -> tray.getExpirationDate() == null || tray.getExpirationDate().after(sampleToCheck.getExpirationDate())).collect(Collectors.toList());
        if(filteredList.isEmpty()){
            throw new CoolingSystemException("No tray with fitting expiration date found");
        }
        return filteredList;
    }



//    public void findPlaceForSample(Tray tray, Sample sample)throws SQLException{
//        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLACE INNER JOIN TRAY ON TRAY.TRAYID = PLACE.TRAYID WHERE TRAY.TRAYID = ? AND PLACE.SAMPLEID IS NULL")) {
//            preparedStatement.setInt(1, tray.getTrayID());
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                if (!resultSet.next()) {
//                    throw new CoolingSystemException("Sample with ID:"+ sample.getSampleID() + "does not exist!");
//                }while(resultSet.next()) {
//                    Place place = new Place();
//                    place.setPlaceID(resultSet.getInt("PLACEID"));
//                    place.setTrayID(resultSet.getInt("TRAYID"));
//                    place.setPlanceNo(resultSet.getInt("PLACENO"));
//                    placesFound.add(place);
//                }
//            }catch (SQLException e){
//                throw new DataException("Fehler bei der Connection: " + e.getMessage());
//            }
//        }
//        if(placesFound == null){
//        }
//        for (Place place : placesFound) {
//        }
//        }

        private void updateTrayExpiration(Tray tray){
            try(PreparedStatement prepStateForDateToSet = connection.prepareStatement("UPDATE TRAY SET EXPIRATIONDATE = ? WHERE TRAYID = ?")){
                prepStateForDateToSet.setDate(1, tray.getExpirationDate());
                prepStateForDateToSet.setInt(2, tray.getTrayID());
                prepStateForDateToSet.executeUpdate();
            }catch(SQLException e) {
               throw new DataException("Fehler bei der Connection: " + e.getMessage());
            }
        }

    }

