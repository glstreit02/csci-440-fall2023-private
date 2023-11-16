package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.*;

public class Artist extends Model {

    Long artistId;
    String name;

    String oldName;

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
        if(this.name != null){
            this.oldName = new String(this.name);
            this.name = name;
        }
        else{
            this.name = name;
        }
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

    public void delete(){

        try(Connection conn = DB.connect()){
            conn.setAutoCommit(false);
            PreparedStatement deleteArtist =conn.prepareStatement("DELETE FROM artists WHERE artists.ArtistId = ? ");
            deleteArtist.setLong(1,this.artistId);
            deleteArtist.execute();
            conn.commit();
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
        }
    }

    public boolean update(){
        try(Connection conn = DB.connect()){

            if(verify()){

                conn.setAutoCommit(false);
                PreparedStatement updateAlbum =conn.prepareStatement("UPDATE artists SET Name = ?" +
                        "WHERE artistId = ? AND Name = ?");

                String origName = Artist.find(this.artistId).getName();

                updateAlbum.setString(1,this.name);
                updateAlbum.setLong(2,this.artistId);
                updateAlbum.setString(3,this.oldName);
                int isSuccess = updateAlbum.executeUpdate();
                System.out.println(isSuccess);

                if (isSuccess != 1){
                    conn.rollback();
                    addError("The artist name you have selected to update no longer exists!" +
                            "a sneaky user must have changed it between reading and sending this update request");
                    return false;
                }
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
