package de.htwberlin.dao;


import de.htwberlin.exceptions.CoolingSystemException;
import de.htwberlin.service.CoolingService;
import de.htwberlin.utils.IDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SampleDao implements IDao<Sample, Integer> {

    private static final Logger L = LoggerFactory.getLogger(CoolingService.class);
    Connection connection = null;

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
    public Sample findBy(Integer id) {
        Sample sample = new Sample();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SAMPLE LEFT JOIN SAMPLEKIND ON SAMPLE.SAMPLEKINDID = SAMPLEKIND.SAMPLEKINDID WHERE SAMPLEID = ?")) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new CoolingSystemException("Sample with ID:"+ id + "does not exist!");
                }else{
                    sample.setSampleID(resultSet.getInt("SAMPLEID"));
                    sample.setExpirationDate(resultSet.getDate("EXPIRATIONDATE"));
                    sample.setSampleName(resultSet.getString("TEXT"));
                    sample.setValidNumberOfDays(resultSet.getInt("VALIDNOOFDAYS"));
                }
            }
        } catch (Exception e) {
            throw new CoolingSystemException("Fehler bei der Connection: " + e.getMessage());
        }
        return sample;
    }
}
