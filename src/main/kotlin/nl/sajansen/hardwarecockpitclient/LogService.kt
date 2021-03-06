package nl.sajansen.hardwarecockpitclient

import nl.sajansen.hardwarecockpitclient.config.Config
import nl.sajansen.hardwarecockpitclient.utils.getFileExtension
import nl.sajansen.hardwarecockpitclient.utils.getFileNameWithoutExtension
import nl.sajansen.hardwarecockpitclient.utils.getReadableFileSize
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.logging.*

object LogService {

    private val logger = Logger.getLogger(LogService::class.java.name)

    private var logFile: File? = null
    private var logFileHandler: FileHandler? = null
    private val applicationLogger = Logger.getLogger("")

    var logBuffer = ArrayList<LogRecord>()

    @Throws(IOException::class, IllegalAccessException::class)
    fun setup(args: Array<String>) {

        if (args.contains("--disable-logging")) {
            logger.info("Disabling logging")
            applicationLogger.handlers.forEach {
                applicationLogger.removeHandler(it)
            }
            return
        }

        if (Config.enableApplicationLoggingToFile) {
            setupFileLogging()
        } else {
            logger.info("File logging is disabled")
        }

        // Set log level
        if (args.contains("-v")) {
            logger.info("Debug logging turned on")

            applicationLogger.level = Level.FINE
            applicationLogger.handlers.forEach {
                it.level = Level.FINE
            }
        } else {
            applicationLogger.level = Level.INFO
        }
    }

    private fun setupFileLogging() {
        logger.fine("Setting up file logging")

        // Create logging directory
        val logDirectoryPath = Paths.get(System.getProperty("java.io.tmpdir"), "HardwareCockpitClient")
        if (!logDirectoryPath.toFile().exists()) {
            logger.info("Creating logging directory: $logDirectoryPath")
            logBuffer.add(LogRecord(Level.INFO, "Creating logging directory: $logDirectoryPath"))
            logDirectoryPath.toFile().mkdirs()
        }

        // Create log file
        logFile = File(logDirectoryPath.toFile(), "hardware-cockpit-client.log")

        // Create log file handler
        logFileHandler = getFileHandlerForFile(logFile!!)
        logFileHandler!!.formatter = SimpleFormatter()
        logger.info("Adding LogHandlers")
        logBuffer.add(LogRecord(Level.INFO, "Adding LogHandlers"))

        // Assign file logger to application logger
        // Logger used by all the classes in this application
        applicationLogger.addHandler(logFileHandler)

        logger.fine("Logging to file: " + logFile!!.absolutePath)

        logger.info("Emptying logbuffer in logfile: --- START ---")
        for (logRecord in logBuffer) {
            logger.log(logRecord)
        }
        logger.info("--- END ---")
    }

    /**
     * Creates a file handler for a specific log file. If the log file's size exceeds the LogFileMaxSize,
     * a new file will be created and the contents of the old file will be moved to a file appended with .1
     * @param logFile File
     * @return FileHandler
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getFileHandlerForFile(logFile: File): FileHandler? {
        logger.fine("Current log file size: " + getReadableFileSize(logFile))
        logBuffer.add(LogRecord(Level.FINE, "Current log file size: " + getReadableFileSize(logFile)))

        if (logFile.length() < Config.maxLogFileSize) {
            logFile.appendText("\n\n\n\n")
            return FileHandler(logFile.absolutePath, true)
        }

        val fileName: String = getFileNameWithoutExtension(logFile)
        val fileExtension: String = getFileExtension(logFile)
        val olgLogFileName = "$fileName.1.$fileExtension"
        logger.info("Emptying current logfile in old log file: $olgLogFileName")
        logBuffer.add(LogRecord(Level.INFO, "Emptying current logfile in old log file: $olgLogFileName"))

        val oldLogFile = File(logFile.parent, olgLogFileName)
        Files.move(logFile.toPath(), oldLogFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        return FileHandler(logFile.absolutePath, false)
    }

    fun getLogFile(): File? = logFile
}