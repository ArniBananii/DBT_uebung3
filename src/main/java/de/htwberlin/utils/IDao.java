package de.htwberlin.utils;

import java.sql.Connection;

public interface IDao<T,D> {

     void setConnection(Connection connection);
    Connection useConnection();
   T findBy(D searchTerm);

}
