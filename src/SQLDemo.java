/**
 * @author richard.stansbury
 * @email richard.stansbury@gmail.com
 * @date March 30, 2018
 *
 * This file provides some simple demonstration of SQL Interface.
 *
 * This builds upon the VideoGame database shown in class and its schema is provided in the db.sql file provided
 * with this distribution.  You must run the schema on your MySQL database to create your own instance with initial
 * data.
 *
 * Operations to be performed include:
 *   SELECT
 *   INSERT
 *   UPDATE
 *   DELETE
 *
 *   Transactions are also demonstrated in the DELETE demonstration.
 *
 *
 * See also:
 *   - Java API: https://docs.oracle.com/javase/8/docs/api/index.html?java/sql/package-summary.html
 *   - MySQL Connector Driver (jar file): https://dev.mysql.com/downloads/connector/j/5.1.html
 *     -  The driver JAR file must be loaded as part of your build and execution path.  Instructions vary based upon
 *        your development environment.
 *   - Tutorials:
 *     - Java official -- https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html
 *
 * Configuration for Demo
 *  - You must have a MySQL database instance running.
 *  - The MySQL database has had the db.sql schema run upon it to create the necessary tables.
 *  - You update the source code below with the correct URL, username, and password for YOUR instance.
 *  - Your build and runtime environment for Java must be appropriately configured for the MySQL Connector jar file.
 */


import java.sql.*;

public class SQLDemo {

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //  DECLARE CLASS VARIABLES

    /**
     *  Creates a class variable for sql connection.
     *  - This demo assumes that the connection is open once when the class is instantiated and not closed.
     *  - For your code, you may wish to consider closing and opening your Connection for each operation/transaction
     *    rather than keeping it open.  This is important if your project needs multiple instances of the connection.
     *    such as if each class file handles its own database I/O.
     */
    Connection sql;

