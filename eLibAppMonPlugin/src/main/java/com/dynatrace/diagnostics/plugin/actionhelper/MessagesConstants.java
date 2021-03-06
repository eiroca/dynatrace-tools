package com.dynatrace.diagnostics.plugin.actionhelper;

/**
 * @author eugene.turetsky
 *
 */
public interface MessagesConstants {

  public final String BaseIncidentFormatter_AUTOMATICALLY_GENERATED_EMAIL_TEXT = "This is an automatically generated notification about an incident.";
  public final String BaseIncidentFormatter_DETAILS_HEADLINE_HTML_EMAIL = "Details";
  public final String BaseIncidentFormatter_DETAILS_HEADLINE_PLAIN_TEXT_EMAIL = "Details";
  public final String BaseIncidentFormatter_ENABLE_WEBSERVICES_TO_GET_A_LINK_TO_THIS_INCIDENT_REPORT_EMAIL_HINT_TEXT = "Enable WebServices to get a link to a report of this Incident";
  public final String BaseIncidentFormatter_ENABLE_WEBSTART_TO_GET_A_LINK_TO_THIS_INCIDENT_EMAIL_HINT_TEXT = "Enable Webstart to get a link to this Incident in the dynaTrace Client";
  public final String BaseIncidentFormatter_INCIDENT_AFFECTED_AGENTS_EMAIL_TEXT_ENTRY = "Agents";
  public final String BaseIncidentFormatter_INCIDENT_AFFECTED_AGENT_GROUPS_EMAIL_TEXT_ENTRY = "AgentGroups";
  public final String BaseIncidentFormatter_INCIDENT_AFFECTED_MONITORS_EMAIL_TEXT_ENTRY = "Monitors";
  public final String BaseIncidentFormatter_INCIDENT_AFFECTED_COLLECTORS_EMAIL_TEXT_ENTRY = "Collectors";
  public final String BaseIncidentFormatter_INCIDENT_AFFECTED_HOSTS_EMAIL_TEXT_ENTRY = "Hosts";
  public final String BaseIncidentFormatter_INCIDENT_BUSINESS_TRANSACTIONS_EMAIL_TEXT_ENTRY = "Business Transactions";
  public final String BaseIncidentFormatter_INCIDENT_DURATION_EMAIL_TEXT_ENTRY = "Duration";
  public final String BaseIncidentFormatter_INCIDENT_DYNATRACE_SERVER_EMAIL_TEXT_ENTRY = "dynaTrace Server";
  public final String BaseIncidentFormatter_INCIDENT_ENDED_EMAIL_SUBJECT = "{0} Incident ended";
  public final String BaseIncidentFormatter_INCIDENT_ENDED_EMAIL_TEXT_ENTRY = "Violation ended";
  public final String BaseIncidentFormatter_INCIDENT_FORMATTED_SUBJECT = "Incidents , {0}";
  public final String BaseIncidentFormatter_INCIDENT_OCCURED_EMAIL_SUBJECT = "{0} Incident occurred";
  public final String BaseIncidentFormatter_INCIDENT_PENDING_EMAIL_TEXT_ENTRY = "Violation pending";
  public final String BaseIncidentFormatter_INCIDENT_SEVERITY_HEADLINE_IN_EMAIL = "{0} Incident";
  public final String BaseIncidentFormatter_INCIDENT_STARTED_EMAIL_SUBJECT = "{0} Incident started";
  public final String BaseIncidentFormatter_INCIDENT_STATUS_EMAIL_TEXT_ENTRY = "Status";
  public final String BaseIncidentFormatter_INCIDENT_SYSTEM_PROFILE_EMAIL_TEXT_ENTRY = "System Profile";

