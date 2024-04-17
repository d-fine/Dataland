import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.Properties
import java.util.UUID

/**
 * This object holds methods to establish a JDBC connection and do sql inserts into a database table
 */
object DatabaseConnection {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun executeMySQLQuery(conn: Connection?, sqlStatement: String, key: String, value: String) {
        var preparedStatement: PreparedStatement? = null
        if (conn != null) {
            try {
                preparedStatement = conn.prepareStatement(sqlStatement)
                preparedStatement.setObject(1, UUID.fromString(key))
                preparedStatement.setString(2, value)

                val rowsInserted = preparedStatement.executeUpdate()
                if (rowsInserted > 0) {
                    logger.info("A new row was inserted successfully.")
                }
            } catch (ex: SQLException) {
                logger.error("A sql exception was thronw: $ex")
            } finally {
                try {
                    preparedStatement?.close()
                    conn?.close()
                } catch (ex: SQLException) {
                    logger.error("A sql exception was thronw: $ex")
                }
            }
        }
    }
    fun executeMySQLQuery2(conn: Connection?, sqlStatement: String, key: String, value: ByteArray) {
        var preparedStatement: PreparedStatement? = null
        if (conn != null) {
            try {
                preparedStatement = conn.prepareStatement(sqlStatement)
                preparedStatement.setObject(1, key)
                preparedStatement.setBytes(2, value)

                val rowsInserted = preparedStatement.executeUpdate()
                if (rowsInserted > 0) {
                    logger.info("A new row was inserted successfully.")
                }
            } catch (ex: SQLException) {
                logger.error("A sql exception was thronw: $ex")
            } finally {
                try {
                    preparedStatement?.close()
                    conn?.close()
                } catch (ex: SQLException) {
                    logger.error("A sql exception was thronw: $ex")
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
        connectionProps["user"] = username
        connectionProps["password"] = password
        try {
            return DriverManager.getConnection(
                databaseUrl,
                connectionProps,
            )
        } catch (ex: SQLException) {
            logger.error("A sql exception was thronw: $ex")
        } catch (ex: Exception) {
            logger.error("An unexpected exception was thronw: $ex")
        }
        return null
    }
}
