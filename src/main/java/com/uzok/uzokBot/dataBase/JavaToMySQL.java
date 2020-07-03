package com.uzok.uzokBot.dataBase;

import com.uzok.uzokBot.utils.Prop;

import java.sql.*;

/**
 * Simple Java program to connect to MySQL database running on localhost and
 * running SELECT and INSERT query to retrieve and add data.
 *
 * @author Javin Paul
 */
public class JavaToMySQL {

    // JDBC URL, username and password of MySQL server
    private static final String url = Prop.getProp("connectionString");
    private static final String user = Prop.getProp("sqlLogin");
    private static final String password = Prop.getProp("sqlPass");

    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static CallableStatement cstmt;
    private static ResultSet rs;

    public JavaToMySQL() {

    }

    public Object executeQuery(BaseSqlProcedure procedure) {
        Object result = null;
        try {
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
            rs = stmt.executeQuery(procedure.sqlQuery);
            result = procedure.execute(rs);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return result;
    }

    public void executeCall(BaseSqlProcedure procedure) {
        try {
            con = DriverManager.getConnection(url, user, password);
            cstmt = con.prepareCall(procedure.sqlQuery);
            rs = cstmt.executeQuery();
            procedure.execute(rs);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            sqlEx.getMessage();
        } finally {
            //close connection ,stmt and result set here
            try {
                rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                cstmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            try {
                con.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

}