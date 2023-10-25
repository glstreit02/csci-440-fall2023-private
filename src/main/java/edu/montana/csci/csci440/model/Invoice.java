package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Invoice extends Model {

    Long invoiceId;
    String billingAddress;
    String billingCity;
    String billingState;
    String billingCountry;
    String billingPostalCode;
    BigDecimal total;

    public Invoice() {
        // new employee for insert
    }

    private Invoice(ResultSet results) throws SQLException {
        billingAddress = results.getString("BillingAddress");
        billingState = results.getString("BillingState");
        billingCountry = results.getString("BillingCountry");
        billingCity = results.getString("BillingCity");
        billingPostalCode = results.getString("BillingPostalCode");
        total = results.getBigDecimal("Total");
        invoiceId = results.getLong("InvoiceId");
    }

    public List<InvoiceItem> getInvoiceItems(){

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM invoice_items  JOIN invoices ON invoice_items.InvoiceId = invoices.InvoiceId  WHERE invoices.InvoiceId = ?");
            x.setLong(1,this.invoiceId);
            ResultSet result = x.executeQuery();
            conn.commit();

            List<InvoiceItem> invoice_items = new ArrayList<InvoiceItem>();
            while(result.next()){
                invoice_items.add(new InvoiceItem(result));
            }
            return invoice_items;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }
    public Customer getCustomer() {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM customers  JOIN invoices ON customers.CustomerId = invoices.CustomerId  WHERE invoices.InvoiceId = ?");
            x.setLong(1,this.invoiceId);
            ResultSet result = x.executeQuery();
            conn.commit();

            return new Customer(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public static List<Invoice> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Invoice> all(int page, int count) {

            try(Connection conn = DB.connect()) {
                int offset = (page-1) * count;
                conn.setAutoCommit(false);

                PreparedStatement x = conn.prepareStatement("SELECT * FROM invoices LIMIT ? OFFSET ?");
                x.setInt(1,count);
                x.setInt(2,offset);
                ResultSet result =  x.executeQuery();
                conn.commit();

                List<Invoice> invoices = new ArrayList<Invoice>();

                while(result.next()) {
                    invoices.add(new Invoice(result));
                }
                return invoices;
            }

            catch(SQLException e){
                System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                        e.getSQLState());
                return null;
            }
    }

    public static Invoice find(long invoiceId) {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM invoices WHERE InvoiceId = ?");
            x.setLong(1,invoiceId);
            ResultSet result =  x.executeQuery();
            conn.commit();
            return new Invoice(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }
}
