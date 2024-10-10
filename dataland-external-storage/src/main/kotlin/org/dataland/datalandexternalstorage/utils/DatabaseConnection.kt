package org.dataland.datalandexternalstorage.utils

import com.fasterxml.jackson.databind.ObjectMapper
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
    fun insertDataIntoSqlDatabase(
        conn: Connection,
        sqlStatement: String,
        dataId: String,
        data: String,
    ): Boolean {
        val preparedStatement: PreparedStatement = conn.prepareStatement(sqlStatement)
        preparedStatement.setObject(1, UUID.fromString(dataId))
        preparedStatement.setString(2, data)

        val successFlag = executeSqlInsertStatement(preparedStatement, "Data for $dataId was inserted successfully.")
        conn.close()
        return successFlag
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
        conn: Connection,
        sqlStatement: String,
        documentId: String,
        document: ByteArray,
    ): Boolean {
        val preparedStatement: PreparedStatement = conn.prepareStatement(sqlStatement)
        preparedStatement.setObject(1, UUID.fromString(documentId))
        preparedStatement.setBytes(2, document)

        val successFlag =
            executeSqlInsertStatement(
                preparedStatement,
                "A Document with the eurodatId $documentId was inserted successfully.",
            )
        conn.close()
        return successFlag
    }

    private fun executeSqlInsertStatement(
        preparedStatement: PreparedStatement,
        loggingMessage: String,
    ): Boolean {
        var successFlag = false
        val rowsInserted = preparedStatement.executeUpdate()
        if (rowsInserted == 1) {
            logger.info(loggingMessage)
            successFlag = true
        } else {
            logger.info("Unexpected number of changed rows. Expected was 1, actual was $rowsInserted .")
        }
        preparedStatement.close()
        return successFlag
    }

    /**
     * This method creates and returns the connection to the external database
     * @param username the username to log into the database
     * @param password the password to log into the database
     * @param databaseUrl the url of the database
     */
    fun getConnection(
        username: String,
        password: String,
        databaseUrl: String,
    ): Connection {
        val connectionProps = Properties()
        connectionProps["user"] = username
        connectionProps["password"] = password
        return DriverManager.getConnection(
            databaseUrl,
            connectionProps,
        )
    }

    /**
     * This method retrieves a data blob entry from an external database
     * @param conn hold the connection details
     * @param sqlStatement the sql statement which should be executed
     * @param dataId the dataId of the dataset to be retrieved
     */
    fun selectJsonStringFromSqlDatabase(
        conn: Connection,
        sqlStatement: String,
        dataId: String,
    ): String {
        var data = String()
        logger.info("Retrieving data from eurodat storage for dataId: $dataId")
        val preparedStatement = conn.prepareStatement(sqlStatement)
        val sqlReturnValue = preparedStatement.executeQuery()
        while (sqlReturnValue.next()) {
            data = sqlReturnValue.getString("blob_json")
        }
        conn.close()
        return ObjectMapper().writeValueAsString(data)
    }

    /**
     * This method retrieves a data blob entry from an external database
     * @param conn hold the connection details
     * @param sqlStatement the sql statement which should be executed
     * @param documentId the documentId of the document to be retrieved
     */
    fun selectDocumentFromSqlDatabase(
        conn: Connection,
        sqlStatement: String,
        documentId: String,
    ): ByteArray {
        var blob = ByteArray(0)
        logger.info("Retrieving document from eurodat storage for documentId: $documentId")
        val preparedStatement: PreparedStatement = conn.prepareStatement(sqlStatement)
        val sqlReturnValue = preparedStatement.executeQuery()
        while (sqlReturnValue.next()) {
            blob = sqlReturnValue.getBytes("blob_pdf")
        }
        conn.close()
        return blob
    }
}
