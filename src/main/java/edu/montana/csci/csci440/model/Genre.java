package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Genre extends Model {

    private Long genreId;
    private String name;

    private Genre(ResultSet results) throws SQLException {
        name = results.getString("Name");
        genreId = results.getLong("GenreId");
    }

    public Long getGenreId() {
        return genreId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Genre> all() {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM genres");
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Genre> genres = new ArrayList<Genre>();

            while(result.next()) {
                genres.add(new Genre(result));
            }
            return genres;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }


}
