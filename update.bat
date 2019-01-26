del /Q .\AppMon-Plugin-AdvancedAction\lib\*
del /Q .\AppMon-Plugin-AdvancedTask\lib\*
del /Q .\AppMon-Plugin-AdvancedMonitor\lib\*

xcopy /y .\eLibDynatrace\target\eLibDynatrace-0.0.1.jar .\AppMon-Plugin-AdvancedAction\lib
xcopy /y .\eLibDynatrace\target\eLibDynatrace-0.0.1.jar .\AppMon-Plugin-AdvancedTask\lib
xcopy /y .\eLibDynatrace\target\eLibDynatrace-0.0.1.jar .\AppMon-Plugin-AdvancedMonitor\lib

xcopy /y .\eLibDynatrace\target\libs\* .\AppMon-Plugin-AdvancedAction\lib
xcopy /y .\eLibDynatrace\target\libs\* .\AppMon-Plugin-AdvancedTask\lib
xcopy /y .\eLibDynatrace\target\libs\* .\AppMon-Plugin-AdvancedMonitor\lib

xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-AdvancedAction\lib
xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-AdvancedTask\lib
xcopy /y ..\..\library\eJDBCDrivers\libs\* .\AppMon-Plugin-AdvancedMonitor\lib
