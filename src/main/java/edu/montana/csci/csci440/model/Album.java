package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;

    public Album() {
    }

    public Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    //Create

    public boolean create(){

        try(Connection conn = DB.connect()){

            conn.setAutoCommit(false);
            PreparedStatement createAlbum =conn.prepareStatement("INSERT INTO albums (Title,ArtistId) VALUES (?,?)");
            createAlbum.setString(1,this.title);
            createAlbum.setLong(2,this.artistId);
            PreparedStatement getID =conn.prepareStatement("SELECT last_insert_rowID()",
                    Statement.RETURN_GENERATED_KEYS);

            createAlbum.execute();
            getID.execute();
            conn.commit();

            ResultSet ID = getID.getGeneratedKeys();
            if(ID.next()){
                this.albumId = (long) ID.getInt(1);
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

        if(this.title == null){
            this.addError("title must not be null and be of the string data type");
            status = false;
        }

        if(this.artistId == null){
            this.addError("artistId must not be null and be of the long data type");
            status = false;
        }
        return status;
    }
    public boolean update(){
        try(Connection conn = DB.connect()){

            if(verify()){
                conn.setAutoCommit(false);
                PreparedStatement updateAlbum =conn.prepareStatement("UPDATE albums SET Title = ?, " +
                        "ArtistId = ? WHERE albumID = ?");

                updateAlbum.setString(1,this.title);
                updateAlbum.setLong(2,this.artistId);
                updateAlbum.setLong(3, this.albumId);
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

    public void delete(){

        try(Connection conn = DB.connect()){
            conn.setAutoCommit(false);
            PreparedStatement deleteAlbum =conn.prepareStatement("DELETE FROM albums WHERE albums.AlbumId = ? ");
            deleteAlbum.setLong(1,this.albumId);
            deleteAlbum.execute();
            conn.commit();
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
        }
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public void setArtistId(Long ID){
        artistId = ID;
    }
    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) {
        this.albumId = album.getAlbumId();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {

        try(Connection conn = DB.connect()) {
            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM albums LIMIT ? OFFSET ?");
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Album> Albums = new ArrayList<Album>();

            while(result.next()) {
                Albums.add(new Album(result));
            }
            return Albums;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
            e.getSQLState());
            return null;
        }

    }

    public static Album find(long i) {

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM albums WHERE albums.AlbumId = ?");
            x.setLong(1,i);
            ResultSet result = x.executeQuery();
            conn.commit();

            return  new Album(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }

    }

    public static List<Album> getForArtist(Long artistId) {


        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM albums JOIN artists ON albums.ArtistId = artists.ArtistId  WHERE artists.artistId = ?");
            x.setLong(1,artistId);
            ResultSet result = x.executeQuery();
            conn.commit();

            List<Album> albums = new ArrayList<Album>();
            while(result.next()){
                albums.add(new Album(result));
            }
            return albums;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

}