  public final String BaseIncidentFormatter_OPEN_IN_BROWSER_EMAIL_LINK_TEXT = "Open in browser";
  public final String BaseIncidentFormatter_OPEN_IN_DYNATRACE_EMAIL_LINK_TEXT = "Open in dynaTrace";
  public final String BaseIncidentFormatter_START_TIME_OF_INCIDENT_EMAIL_TEXT_ENTRY = "Time";
  public final String BaseIncidentFormatter_VIOLATIONS_HEADLINE_HTML_EMAIL = "Violations";
  public final String BaseIncidentFormatter_VIOLATIONS_HEADLINE_PLAIN_TEXT_EMAIL = "Violations";
  public final String BaselineIncidentProcessor_AVERAGE_RESPONSE_TIME_DEGRADED_INCIDENT_MESSAGE = "Observed response time of {0} is higher than expected. During the last {1} the average response time was less than {2}.";
  public final String BaselineIncidentProcessor_FAILURE_RATE_INCIDENT_MESSAGE = "Observed failure rate of {0,number,\\#.\\#}% is higher than expected. During the last {1} the failure rate was less than {2,number,\\#.\\#}%.";
  public final String BaselineIncidentProcessor_FAILURE_RATE_VIOLATION_MESSAGE = "{0,number,\\#.\\#}% (expected: {1,number,\\#.\\#}%)";
  public final String BaselineIncidentProcessor_RESPONSE_TIME_SLOW_REQUESTS_INCIDENT_MESSAGE = "10% of all requests are observing response times higher than {0}. During the last {1} these requests could be served in {2}";
  public final String BaselineIncidentProcessor_RESPONSE_TIME_VIOLATION_MESSAGE = "{0} (expected: {1})";
  public final String CIIncidentFormatter_AUTOMATICALLY_GENERATED_NOTIFICATION_TEST_VIOLATION_EMAIL_TEXT = "This is an automatically generated notification about a test violation.";
  public final String CIIncidentFormatter_TEST_MARKER = "Marker";
  public final String CIIncidentFormatter_TEST_PACKAGE = "Package";
  public final String CIIncidentFormatter_TEST_NAME = "Test Name";
  public final String CIIncidentFormatter_VERSION = "Version";
  public final String ServerIncidentFactory_GC_UNHEALTHY = "GC health of process ''{0}'' is not ok.\n\nThe process constantly spent more than {1}% of it''s execution time for Garbage Collection in the last {2} minutes.";
  public final String ServerIncidentFactory_HOST_AVAILABLE = "The host ''{0}'' is available.";
  public final String ServerIncidentFactory_HOST_CPU_UNHEALTHY = "The CPU health of host ''{0}'' is not ok.\n\nDuring the last {1} minutes this host reported a high overall CPU usage of {2}%, a load greater than {3} (unix based systems only) or more than {4}% system CPU usage.";
  public final String ServerIncidentFactory_HOST_DISK_UNHEALTHY = "The hard disk health of host ''{0}'' is not ok.\n\nAt least one disk has less than {1}% and less than {2} usable space left.";
  public final String ServerIncidentFactory_HOST_MEMORY_UNHEALTHY = "The memory health of host ''{0}'' is not ok.\n\nDuring the last {1} minutes this host had less than {2}% and less than {3} memory available or reported constantly more than {4} hard page faults per second.";
  public final String ServerIncidentFactory_HOST_NETWORK_UNHEALTHY = "The network health of host ''{0}'' is not ok.\n\nAt least one network interface used more than {1}% of it''s available bandwidth for the last {2} minutes.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_CONFLICTING_JVM_OPTION_WITH_OPTION = "Agent ''{0}'' has a conflicting JVM option: {1}";
  public final String ServerIncidentFactory_INCIDENT_AGENT_CONNECTED_CLOSE_CONNECTION_LOST = "connection lost";
  public final String ServerIncidentFactory_INCIDENT_AGENT_CONNECTED_CLOSE_DISCONNECTED = "disconnected";
  public final String ServerIncidentFactory_INCIDENT_AGENT_CONNECTION_LOST = "Agent ''{0}'' connection lost";
  public final String ServerIncidentFactory_INCIDENT_AGENT_DID_NOT_MATCH_SYSTEM_PROFILE = "Agent ''{0}'' could not be mapped to a System Profile.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_DID_NOT_MATCH_SYSTEM_PROFILE_DESCRIPTION = "Agent did not match any System Profile.\nThe System Profile may be disabled, the Agent Mapping in the Profile may be configured incorrectly or the Agent's name may be misspelled.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_DISABLED_CPU_TIME_CAPTURING = "Agent ''{0}'' disabled CPU time capturing.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_DISABLED_CPU_TIME_CAPTURING_DESCRIPTION = "The agent's CPU time capturing option is set to 'automatic' and the agent detected that CPU time capturing would cause too much overhead. Check the agent group's sensor configuration to force or disable CPU time capturing.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_MATCHED_SYSTEM_PROFILES = "Agent ''{0}'' matched {1} System Profiles.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_MATCHED_SYSTEM_PROFILES_DESCRIPTION = "Agent matched profiles: {0}.\nMatch was applied to ''{1}''.";
  public final String ServerIncidentFactory_INCIDENT_AGENT_USES_LOW_PRECISION_TIMER_WITH_TIMER_NAME = "Agent ''{0}'' uses low precision Timer ''{1}''.";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_INFO_WITH_HOST_AND_PORT_AND_COMMUNICATION_VERSION_AND_BASIC_SESSION_SETTINGS_DESCRIPTION = "{0}:{1}, {2} [{3}]";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_INFO_WITH_HOST_AND_PORT_AND_COMMUNICATION_VERSION_DESCRIPTION = "{0}:{1}, {2}";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_SETTING_COMPRESSION = "Compression";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_SETTING_PROXY_DESCRIPTION = "Proxy";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_SETTING_SSL_DESCRIPTION = "SSL";
  public final String ServerIncidentFactory_INCIDENT_BASIC_SESSION_SETTING_TUNNEL_DESCRIPTION = "Tunnel";
  public final String ServerIncidentFactory_INCIDENT_COLLECTOR_CONNECTION_LOST = "Collector ''{0}'' connection lost.";
  public final String ServerIncidentFactory_INCIDENT_CONTINUOUS_TRANSACTION_STORAGE_STOPPED = "Continuous transaction recording was stopped for system profile ''{0}''.";
  public final String ServerIncidentFactory_INCIDENT_CONTINUOUS_TRANSACTION_STORAGE_STOPPED_DESCRIPTION = "Due to low disk space the continuous recording of transactions was stopped for the ''{0}'' system profile. Recording will be resumed automatically when enough disk space is available again.";
  public final String ServerIncidentFactory_INCIDENT_PERFROMANCE_WAREHOUSE_OFFLINE = "Performance Warehouse is offline";
  public final String ServerIncidentFactory_INCIDENT_PORT_CONFLICT = "The port {0} could not be opened\\!";
  public final String ServerIncidentFactory_INCIDENT_REPOSITORY_CLEANUP = "Repository Clean-Up";
  public final String ServerIncidentFactory_INCIDENT_SERVER_OFFLINE = "Server was offline";
  public final String ServerIncidentFactory_INCIDENT_SERVER_OFFLINE_UNEXPECTED = "Server was offline (unexpected)";
  public final String ServerIncidentFactory_INCIDENT_SERVER_ONLINE = "Server Online";
  public final String ServerIncidentFactory_INCIDENT_SESSION_CLEARED_BY_USER = "Session ''{0}'' cleared by user ''{1}''";
  public final String ServerIncidentFactory_INCIDENT_SESSION_CLEARED_BY_USER_DESCRIPTION = "Clear Session";
  public final String ServerIncidentFactory_INCIDENT_SESSION_DETAILS_COMPRESSED_BYTES_WITH_RATIO_DESCRIPTION = "\nBytes Written Compressed: {0} (x{1})\nBytes Read Compressed: {2} (x{3})";
  public final String ServerIncidentFactory_INCIDENT_SESSION_DETAILS_WITH_FLAGS_AND_BYTES_WRITTEN_AND_READ_DESCRIPTION = "SSL: {0}\nTunnel: {1}\nProxy: {2}\nCompression: {3}\nBytes Written: {4}\nBytes Read: {5}";
  public final String ServerIncidentFactory_INCIDENT_SESSION_STORAGE_MINIMUM_FREE_DISKSPACE_REACHED = "Minimum free diskspace on Session Storage disk reached";
  public final String ServerIncidentFactory_INCIDENT_SESSION_STORAGE_MINIMUM_FREE_DISKSPACE_REACHED_DESCRIPTION = "Minimum free diskspace of {0} reached. Unable to free {1}";
  public final String ServerIncidentFactory_INCIDENT_SESSION_STORAGE_QUOTA_EXCEEDED = "Session storage quota exceeded";
  public final String ServerIncidentFactory_INCIDENT_SESSION_STORAGE_QUOTA_EXCEEDED_DESCRIPTION = "Session storage quota of {0} exceeded. Unable to free {1}";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_COUNT_BY_NOW_DESCRIPTION = "There have been {0} out of memory events until now.";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_LATEST_OUT_OF_MEMORY_SECONDS_AGO_DESCRIPTION = "The latest out of memory event occurred {0} ago.";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_SNAPSHOT_BY_PREVIOUS_INCIDENT_DESCRIPTION = "A memory snapshot has already been triggered upon a previous incident.";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_SNAPSHOT_FAILED_DESCRIPTION = "A memory snapshot has been triggered but a problem occurred while creating it.";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_SNAPSHOT_TRIGGERED_DESCRIPTION = "A memory snapshot has been triggered.";
  public final String ServerIncidentFactory_INCIDENT_SUD_OUT_OF_MEMORY_WITH_VM_DESCRIPTION = "Out-of-Memory on Agent ''{0}'': {1}.";
  public final String ThresholdViolationData_TYPE_LOWER_SEVERE = "Lower Severe";
  public final String ThresholdViolationData_TYPE_LOWER_WARNING = "Lower Warning";
  public final String ThresholdViolationData_TYPE_UPPER_SEVERE = "Upper Severe";
  public final String ThresholdViolationData_TYPE_UPPER_WARNING = "Upper Warning";

}
