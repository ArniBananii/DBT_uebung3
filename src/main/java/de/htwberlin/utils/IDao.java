package de.htwberlin.utils;

import de.htwberlin.entities.Sample;

import java.sql.Connection;
import java.util.Optional;

public interface IDao<T,D> {

     void setConnection(Connection connection);
    Connection useConnection();
   T findBy(D id);

}
