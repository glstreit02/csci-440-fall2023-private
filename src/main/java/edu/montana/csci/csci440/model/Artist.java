package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.*;
import java.util.*;

public class Artist extends Model {

    Long artistId;
    String name;

    public Artist() {
    }

    private Artist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        artistId = results.getLong("ArtistId");
    }

    public List<Album> getAlbums(){
        return Album.getForArtist(artistId);
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtist(Artist artist) {
        this.artistId = artist.getArtistId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Artist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Artist> all(int page, int count) {
        try(Connection conn = DB.connect()) {
            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM artists LIMIT ? OFFSET ?");
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Artist> Artists = new ArrayList<Artist>();

            while(result.next()) {
                Artists.add(new Artist(result));
            }
            return Artists;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }


    }

    public static Artist find(long i) {


        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM artists WHERE artistId = ?");
            x.setLong(1,i);
            ResultSet result =  x.executeQuery();
            return new Artist(result);
        }

        catch(SQLException e){
                System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                        e.getSQLState());
                return null;
            }



    }

    public boolean create(){

        try(Connection conn = DB.connect()){

            conn.setAutoCommit(false);
            PreparedStatement createAlbum =conn.prepareStatement("INSERT INTO artists (Name) VALUES (?)");
            createAlbum.setString(1,this.name);
            PreparedStatement getID =conn.prepareStatement("SELECT last_insert_rowID()",
                    Statement.RETURN_GENERATED_KEYS);

            createAlbum.execute();
            getID.execute();
            conn.commit();

            ResultSet ID = getID.getGeneratedKeys();
            if(ID.next()){
                this.artistId = (long) ID.getInt(1);
            }
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return false;
        }
        return true;
    }

 public boolean verify(){
     _errors.clear();
    boolean status = true;

        if(this.name == null){
        this.addError("name must not be null and be of the string data type");
        status = false;
    }
        return status;
 }
 //TODO repair update for album and then do update for artists
    public boolean update(){
        try(Connection conn = DB.connect()){

            if(verify()){
                conn.setAutoCommit(false);
                PreparedStatement updateAlbum =conn.prepareStatement("UPDATE artists SET Name = ?" +
                        "WHERE ArtistId = ?");

                updateAlbum.setString(1,this.name);
                updateAlbum.setLong(2,this.artistId);
                updateAlbum.execute();
                conn.commit();
                return true;
            }
        }
        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return false;
        }
        return true;
    }



}
