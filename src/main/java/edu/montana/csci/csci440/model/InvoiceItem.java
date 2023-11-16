package edu.montana.csci.csci440.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoiceItem extends Model {

    Long invoiceLineId;
    Long invoiceId;
    Long trackId;
    BigDecimal unitPrice;
    Long quantity;
    String TrackName;
    String AlbumName;
    String ArtistName;

    public InvoiceItem(ResultSet result) throws SQLException {
        invoiceLineId = result.getLong("InvoiceLineId");
        invoiceId = result.getLong("InvoiceId");
        trackId = result.getLong("TrackId");
        unitPrice = result.getBigDecimal("UnitPrice");
        quantity = result.getLong("Quantity");

        Track invItem = Track.find(trackId);
        TrackName = invItem.getName();
        AlbumName = invItem.getAlbumTitle();
        ArtistName = invItem.getArtistName();
    }
    public Track getTrack() {
        return null;
    }

    public String getArtistName(){
        return ArtistName;
    }
    public String getAlbumName(){
        return AlbumName;
    }

    public String getTrackName(){
        return TrackName;
    }
    public Invoice getInvoice() {
        return null;
    }

    public Long getInvoiceLineId() {
        return invoiceLineId;
    }

    public void setInvoiceLineId(Long invoiceLineId) {
        this.invoiceLineId = invoiceLineId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
