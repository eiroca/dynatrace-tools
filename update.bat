for /D %%d in (AppMon-Plugin-Monitor-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-0.0.8.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

for /D %%d in (AppMon-Plugin-Task-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-0.0.8.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

for /D %%d in (AppMon-Plugin-Action-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-0.0.8.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y ..\..\library\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-OracleServer\lib

xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-Monitor-DatabaseServer\lib
xcopy /y ..\..\library\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-DatabaseServer\lib

xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-Monitor-PostgresServer\lib
xcopy /y ..\..\library\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-PostgresServer\lib

xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-Monitor-Advanced\lib
xcopy /y ..\..\library\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-Advanced\lib
