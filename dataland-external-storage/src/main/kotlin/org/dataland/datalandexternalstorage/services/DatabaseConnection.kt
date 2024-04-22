import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.Properties
import java.util.UUID

/**
 * This object holds methods to establish a JDBC connection and do sql inserts into a database table
 */
object DatabaseConnection {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * The method prepares and executes a sql statement to insert a pair of dataId data into an external database
     * @param conn hold the connection details
     * @param sqlStatement the sql statement which should be executed
     * @param dataId the dataId of the data to be inserted
     * @param data the data to be inserted
     */
    fun insertDataIntoSqlDatabase(conn: Connection?, sqlStatement: String, dataId: String, data: String): Boolean {
        val preparedStatement: PreparedStatement?
        var rowsInserted = 0
        if (conn != null) {
            preparedStatement = conn.prepareStatement(sqlStatement)
            preparedStatement.setObject(1, UUID.fromString(dataId))
            preparedStatement.setString(2, data)

            rowsInserted = preparedStatement.executeUpdate()
            if (rowsInserted > 0) {
                logger.info("Data for $dataId was inserted successfully.")
            }
            preparedStatement?.close()
            conn?.close()
        }
        return rowsInserted > 0
    }

    /**
     * The method prepares and executes a sql statement to insert a pair of documentId and document into an
     * external database
     * @param conn hold the connection details
     * @param sqlStatement the sql statement which should be executed
     * @param documentId the documentId of the document to be inserted
     * @param document the document to be inserted
     */
    fun insertByteArrayIntoSqlDatabase(
        conn: Connection?,
        sqlStatement: String,
        documentId: String,
        document: ByteArray,
    ): Boolean {
        val preparedStatement: PreparedStatement?
        var rowsInserted = 0
        if (conn != null) {
            preparedStatement = conn.prepareStatement(sqlStatement)
            preparedStatement.setObject(1, UUID.fromString(documentId))
            preparedStatement.setBytes(2, document)

            rowsInserted = preparedStatement.executeUpdate()
            if (rowsInserted > 0) {
                logger.info("A Document with the $documentId was inserted successfully.")
            }
            preparedStatement?.close()
            conn?.close()
        }
        return rowsInserted > 0
    }

    /**
     * This method creates and returns the connection to the external database
     * @param username the username to log into the database
     * @param password the password to log into the database
     * @param databaseUrl the url of the database
     */
    fun getConnection(username: String, password: String, databaseUrl: String): Connection? {
        val connectionProps = Properties()
        connectionProps["user"] = username
        connectionProps["password"] = password
        return DriverManager.getConnection(
            databaseUrl,
            connectionProps,
        )
    }
}
