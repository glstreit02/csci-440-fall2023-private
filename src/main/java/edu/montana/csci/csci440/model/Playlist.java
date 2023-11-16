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

public class Playlist extends Model {

    Long playlistId;
    String name;

    public Playlist() {
    }

    public Playlist(ResultSet results) throws SQLException {
        name = results.getString("Name");
        playlistId = results.getLong("PlaylistId");
    }


    public List<Track> getTracks(){

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, " +
                    "tracks.MediaTypeId as MediaTypeId, tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, " +
                    "tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName, albums.Title as AlbumName FROM tracks " +
                    "JOIN playlist_track ON playlist_track.trackId = tracks.trackId " +
                    "JOIN playlists ON playlists.PlaylistId = playlist_track.PlaylistId " +
                    "JOIN albums ON albums.AlbumId = tracks.AlbumId " +
                    "JOIN artists ON artists.ArtistId = albums.ArtistId " +
                    "WHERE playlists.PlaylistId = ? ORDER BY tracks.Name ASC");

            x.setLong(1,this.playlistId);
            ResultSet result =  x.executeQuery();
            List<Track> tracks = new ArrayList<Track>();

            while(result.next()){
                tracks.add(new Track(result));
            }
            return tracks;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<Playlist> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Playlist> all(int page, int count) {

        try(Connection conn = DB.connect()) {
            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM playlists LIMIT ? OFFSET ?");
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Playlist> playlists = new ArrayList<Playlist>();

            while(result.next()) {
                playlists.add(new Playlist(result));
            }
            return playlists;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }

    }

    public static Playlist find(int i) {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM playlists WHERE PlaylistId = ?");
            x.setLong(1,i);
            ResultSet result =  x.executeQuery();
            return new Playlist(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

}
