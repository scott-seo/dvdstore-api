package com.scottwseo.util;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;

/**
 * Created by sseo on 9/7/16.
 */
public class PostgreSQLDatabase {

    public static boolean check() {

        String url = Config.DB_URL.getString();
        String user = Config.DB_USER.getString();
        String password = Config.DB_PWD.getString();
        String validationQuery = "/* API Health Check */ SELECT 1";

        // url e.g. "jdbc:postgresql://host:port/database"
        final DBI dbi = new DBI(url + "?user=" + user + "&password=" + password);

        try (Handle handle = dbi.open()) {
            handle.execute(validationQuery);
            return true;
        }
        catch (UnableToObtainConnectionException e) {
            return false;
        }

    }

}
