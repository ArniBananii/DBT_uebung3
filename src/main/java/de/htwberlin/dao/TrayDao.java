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


    /**
     * Gibt eine Liste von Trays zurück, die alle noch freie PLätze haben und wo Diameter passt - Es werden hier auch die Trays zurückgegeben, die noch frei sind. Dann wird das ExpirationDate gesetzt
     * @param diameter
     * @param sample
     * @return
     * @throws CoolingSystemException
     * @throws DataException
     */
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
                    if (rs.getDate("EXPIRATIONDATE") == null) {
                        // Tray hat kein Ablaufdatum -> also ist es bisher leer
                        L.info("TRAY HAT KEIN ABLAUFDATUM, ALSO WIRD ES GESETZT");
                        tray.setExpirationDate(DateUtils.localDate2SqlDate(DateUtils.sqlDate2LocalDate(sample.getExpirationDate()).plusDays(30)));
                        tray.setSetExpirationDateManually(true);
                    }else{
                        L.info("TRAY HAT EIN ABLAUFDATUM");
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


    /**
     * Bekommt eine Liste von Trays sowie das Sample und gibt dann den engültigen Tray zurück, wo die Probe hinkommt - wenn es kein Tray gibt, dann wird eine Exception ausgelöst
     * @param sampleToCheck
     * @param trayList
     * @return
     * @throws CoolingSystemException
     */
    public Tray findFinalTray(Sample sampleToCheck, List<Tray> trayList)throws CoolingSystemException {
        Tray finalTray = new Tray();

        // Guckt, ob es nur ein Tray in der Liste gibt und packt dieses in das finalTray() Objekt, ansonsten wird gefiltert
        if(trayList.size() == 1) {
            finalTray = trayList.get(0);
        } else {
            // Filter die Tray-Liste nach dem Ablaufdatum
            List<Tray> trayListFilteredByExpirationDate = trayList.stream().filter(tray -> tray.getExpirationDate().after(sampleToCheck.getExpirationDate())).collect(Collectors.toList());
            for(Tray current : trayListFilteredByExpirationDate) {
                if (!checkIfSampleExistsOnTray(sampleToCheck, current)) {
                    finalTray = current;
                    break;
                }
            }
        }

        if (checkIfSampleExistsOnTray(sampleToCheck, finalTray)) {
            throw new CoolingSystemException("Sample already exists on Tray");
        }

        return finalTray;
    }

    public void updateExpirationDate(Tray tray) {
        LocalDate expirationDate = DateUtils.sqlDate2LocalDate(tray.getExpirationDate());
        L.info("ExpirationDate: " + expirationDate);
        String sql = "UPDATE TRAY SET EXPIRATIONDATE = ? WHERE TRAYID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, DateUtils.localDate2SqlDate(expirationDate));
            stmt.setInt(2, tray.getTrayID());
            stmt.executeUpdate();
            tray.setSetExpirationDateManually(false);
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    public void findPlace(Tray tray, Sample sample) {

        L.info("Starte findPlace Methode");

        Place finalPlace = new Place();

        // Liste mit freien Plätzen
        List<Integer> emptyPlaces = getFreePlacesList(tray);

        // Logging
        L.info("Empty Places logging startet und EmptyPlaces hat eine Länge von" + emptyPlaces.size());
        for (Integer placeNo : emptyPlaces) {
            L.info("Platz: " + placeNo);
        }

        // Liste aufsteigend sortieren
        emptyPlaces.sort(Integer::compareTo);
        int firstPlaceNo = emptyPlaces.get(0);
        // Daten in das Objekt packen
        finalPlace.setPlanceNo(firstPlaceNo);
        finalPlace.setSampleID(sample.getSampleID());
        finalPlace.setTrayID(tray.getTrayID());

        // Packt den Place in die Datenbank
        placeDao.createPlace(finalPlace.getTrayID(), finalPlace.getPlanceNo(), finalPlace.getSampleID());
    }

    private List<Integer> getFreePlacesList(Tray tray) {
        List<Integer> freePlaces = new ArrayList<>();

        // Gets the capacity of the tray
        int capacity = tray.getCapacity();

        // Gets the number of places that are already occupied for that tray
        List<Integer> occupiedPlaces = new ArrayList<>();
        String sql = "SELECT PLACENO FROM PLACE WHERE TRAYID = ?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tray.getTrayID());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    occupiedPlaces.add(rs.getInt("PLACENO"));
                }
            }
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }

        // Add placeNo to freePlaces
        for (int i = 1; i <= capacity; i++) {
            if (!occupiedPlaces.contains(i)) {
                freePlaces.add(i);
            }
        }

        return freePlaces;
    }


    /**
     * Guckt, ob das Sample schon auf dem Tray vorhanden ist
     * @param sampleToCheck
     * @param trayToCheck
     * @return
     */
    private Boolean checkIfSampleExistsOnTray(Sample sampleToCheck, Tray trayToCheck) {

        String sql = "SELECT * FROM SAMPLE INNER JOIN PLACE ON SAMPLE.SAMPLEID = PLACE.SAMPLEID INNER JOIN TRAY ON PLACE.TRAYID = TRAY.TRAYID WHERE TRAY.TRAYID = ? AND SAMPLE.SAMPLEID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, trayToCheck.getTrayID());
            stmt.setInt(2, sampleToCheck.getSampleID());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }catch(SQLException e){
                throw new CoolingSystemException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
        return false;
    }


}
