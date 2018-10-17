<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

<!-- see also: http://wiki.apache.org/logging-log4j/Log4jXmlFormat -->

<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">

	<appender name="DB" class="org.apache.log4j.jdbc.JDBCAppender">
		<param name="URL" value="jdbc:postgresql://localhost:5432/test" />
		<param name="driver" value="org.postgresql.Driver" />
		<param name="user" value="postgres" />
		<param name="password" value="postgres" />
		<layout class="org.apache.log4j.EnhancedPatternLayout">
			<param name="ConversionPattern"
				value="INSERT INTO APP_LOGS
				(THREAD, DATE_OF_OCCURENCE, CLASS, LINE_NUMBER, LEVEL, MESSAGE, API, DURATION, STACKTRACE) VALUES 
				('%t', now() ,'%C', '%L', '%p','%m', '%X{API}', %X{DURATION}, '%throwable{200}')" />
		</layout>
	</appender>

	<appender name="CONSOLETest1" class="org.apache.log4j.ConsoleAppender">
		<param name="target" value="System.out" />
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern"
				value="%d{ABSOLUTE} %-5p [%c{1}] %m || (AT LINE %L in %C{1}) %n" />
		</layout>
	</appender>

	<appender name="CONSOLETest2" class="org.apache.log4j.ConsoleAppender">
		<param name="target" value="System.out" />
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern" value="%d{ABSOLUTE} %-5p [%c{1}] %m%n" />
		</layout>

		<filter class="org.apache.log4j.varia.StringMatchFilter">
			<param name="StringToMatch" value="#test1$" />
			<param name="AcceptOnMatch" value="true" />
		</filter>
		<filter class="org.apache.log4j.varia.DenyAllFilter" />
	</appender>

	<!-- rolling file appender -->
	<appender name="file" class="org.apache.log4j.RollingFileAppender">
		<param name="File" value="logs/main.log" />
		<param name="Append" value="true" />
		<param name="ImmediateFlush" value="true" />
		<param name="MaxFileSize" value="10MB" />
		<param name="MaxBackupIndex" value="5" />
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern" value="%d %d{Z} [%t] %-5p (%F:%L) - %m%n" />
		</layout>
	</appender>

	<!--wrap ASYNC around other appender if you want -->
	<appender name="ASYNC" class="org.apache.log4j.AsyncAppender">
		<param name="BufferSize" value="2" />
		<!-- large buffer not working for test example -->
		<param name="LocationInfo" value="true" />
		<!-- <param name="includeLocation" value="true" /> -->
		<appender-ref ref="DB" />
	</appender>


	<logger name="com.san.test1" additivity="false">
		<level value="DEBUG" />
		<appender-ref ref="CONSOLETest1" />
	</logger>
	<logger name="com.san.test2" additivity="false">
		<level value="DEBUG" />
		<appender-ref ref="CONSOLETest2" />
	</logger>
	<root>
		<priority value="debug" />
		<appender-ref ref="ASYNC" />
	</root>

</log4j:configuration>