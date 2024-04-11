import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.Properties

/**
 * Program to list databases in MySQL using Kotlin
 */
object DatabaseConnection {

    // internal var conn: Connection? = null
    // internal var username = "username" // provide the username
    // internal var password = "password" // provide the corresponding password

    fun executeMySQLQuery(conn: Connection?, sqlStatement: String, key: String, value: String) {
        var preparedStatement: PreparedStatement? = null
        if (conn != null) {
            try {
                // Prepare the insert statement
                preparedStatement = conn.prepareStatement(sqlStatement)
                preparedStatement.setString(1, key)
                preparedStatement.setString(2, value)

                // Execute the insert statement
                val rowsInserted = preparedStatement.executeUpdate()
                if (rowsInserted > 0) println("A new row was inserted successfully.")
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                // Close resources
                try {
                    preparedStatement?.close()
                    conn?.close()
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                }
            }
        }
    }

    /**
     * This method makes a connection to MySQL Server
     * In this example, MySQL Server is running in the local host (so 127.0.0.1)
     * at the standard port 3306
     */
    fun getConnection(username: String, password: String, databaseUrl: String): Connection? {
        val connectionProps = Properties()
        connectionProps.put("user", username)
        connectionProps.put("password", password)
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance()
            return DriverManager.getConnection(
                databaseUrl,
                connectionProps,
            )
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
        } catch (ex: Exception) {
            // handle any errors
            ex.printStackTrace()
        }
        return null
    }
}
