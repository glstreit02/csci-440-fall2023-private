package edu.montana.csci.csci440.helpers;

import edu.montana.csci.csci440.model.Employee;

import java.util.*;

public class EmployeeHelper {

    public static String makeEmployeeTree() {
        // TODO, change this to use a single query operation to get all employees
        List <Employee> Employees = Employee.all(); // root employee
        // and use this data structure to maintain reference information needed to build the tree structure

        Employee employee =Employees.get(0);
        Map<Long, List<Employee>> employeeMap = new HashMap<>();

        long count =1;
        for( Employee i: Employees){
            List<Employee> inner = new ArrayList<Employee>();
           for ( Employee j : Employees){
               if ( i.getEmployeeId().equals(j.getReportsTo() )){
                 inner.add(j);
               }
           }
           employeeMap.put(count,inner);
           count++;
        }

        return "<ul>" + makeTree(employee, employeeMap) + "</ul>";
    }

    // TODO - currently this method just uses the employee.getReports() function, which
    //  issues a query.  Change that to use the employeeMap variable instead
    public static String makeTree(Employee employee, Map<Long, List<Employee>> employeeMap) {
        String list = "<li><a href='/employees/" + employee.getEmployeeId() + "'>"
                + employee.getEmail() + "</a><ul>";

        List<Employee> reports = employeeMap.get(employee.getEmployeeId());
        for (Employee report : reports) {
            list += makeTree(report, employeeMap);
        }
        return list + "</ul></li>";
    }
}
