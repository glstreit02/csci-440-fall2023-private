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

public class Customer extends Model {

    private Long customerId;
    private Long supportRepId;
    private String firstName;
    private String lastName;
    private String email;

    public Employee getSupportRep() {
         return Employee.find(supportRepId);
    }

    public List<Invoice> getInvoices(){

        try(Connection conn = DB.connect()) {
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM invoices WHERE CustomerId = ? ");
            x.setLong(1,this.customerId);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Invoice> Invoices = new ArrayList<Invoice>();

            while(result.next()) {
                Invoices.add(new Invoice(result));
            }
            return Invoices;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }


    public Customer(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        customerId = results.getLong("CustomerId");
        supportRepId = results.getLong("SupportRepId");
        email = results.getString("Email");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getSupportRepId() {
        return supportRepId;
    }

    public static List<Customer> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Customer> all(int page, int count) {
        try(Connection conn = DB.connect()) {
            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM customers LIMIT ? OFFSET ?");
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Customer> Customers = new ArrayList<Customer>();

            while(result.next()) {
                Customers.add(new Customer(result));
            }
            return Customers;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public static Customer find(long customerId) {

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM customers WHERE CustomerId = ?");
            x.setLong(1,customerId);
            ResultSet result =  x.executeQuery();
            return new Customer(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public static List<Customer> forEmployee(long employeeId) {

        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT customers.FirstName, customers.LastName," +
                    "customers.customerId, customers.supportRepId, customers.Email FROM customers JOIN employees " +
                    "ON customers.SupportRepId  = employees.EmployeeId WHERE employees.EmployeeId = ? ");

            x.setLong(1,employeeId);
            ResultSet result = x.executeQuery();
            List<Customer> customers = new ArrayList<Customer>();

            while(result.next()){
                customers.add(new Customer(result));
            }

            return customers;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

}
