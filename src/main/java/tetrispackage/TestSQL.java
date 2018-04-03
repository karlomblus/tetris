package tetrispackage;

import java.sql.*;
import java.util.Calendar;

public class TestSQL {


    public static void main(String[] argv) throws Exception {

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://tetris.carlnet.ee:33060/tetris?user=tetris&password=oopprojekt");


        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }


        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();


            //
            // Issue the DDL queries for the table for this example
            //

            //stmt.executeUpdate("CREATE TABLE test (id INT NOT NULL AUTO_INCREMENT, data VARCHAR(64), PRIMARY KEY (id))");

            //
            // Insert one row that will generate an AUTO INCREMENT
            // key in the 'priKey' field
            //

            stmt.executeUpdate(
                    "INSERT INTO test (id,data) values (0,'Can I Get the Auto Increment Field?')", Statement.RETURN_GENERATED_KEYS);


            // create a sql date object so we can use it in our INSERT statement
            Calendar calendar = Calendar.getInstance();
            java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());

            // the mysql insert statement
            String query2 = " insert into test (id,data)"
                    + " values (?, ?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query2);
            preparedStmt.setInt(1, 0);
            //preparedStmt.setString (2, "Barney");
            preparedStmt.setDate(2, startDate);

            preparedStmt.execute();


            int autoIncKeyFromApi = -1;
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                autoIncKeyFromApi = rs.getInt(1);
            } else {
                // throw an exception from here
            }

            System.out.println("Key returned from getGeneratedKeys():" + autoIncKeyFromApi);

            /*
            //var 2
            int autoIncKeyFromFunc = -1;
    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");

    if (rs.next()) {
        autoIncKeyFromFunc = rs.getInt(1);
    } else {
        // throw an exception from here
    }
             */


            //rs = stmt.executeQuery("SELECT foo FROM bar");

            // or alternatively, if you don't know ahead of time that
            // the query will be a SELECT...


            //if (stmt.execute("SELECT id,data FROM test")) {
            //    rs = stmt.getResultSet();
            //    System.out.println(rs.getInt("id") + " " + rs.getString("data"));
            //}

            // Now do something with the ResultSet ....

            String query = "SELECT id,data FROM test";
            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " " + rs.getString("data"));
            }
            st.close();


        }
        /*catch (SQLException ex){
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }*/ finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }


        } // finally


    } // main


}