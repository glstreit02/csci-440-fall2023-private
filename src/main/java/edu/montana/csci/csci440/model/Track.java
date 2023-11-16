package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private String oldName;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String artistName;
    private String albumName;
    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    public Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        artistName = results.getString("ArtistName");
        albumName = results.getString("AlbumName");
    }

    public boolean create(){

        try(Connection conn = DB.connect()){

            conn.setAutoCommit(false);
            PreparedStatement createTrack =conn.prepareStatement("INSERT INTO tracks (Name,AlbumId,MediaTypeId,GenreId,Milliseconds,Bytes,UnitPrice) " +
                    "VALUES (?,?,?,?,?,?,?)");


            createTrack.setString(1,this.name);
            createTrack.setLong(2,this.albumId);
            createTrack.setLong(3,this.mediaTypeId);
            createTrack.setLong(4,this.genreId);
            createTrack.setLong(5,this.milliseconds);
            createTrack.setLong(6,this.bytes);
            createTrack.setBigDecimal(7,this.unitPrice);

            PreparedStatement getID =conn.prepareStatement("SELECT last_insert_rowID()",
                    Statement.RETURN_GENERATED_KEYS);

            createTrack.execute();
            getID.execute();
            conn.commit();

            ResultSet ID = getID.getGeneratedKeys();
            if(ID.next()){
                this.trackId = (long) ID.getInt(1);
            }
            Jedis jedis = new Jedis();
            jedis.del(REDIS_CACHE_KEY);
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
            PreparedStatement createTrack =conn.prepareStatement("DELETE FROM tracks WHERE tracks.TrackId = ? ");
            createTrack.setLong(1,this.trackId);
            createTrack.execute();
            conn.commit();
            Jedis jedis = new Jedis();
            jedis.del(REDIS_CACHE_KEY);
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
                PreparedStatement updateAlbum =conn.prepareStatement("UPDATE tracks SET Name = ?, AlbumId = ?, " +
                        "MediaTypeId = ?, GenreId = ?, Milliseconds = ?, Bytes = ?, UnitPrice = ?" +
                        "WHERE TrackId = ? AND Name = ?");

                String origName = Track.find(this.trackId).getName();

                updateAlbum.setString(1,this.name);
                updateAlbum.setLong(2,this.albumId);
                updateAlbum.setLong(3,this.mediaTypeId);
                updateAlbum.setLong(4,this.genreId);
                updateAlbum.setLong(5,this.milliseconds);
                updateAlbum.setLong(6,this.bytes);
                updateAlbum.setBigDecimal(7,this.unitPrice);
                updateAlbum.setLong(8,this.trackId);
                updateAlbum.setString(9,this.oldName);

                int isSuccess = updateAlbum.executeUpdate();

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



    public boolean verify(){
        _errors.clear();
        boolean status = true;

        if(this.name == null){
            this.addError("name must not be null and be of the string data type");
            status = false;
        }

        if(this.albumId == null){
            this.addError("albumID must not be null and be of the string data type");
            status = false;
        }

        return status;
    }

    public static Track find(long i) {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, tracks.MediaTypeId as MediaTypeId, " +
                    "tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName, " +
                    "albums.Title as AlbumName FROM tracks JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                    "JOIN artists ON artists.ArtistId = albums.ArtistId WHERE trackId = ?");
            x.setLong(1,i);
            ResultSet result =  x.executeQuery();
            return new Track(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public static Long count() {

       Jedis jedis = new Jedis();
        String stringCount = jedis.get(REDIS_CACHE_KEY);

        if (stringCount != null){
           return Long.parseLong(stringCount);
       }

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT COUNT(*) as trackCount FROM tracks");
            ResultSet result =  x.executeQuery();
            Long count = result.getLong("trackCount");
            jedis.set(REDIS_CACHE_KEY,String.valueOf(count));
            return count;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public Album getAlbum() {

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM albums " +
                    "JOIN tracks ON albums.AlbumId = tracks.AlbumId WHERE tracks.TrackId = ?");
            x.setLong(1,this.trackId);
            ResultSet result =  x.executeQuery();
            return new Album(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM playlists " +
                    "JOIN playlist_track ON playlist_track.PlaylistId = playlists.PlaylistId " +
                    "JOIN tracks ON playlist_track.TrackId = tracks.TrackId WHERE tracks.TrackId = ?");
            x.setLong(1,this.trackId);
            ResultSet result =  x.executeQuery();
            List<Playlist> playlists = new ArrayList<Playlist>();
            while(result.next()){
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

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if(this.name == null) {
            this.name = name;
        }
        else{
            this.oldName = new String(this.name);
            this.name = name;
        }
    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public String getAlbumTitle() {
        return this.albumName;
    }

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {

        try(Connection conn = DB.connect()) {

            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            String begin = "SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, tracks.MediaTypeId as MediaTypeId," +
                    " tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName," +
                    " albums.Title as AlbumName FROM tracks";
            String end = " LIMIT ? OFFSET ?";
            String joins = " JOIN albums ON tracks.AlbumId = albums.AlbumId JOIN artists ON artists.ArtistId = albums.ArtistId ";
            List<String> where = new ArrayList<String>();
            String wheres = "";
            List<Object> mysteryValues = new ArrayList<Object>();

            if(!Objects.isNull(artistId)){
                where.add("artists.ArtistId = ? ");
                mysteryValues.add(artistId);
            }

            if(!StringUtils.isEmpty(search)){
                where.add("tracks.Name LIKE ?");
                mysteryValues.add(new String("%" + search +"%"));
            }

            if(!Objects.isNull(albumId)){
                where.add("tracks.AlbumId =  ?");
                mysteryValues.add(albumId);
            }

            if(!Objects.isNull(maxRuntime)){
                where.add("tracks.Milliseconds < ?");
                mysteryValues.add(maxRuntime);
            }

            if(!Objects.isNull(minRuntime)){
                where.add("tracks.Milliseconds > ?");
                mysteryValues.add(minRuntime);
            }


            if(where.size() >0){ wheres += " WHERE " + where.get(0);}

            for(int i =1; i<where.size(); i++){
                wheres += " AND " + where.get(i);
            }

           String query = begin+joins+wheres+end;
            System.out.println(query);

            PreparedStatement x = conn.prepareStatement(query);
            for(int i = 0; i<mysteryValues.size();i++){
                x.setObject(i+1,mysteryValues.get(i));
            }

            x.setInt(mysteryValues.size()+1,count);
            x.setInt(mysteryValues.size()+2,offset);
            System.out.println(x);

            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Track> tracks = new ArrayList<Track>();

           while(result.next()) {
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

    public static List<Track> search(int page, int count, String orderBy, String search) {

        List<String> validColValues =  Arrays.asList("Name", "TrackId", "AlbumId", "MediaTypeId", "GenreID", "Milliseconds", "Bytes", "UnitPrice",null);
        System.out.println(orderBy);
        if(!validColValues.contains(orderBy)){
            System.out.println("Invalid value for orderBy parameter");
            return null;
        }

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, tracks.MediaTypeId as MediaTypeId, " +
                    "tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName, " +
                    "albums.Title as AlbumName FROM tracks JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                    "JOIN artists ON artists.ArtistId = albums.ArtistId " +
                    "WHERE tracks.Name LIKE ? ORDER BY ? LIMIT ? OFFSET ?");

            int offset = (page-1) * count;

            search = new String("%" + search + "%");
            x.setString(1,search);
            x.setString(2,orderBy);
            x.setLong(3,count);
            x.setLong(4,offset);
            ResultSet result = x.executeQuery();
            List<Track> tracks = new ArrayList<Track>();

            while(result.next()){
                tracks.add(new Track(result));
            }
            System.out.println("TrackSize: " + tracks.size() + " " + count + " " + offset);
            return tracks;
        }
        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }

    }

    public static List<Track> forAlbum(Long albumId) {

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, tracks.MediaTypeId as MediaTypeId, " +
                    " tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName, " +
                    " albums.Title as AlbumName FROM tracks JOIN albums ON tracks.AlbumId = albums.AlbumId " +
                    " JOIN artists ON artists.ArtistId = albums.ArtistId WHERE albums.AlbumId = ? ");

            x.setLong(1, albumId);
            ResultSet result = x.executeQuery();
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

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {

        List<String> validColValues =  Arrays.asList("Name", "TrackId", "AlbumId", "MediaTypeId", "GenreID", "Milliseconds", "Bytes", "UnitPrice",null);
        try(Connection conn = DB.connect()) {

            int offset = (page-1) * count;
            conn.setAutoCommit(false);

            if(!validColValues.contains(orderBy)){
                System.out.println("Invalid value for orderBy parameter");
                return null;
            }

            String query = "SELECT tracks.TrackId as TrackId, tracks.AlbumId as AlbumId, tracks.MediaTypeId as MediaTypeId, " +
                    " tracks.GenreId as GenreId, tracks.UnitPrice as UnitPrice, tracks.Name as Name, tracks.Milliseconds as Milliseconds, tracks.Bytes as Bytes, artists.Name as ArtistName, " +
                    " albums.Title as AlbumName FROM tracks JOIN albums ON tracks.AlbumId = albums.AlbumId" +
                    " JOIN artists ON artists.ArtistId = albums.ArtistId ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
            PreparedStatement x = conn.prepareStatement(query);
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Track> tracks = new ArrayList<Track>();

            while(result.next()) {
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

}
