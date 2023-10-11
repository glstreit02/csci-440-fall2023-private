package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Employee extends Model {

    private Long employeeId;
    private Long reportsTo;
    private String firstName;
    private String lastName;
    private String email;
    private String title;

    public Employee() {
        // new employee for insert
    }

    private Employee(ResultSet results) throws SQLException {
        firstName = results.getString("FirstName");
        lastName = results.getString("LastName");
        email = results.getString("Email");
        employeeId = results.getLong("EmployeeId");
        reportsTo = results.getLong("ReportsTo");
        title = results.getString("Title");
    }

    public static List<Employee.SalesSummary> getSalesSummaries() {
        //TODO - a GROUP BY query to determine the sales (look at the invoices table), using the SalesSummary class
        return Collections.emptyList();
    }

    @Override
    public boolean verify() {

        _errors.clear();

        if (firstName == null || firstName.isBlank()) {
            addError("firstName can't be null or blank");
        }

        if(lastName == null || lastName.isBlank()){
            addError("lastName can't be null or blank");
        }

        if(email == null || email.isBlank() || !email.contains("@")){
            addError("email cannot be blank and must contain the @ character");
        }

        return !hasErrors();
    }

    @Override
    public boolean update() {
        if (verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE employees SET FirstName=?, LastName=?, Title = ?, Email=? WHERE EmployeeId=?")) {
                stmt.setString(1, this.getFirstName());
                stmt.setString(2, this.getLastName());
                stmt.setString(3, this.getEmail());
                stmt.setString(4,this.getTitle());
                stmt.setLong(5, this.getEmployeeId());
                stmt.executeUpdate();
                return true;
            }
            catch(SQLException e){
                System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                        e.getSQLState());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean create() {
        if (verify()) {
            try (Connection conn = DB.connect()){

                conn.setAutoCommit(false);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO employees " +
                         "(FirstName, LastName, Email, Title, ReportsTo) VALUES (?, ?, ?,?,?)");
                 stmt.setString(1, this.getFirstName());
                 stmt.setString(2, this.getLastName());
                 stmt.setString(3, this.getEmail());
                 stmt.setString(4,this.getTitle());
                 stmt.setLong(5,this.getReportsTo());
                 stmt.executeUpdate();
                 conn.commit();

                 this.employeeId = DB.getLastID(conn);
                 return true;
            }
            catch (SQLException e) {
                System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                        e.getSQLState());
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public void delete() {
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public List<Customer> getCustomers() {
        return Customer.forEmployee(employeeId);
    }

    public Long getReportsTo() {
        return reportsTo;
    }

    public void setReportsTo(Long reportsTo) {
        this.reportsTo = reportsTo;
    }

    public List<Employee> getReports() {
        return Collections.emptyList();
    }
    public Employee getBoss() {
     return find(this.reportsTo);
    }

    public static List<Employee> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Employee> all(int page, int count) {

        try(Connection conn = DB.connect()) {
            int offset = (page-1) * count;
            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM employees LIMIT ? OFFSET ?");
            x.setInt(1,count);
            x.setInt(2,offset);
            ResultSet result =  x.executeQuery();
            conn.commit();

            List<Employee> Employees = new ArrayList<Employee>();

            while(result.next()) {
                Employees.add(new Employee(result));
            }
            return Employees;
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }

    }

    public static Employee findByEmail(String newEmailAddress) {
        throw new UnsupportedOperationException("Implement me");
    }

    public static Employee find(long employeeId) {
        try(Connection conn = DB.connect()) {

            conn.setAutoCommit(false);
            PreparedStatement x = conn.prepareStatement("SELECT * FROM employees WHERE EmployeeId = ?");
            x.setLong(1,employeeId);
            ResultSet result = x.executeQuery();
            conn.commit();

            return  new Employee(result);
        }

        catch(SQLException e){
            System.out.println(e.getMessage() + "\n" + e.getErrorCode() + "\n" +
                    e.getSQLState());
            return null;
        }
    }

    public void setTitle(String programmer) {
        title = programmer;
    }

    public String getTitle(){
        return this.title;
    }

    public void setReportsTo(Employee employee) {
        this.reportsTo = employee.employeeId;
    }

    public static class SalesSummary {
        private String firstName;
        private String lastName;
        private String email;
        private Long salesCount;
        private BigDecimal salesTotals;
        private SalesSummary(ResultSet results) throws SQLException {
            firstName = results.getString("FirstName");
            lastName = results.getString("LastName");
            email = results.getString("Email");
            salesCount = results.getLong("SalesCount");
            salesTotals = results.getBigDecimal("SalesTotal");
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

        public Long getSalesCount() {
            return salesCount;
        }

        public BigDecimal getSalesTotals() {
            return salesTotals;
        }
    }
}