    /**
     * Default Constructor - creates an instance of the database connection
     *
     * @param url - URL for the MySQL database connection
     * @param user - user name for the MySQL database
     * @param pswd - password for MySQL Database.
     */
    public SQLDemo(String url, String user, String pswd) {

        /** Creates an instance of the database connection **/
        try {
           this.sql = DriverManager.getConnection(url, user, pswd);
        }
        catch (SQLException e){
            System.out.println(e.getMessage()); //Handle exceptions
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    //  SELECT Queries

    /**
     * Prints column labels and tuples in a comma separated values format
     * @param table - table to be queried.
     */
    public void printAll(String table) {
        try {

            System.out.println("\nPrinting All Records from: " + table);

            //SQL Select Query
            String query = "SELECT * FROM " + table;

            //Create a SQL Statement object
            Statement stmt = sql.createStatement();

            //Execute the query on the statement object
            //  Returns a ResultSet.
            ResultSet results = stmt.executeQuery(query);


            //From our result set, we can extract our column labels.
            // (1) Retrieve the meta data from the result set
            // (2) Determine number of columns
            // (3) print each column with a comma separator
            //
            // Note: the columns are indexed starting at 1 and not zero.
            ResultSetMetaData rsmd = results.getMetaData();
            int numCols = rsmd.getColumnCount();
            for (int i=1; i <= numCols; i++) {
                if (i == numCols)
                    System.out.println(rsmd.getColumnLabel(i));
                else
                    System.out.print(rsmd.getColumnLabel(i) +", ");
            }


            //From our results, we can now print out the data retrieved.
            //  - result.next() advances the result record to the next result tuple
            //     * Your initial result.next() advances to the first record
            //     * returns null when you have reached the end of the result set.

            //Let's look at the data
            while (results.next()) {

                //Given the result, you now must retrieve the value column-by-column
                // * We are generically retrieving ech column as type string, which returns the string
                //   representation of the tuple element.
                for (int i=1; i <= numCols; i++) {

                    //Retrieving the value for the ith column in the tuple
                    String nextVal = results.getString(i);
                    if (i==numCols)
                        System.out.println(nextVal);
                    else
                        System.out.print(nextVal + ", ");
                }
            }
        } catch(SQLException e) { //Handle exceptions
            System.out.println(e.getMessage());
        }
    }

    /**
     * Demonstrates a simple select query using JDBC to MySQL.
     */
    public void printVideoGames()
    {
        try {

            //Define the query as a string
            String query = "SELECT * FROM VideoGames";

            //Request a Statement object from SQL class
            Statement stmt = sql.createStatement();

            //Execute the query
            ResultSet results = stmt.executeQuery(query);

            //Print each record
            while (results.next()) {

                //Since we know the attribute names and ther data types, we can use the appropriate
                // get method to retrieve based on label.
                //
                // ResultSet also has get methods for each data type based upon the index of the returned item
                //  in the tuple.  Please note that the indexing starts at 1 and not zero.
                System.out.printf("%s, %d, %s, %s\n",
                        results.getString("title"),
                        results.getInt("year"),
                        results.getString("publisher"),
                        results.getString("genre"));
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage()); //Handle exceptions
        }
    }


    /**
     * This query shows that you can extract information from multiple relations and how to use
     * PreparedStatements to implement your WHERE clause.
     *
     * Prepared statements use ? to note the location of values that you will be performing the query on
     *   it does some of the work for us and helps protect us from SQL injections.
     *
     *   You can always use the regulatr Statement object and build your own query strings, but this method helps
     *   reduce the number of syntax errors you may encounter, and provides some protection against SQL injection.
     *
     * @param title - title of game whose information we want.
     * @param year - year of the game whose information we want.
     */
    public void printGameInfo(String title, int year) {

        //SQL queries...not the "?" in place of the values that we wish to search upon.  We will assign those values
        // usng the PreparedStatement below.
        String gameQueryString = "SELECT publisher, genre FROM VideoGames WHERE title=? AND year=?";
        String releaseQueryString = "SELECT platform, date FROM GameReleases WHERE title=? AND year=?";

        //Define variables for PreparedStatements and ResultSets.
        PreparedStatement gameStmt, releaseStmt;
        ResultSet rs1, rs2;
        try {

            ///////////////////////////////////////////////////////
            //  Retrieve and print Game Info

            //Request a PreparedStatement object and assign values to replace the ? in the SQL string
            gameStmt = sql.prepareStatement(gameQueryString);
            gameStmt.setString(1, title);
            gameStmt.setInt(2, year);

            //Execute the query
            rs1 = gameStmt.executeQuery();

            //Only one record max will return.
            //  We will first check to make sure a valid result is present.
            //  If so, we extract the information from the query and print the details.
            //  If not, we will inform the user.
            if (rs1.next()) {
                System.out.println("\nGame: " + title);
                System.out.println("Initial Release Year: " + year);
                System.out.println("Publisher: " + rs1.getString("publisher"));
                System.out.println("Genre: " + rs1.getString("genre"));
            } else {
                System.out.println("\nGame (" + title + ", " + year + ") not found.");
                return;
            }


            ///////////////////////////////////////////////////////
            //  Retrieve and print Release Info

            //Initialize the PreparedStatement given our query
            releaseStmt = sql.prepareStatement(releaseQueryString);
            releaseStmt.setString(1, title);
            releaseStmt.setInt(2, year);

            //Execute prepared statement
            rs2 = releaseStmt.executeQuery();

            //Print the release information for each returned record.
            System.out.println("Releases:");
            while (rs2.next()) {
                System.out.println("\t " + rs2.getString("platform") + ", " + rs2.getDate("date"));
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage()); //Handles Exception
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    //  INSERT statements

    /**
     * Inserts a new video game into the database
     * @param title - title of the new game
     * @param year - year of initial release
     * @param publisher - publisher of the game
     * @param genre - genre of the game.
     */
    public void addVideoGame(String title,
                             int year,
                             String publisher,
                             String genre) {
        try {

            //Write string to handle insert.  Note: since inserting values for all attributes the
            // attribute is not included.  Using ? for the values, which will be added to prepared statement
            String insertString = "INSERT INTO VideoGames VALUES (?, ?, ?, ?)";

            //Building prepared statement
            PreparedStatement insertStatement = sql.prepareStatement(insertString);
            insertStatement.setString(1, title);
            insertStatement.setInt(2, year);
            insertStatement.setString(3, publisher);
            insertStatement.setString(4, genre);

            //Execute the query and confirm that the new record was added.
            int rows = insertStatement.executeUpdate();
            if (rows != 1) {
                System.out.println("ALERT: Insertion failed.");
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage()); //handle exceptions.
        }
    }


    /**
     * Adds a tuple to game releases.
     *
     * @param title - title of game to be added
     * @param year - year of initial release of game to be added
     * @param platform - platform for this version of the game.
     * @param date - date of release of this version of game
     */
    public void addGameRelease(String title,
                               int year,
                               String platform,
                               String date) {
        try {

            //Create insert query string
            String insertString = "INSERT INTO GameReleases VALUES (?, ?, ?, ?)";

            //Create prepared statement for insert and assign it the values.
            PreparedStatement insertStatement = sql.prepareStatement(insertString);
            insertStatement.setString(1, title);
            insertStatement.setInt(2, year);
            insertStatement.setString(3, platform);
            insertStatement.setDate(4, Date.valueOf(date));

            //Perform the insert and confirm success.
            int rows = insertStatement.executeUpdate();
            if (rows != 1) {
                System.out.println("ALERT: Insertion failed.");
            }

        } catch(SQLException e) {
            System.out.println(e.getMessage()); //Handles exceptons.
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////
    //  Update statements

    /**
     * Updates the release date of a game.
     *
     * @param title - key attribute, title of game release to be updated
     * @param year - key attribute, year of game release to be updated
     * @param platform - key attribute, platform of the game release to be updated
     * @param date - new date for the game release.
     */
    public void updateGameRelease(String title,
                                  int year,
                                  String platform,
                                  String date) {

        try {
            //Query String
            String query = "UPDATE GameReleases SET date=? WHERE title=? and year=? and platform=?";

            //Prepared Statement
            PreparedStatement pstmt = sql.prepareStatement(query);
            pstmt.setDate(1, Date.valueOf(date)); //Note: must convert date string to date object
            pstmt.setString(2, title);
            pstmt.setInt(3, year);
            pstmt.setString(4, platform);

            //Execute the update
            pstmt.executeUpdate();

        } catch(SQLException e) {
            System.out.println(e.getMessage()); //Handles exception
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    //  DELETE statements


    /**
     * Deletes a game and its associated releases
     * @param title - title of game to delete
     * @param year - year of game to delete
     */
    public void deleteGame(String title, int year) {


        //Prepare the delete statement strings with ? for the key attributes of title and year
        String deleteGameString = "DELETE FROM VideoGames WHERE title = ? AND year = ?";
        String deleteReleaseString = "DELETE FROM GameReleases WHERE title = ? AND year = ?";

        //Declare prepared statement variables.
        PreparedStatement deleteGameStmt;
        PreparedStatement deleteReleaseStmt;

        try {

            //Start Transaction by Setting Auto Commit to False
            //  Note: we must re-enable auto-commit when we are done to restore the system to the default
            //  state.
            sql.setAutoCommit(false);

            //Perform the first delete by preparing and executing the statement
            deleteGameStmt = sql.prepareStatement(deleteGameString);
            deleteGameStmt.setString(1, title);
            deleteGameStmt.setInt(2, year);
            deleteGameStmt.executeUpdate();

            //Perform the second delete by preparing and executing the statement.
            deleteReleaseStmt = sql.prepareStatement(deleteReleaseString);
            deleteReleaseStmt.setString(1, title);
            deleteReleaseStmt.setInt(2, year);
            deleteReleaseStmt.executeUpdate();

            //Commit the change to make the change live to the database.
            sql.commit();

        } catch(SQLException e) {

            //If an exception occurs, we will roll back the transaction to avoid having an error state.

            try {
                System.out.println("Rolling Back Transaction.");

                //Performs the roll back.
                sql.rollback();
            } catch(SQLException e2) {
                System.out.println(e2.getMessage()); //Handle exception
            }

        } finally {

            //Finally is called after the try ends by reaching the end of its code or from a return statement.
            //  We include it here so that we can reenable autocommit and take it out of transaction mode.

            try {
                //Re-enable autocommit
                sql.setAutoCommit(true);
            } catch(SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * Main - runs the demo.
     * @param args
     */
    public static void main(String[] args) {

        /**
         *   MySQL connection parameters
         *   -- specifying your system parameters in plain text such as this is NOT acceptible for a
         *      real-world project. It is suggested you create a properties file and store user name, password,
         *      URL, etc. within it.
         *
         *      Reference for using .properties files:
         *          https://docs.oracle.com/javase/tutorial/essential/environment/properties.html
         */
        String url = "jdbc:mysql://localhost:3306/videogames?useSSL=false";
        String user = "root";
        String pswd = "12345";


        //Create the demo object.
        SQLDemo demo1 = new SQLDemo(url, user, pswd);


        System.out.println("\n---------------------------------------------------");
        System.out.println("Print all games:");
        System.out.println("---------------------------------------------------");
        demo1.printVideoGames();

        System.out.println("\n---------------------------------------------------");
        System.out.println("Print Single Game Info:");
        System.out.println("---------------------------------------------------");
        demo1.printGameInfo("Minecraft", 2009);


        System.out.println("\n---------------------------------------------------");
        System.out.println("Add X-wing vs. Tie Fighter:");
        System.out.println("---------------------------------------------------");
        demo1.addVideoGame("X-Wing vs. Tie Fighter", 1997, "Electronic Arts", "Space Shooter");
        demo1.addGameRelease("X-Wing vs. Tie Fighter", 1997, "WIN", "1997-04-30");
        demo1.addGameRelease("X-Wing vs. Tie Fighter", 1997, "MAC", "1997-04-30");
        demo1.printGameInfo("X-Wing vs. Tie Fighter", 1997);

        System.out.println("\n---------------------------------------------------");
        System.out.println("Update X-wing vs. Tie Fighter Release Date:");
        System.out.println("---------------------------------------------------");
        demo1.updateGameRelease("X-Wing vs. Tie Fighter", 1997, "MAC", "1997-12-31");
        demo1.printGameInfo("X-Wing vs. Tie Fighter", 1997);

        System.out.println("\n---------------------------------------------------");
        System.out.println("Delete Game and All Release Info:");
        System.out.println("---------------------------------------------------");
        demo1.deleteGame("X-Wing vs. Tie Fighter", 1997);
        demo1.printGameInfo("X-Wing vs. Tie Fighter", 1997);
        demo1.printAll("VideoGames");
        demo1.printAll("GameReleases");
    }
}
