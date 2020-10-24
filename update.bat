SET ELIB_PATH=..\
SET ELIB_VER=0.0.10

for /D %%d in (AppMon-Plugin-Monitor-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-%ELIB_VER%.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

for /D %%d in (AppMon-Plugin-Task-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-%ELIB_VER%.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

for /D %%d in (AppMon-Plugin-Action-*) do (
	del /Q .\%%d\lib\*
	xcopy /y .\eLibAppMonPlugin\target\eLibAppMonPlugin-%ELIB_VER%.jar .\%%d\lib
	xcopy /y .\eLibAppMonPlugin\target\libs\* .\%%d\lib
)

xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\ojdbc* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\ons-* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\oraclepki-* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\orai18n-* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\osdt_* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\ucp-* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\xdb-* .\AppMon-Plugin-Monitor-OracleServer\lib
xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\simplefan-* .\AppMon-Plugin-Monitor-OracleServer\lib

xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-DatabaseServer\lib

xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\postgresql-* .\AppMon-Plugin-Monitor-PostgresServer\lib

xcopy /y %ELIB_PATH%\eJDBCDrivers\target\libs\* .\AppMon-Plugin-Monitor-Advanced\lib
