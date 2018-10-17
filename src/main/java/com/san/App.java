package com.san;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.jdbc.JDBCAppender;

import com.san.test1.NonDBLoggingTester1;
import com.san.test2.NonDBLoggingTester2;

// Reference : https://stackify.com/log4j-guide-dotnet-logging/
// Reference : https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html
// Reference : https://arviarya.wordpress.com/2012/11/09/log4j-for-high-performance-application/
// Reference : https://blog.codinghorror.com/the-problem-with-logging/

// Note : AsyncAppender doesn't include location info by default, its expensive for AsyncAppender
// Its NOT recommended to use location info ie. “%C”, “%F”, “%M”, “%L” conversion characters in AsyncAppender
// Reference : https://logging.apache.org/log4j/log4j-2.3/manual/async.html#Location
// Reference : https://stackoverflow.com/questions/37525996/can-i-run-my-log-asynchronously-using-log4j-1-x-with-log4j-properties-file/52850193#52850193

public class App {

	// Note : There are two ways to configure AsyncAppender in log4j 1.x
	// 1 - Using log4j.xml : Remove log4j.properties file, rename log4j.xml-1.x to log4j.xml and remove enableAsyncAuditLog() call from static block
	// 2 - Programmatically : Use log4j.properties for basic configuration and implement AsyncAppender as enableAsyncAuditLog() function

	static {
		enableAsyncAuditLog(Logger.getRootLogger());
		// enableAsyncAuditLog(Logger.getLogger("com.san.test1"));
		// enableAsyncAuditLog(Logger.getLogger("com.san.test2"));
		logger = Logger.getLogger(App.class);
		attachShutdownHook();
	}

	private static Logger logger;
	private static JDBCAppender appender;

	public static void main(String[] args) throws SQLException {

		App obj = new App();
		obj.initializeDBForFirstTime();

		MDC.put("API", "/info/JDBCAppender/log4j");
		MDC.put("DURATION", 1500);
		logger.info("This is an example of JDBCAppender of log4j!");

		MDC.put("API", "/debug/start/run");
		MDC.put("DURATION", 1100);
		logger.debug("Start of run()");

		NonDBLoggingTester1 tester1 = new NonDBLoggingTester1();
		tester1.testLog();

		NonDBLoggingTester2 tester2 = new NonDBLoggingTester2();
		tester2.testLog();

		try {
			// Intentionally we trigger divide by zero exception
			if (23 / 0 > 23) {
			}
		} catch (Exception e) {
			MDC.put("API", "/error/Execution/error");
			MDC.put("DURATION", 0);
			logger.error("Execution error", e);
		}
		MDC.put("API", "/debug/End/run");
		MDC.put("DURATION", 200);
		logger.debug("End of run()");
		MDC.clear();

		// obj.showDBLogs();
	}

	// @SuppressWarnings("unused")
	private void initializeDBForFirstTime() throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		try {
			// For Postgres :
			// statement.execute("CREATE TABLE APP_LOGS(THREAD VARCHAR(20), DATE_OF_OCCURENCE timestamp, CLASS VARCHAR(100)," + "LINE_NUMBER INTEGER, LEVEL VARCHAR(10), MESSAGE VARCHAR(1000), API VARCHAR(100), DURATION INTEGER, STACKTRACE BYTEA)");

			// For HSQLDB :
			statement.execute("CREATE TABLE APP_LOGS(THREAD VARCHAR(20), DATE_OF_OCCURENCE timestamp, CLASS VARCHAR(100)," + "LINE_NUMBER INTEGER, LEVEL VARCHAR(10), MESSAGE VARCHAR(1000), API VARCHAR(100), DURATION INTEGER, STACKTRACE CLOB)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// @SuppressWarnings("unused")
	private void showDBLogs() throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		try {
			ResultSet rs = statement.executeQuery("select * from APP_LOGS");
			System.out.println("Thread | Date | Class | Line Number | Level | Message | Stacktrace");
			while (rs.next()) {
				String userId = rs.getString("THREAD");
				Date date = rs.getDate("DATE_OF_OCCURENCE");
				String logger = rs.getString("CLASS");
				int line = rs.getInt("LINE_NUMBER");
				String level = rs.getString("LEVEL");
				String message = rs.getString("MESSAGE");
				// String stackTrace = rs.getString("STACKTRACE");
				String api = rs.getString("API");
				int duration = rs.getInt("DURATION");
				// String stackTrace = new String(rs.getBytes("STACKTRACE")); // For Postgres
				String stackTrace = rs.getString("STACKTRACE"); // For HSQLDB
				System.out.println(userId + " | " + date + " | " + logger + " | " + line + " | " + level + " | " + message + " | " + api + " | " + duration + " | " + stackTrace);
			}
			rs.close();
			// statement.executeUpdate("DROP TABLE APP_LOGS");
			// connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		findJDBCAppender();
		return DriverManager.getConnection(appender.getURL(), appender.getUser(), appender.getPassword());
	}

	@SuppressWarnings("unchecked")
	private void findJDBCAppender() {
		Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
		while (appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			if (appender instanceof JDBCAppender) {
				App.appender = (JDBCAppender) appender;
				break;
			}
			if (appender instanceof AsyncAppender) {
				Enumeration<Appender> subAppenders = ((AsyncAppender) appender).getAllAppenders();
				while (subAppenders.hasMoreElements()) {
					Appender subAppender = subAppenders.nextElement();
					if (subAppender instanceof JDBCAppender) {
						App.appender = (JDBCAppender) subAppender;
						break;
					}
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	private static void enableAsyncAuditLog(Logger targetLogger) {
		Enumeration<Appender> appenders = targetLogger.getAllAppenders();
		AsyncAppender asyncAppender = new AsyncAppender();
		asyncAppender.setBufferSize(500);
		asyncAppender.setLocationInfo(true); // Otherwise Class and Line info will not be available to logger
		while (appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			if (!(appender instanceof AsyncAppender)) {
				targetLogger.removeAppender(appender);
				asyncAppender.addAppender(appender);
			}
		}
		appenders = asyncAppender.getAllAppenders();
		if (appenders != null && appenders.hasMoreElements()) {
			targetLogger.addAppender(asyncAppender);
			// asyncAppender.activateOptions();
		}
	}

	// Issue : Shutdown sequence leaves not enough time for async {loggers, appenders} to write out the messages that still reside in the {ring buffer, queue}.
	// Issue Reference : https://issues.apache.org/jira/browse/LOG4J2-520
	// Workaround : Do not use buffering for logging at all or keep it as small as possible
	// Workaround : Put a wait period before application shutting down.
	private static void attachShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.println("System going to be shutdown.");
					Thread.sleep(1000);
					new App().showDBLogs();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					System.out.println("Exception occurred while printing logs, captured through JDBCAppender");
					e.printStackTrace();
				}
			}
		});
	}

}
