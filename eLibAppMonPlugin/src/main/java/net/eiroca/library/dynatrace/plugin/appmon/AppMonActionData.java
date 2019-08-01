/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.library.dynatrace.plugin.appmon;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.AgentGroupSource;
import com.dynatrace.diagnostics.pdk.AgentSource;
import com.dynatrace.diagnostics.pdk.CollectorSource;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Incident.Severity;
import com.dynatrace.diagnostics.pdk.IncidentRule;
import com.dynatrace.diagnostics.pdk.Key;
import com.dynatrace.diagnostics.pdk.Measure;
import com.dynatrace.diagnostics.pdk.Metric;
import com.dynatrace.diagnostics.pdk.MonitorSource;
import com.dynatrace.diagnostics.pdk.Sensitivity;
import com.dynatrace.diagnostics.pdk.ServerSource;
import com.dynatrace.diagnostics.pdk.Source;
import com.dynatrace.diagnostics.pdk.SourceType;
import com.dynatrace.diagnostics.pdk.Threshold;
import com.dynatrace.diagnostics.pdk.Timestamp;
import com.dynatrace.diagnostics.pdk.Value;
import com.dynatrace.diagnostics.pdk.Violation;
import com.dynatrace.diagnostics.pdk.Violation.TriggerValue;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;
import com.dynatrace.diagnostics.sdk.resources.DurationFormat;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.actiondata.Messages;

public class AppMonActionData extends ActionData {

  public static final String SYSTEM_PROFILE = "SYSTEM_PROFILE";

  public static final String DYNATRACE_SERVER_NAME = "DYNATRACE_SERVER_NAME";
  public static final String START_TIME = "START_TIME";
  public static final String END_TIME = "END_TIME";
  public static final String AGENT_NAME = "";
  public static final String fAGENT_NAME = "AGENT_NAME_%d";
  public static final String AGENT_HOST = "AGENT_HOST";
  public static final String fAGENT_HOST = "AGENT_HOST_%d";
  public static final String ALL_AGENT_NAMES = "ALL_AGENT_NAMES";
  public static final String ALL_AGENT_HOSTS = "ALL_AGENT_HOSTS";
  public static final String ALL_AGENTS = "ALL_AGENTS";
  public static final String AGENT_GROUP_NAME = "AGENT_GROUP_NAME";
  public static final String fAGENT_GROUP_NAME = "AGENT_GROUP_NAME_%d";
  public static final String ALL_AGENT_GROUP_NAMES = "ALL_AGENT_GROUP_NAMES";
  public static final String MONITOR_NAME = "MONITOR_NAME";
  public static final String fMONITOR_NAME = "MONITOR_NAME_%d";
  public static final String MONITOR_HOST = "MONITOR_HOST";
  public static final String fMONITOR_HOST = "MONITOR_HOST_%d";
  public static final String ALL_MONITOR_NAMES = "ALL_MONITOR_NAMES";
  public static final String ALL_MONITOR_HOSTS = "ALL_MONITOR_HOSTS";
  public static final String ALL_MONITORS = "ALL_MONITORS";
  public static final String COLLECTOR_NAME = "COLLECTOR_NAME";
  public static final String fCOLLECTOR_NAME = "COLLECTOR_NAME_%d";
  public static final String COLLECTOR_HOST = "COLLECTOR_HOST";
  public static final String fCOLLECTOR_HOST = "COLLECTOR_HOST_%d";
  public static final String ALL_COLLECTOR_NAMES = "ALL_COLLECTOR_NAMES";
  public static final String ALL_COLLECTOR_HOSTS = "ALL_COLLECTOR_HOSTS";
  public static final String ALL_COLLECTORS = "ALL_COLLECTORS";
  public static final String SERVER_NAME = "SERVER_NAME";
  public static final String fSERVER_NAME = "SERVER_NAME_%d";
  public static final String SERVER_HOST = "SERVER_HOST";
  public static final String fSERVER_HOST = "SERVER_HOST_%d";
  public static final String ALL_SERVER_NAMES = "ALL_SERVER_NAMES";
  public static final String MESSAGE = "MESSAGE";
  public static final String RULE_NAME = "RULE_NAME";
  public static final String RULE_DESCRIPTION = "RULE_DESCRIPTION";
  public static final String SENSITIVITY = "SENSITIVITY";
  public static final String SESSION_ID = "SESSION_ID";
  public static final String SESSION_NAME = "SESSION_NAME";
  public static final String DURATION = "DURATION";
  public static final String IS_OPEN = "IS_OPEN";
  public static final String IS_CLOSED = "IS_CLOSED";
  public static final String SEVERITY = "SEVERITY";
  public static final String KEY = "KEY";
  public static final String STATE = "STATE";
  public static final String APPLICATION = "APPLICATION";
  public static final String VIOLATED_MEASURE_NAME = "VIOLATED_MEASURE_NAME";
  public static final String VIOLATED_MEASURE_DESCRIPTION = "VIOLATED_MEASURE_DESCRIPTION";
  public static final String VIOLATED_MEASURE_VALUE = "VIOLATED_MEASURE_VALUE";
  public static final String VIOLATED_MEASURE_UNIT = "VIOLATED_MEASURE_UNIT";
  public static final String VIOLATED_MEASURE_CONFIGURATION = "VIOLATED_MEASURE_CONFIGURATION";
  public static final String VIOLATED_MEASURE_SPLITTINGS = "VIOLATED_MEASURE_SPLITTINGS";
  public static final String VIOLATED_MEASURE_TRESHOLD_UPPER_SEVERE = "VIOLATED_MEASURE_TRESHOLD_UPPER_SEVERE";
  public static final String VIOLATED_MEASURE_TRESHOLD_LOWER_SEVERE = "VIOLATED_MEASURE_TRESHOLD_LOWER_SEVERE";
  public static final String VIOLATED_MEASURE_TRESHOLD_UPPER_WARNING = "VIOLATED_MEASURE_TRESHOLD_UPPER_WARNING";
  public static final String VIOLATED_MEASURE_TRESHOLD_LOWER_WARNING = "VIOLATED_MEASURE_TRESHOLD_LOWER_WARNING";
  public static final String VIOLATED_MEASURE_METRIC_NAME = "VIOLATED_MEASURE_METRIC_NAME";
  public static final String VIOLATED_MEASURE_METRIC_DESCRIPTION = "VIOLATED_MEASURE_METRIC_DESCRIPTION";
  public static final String VIOLATED_MEASURE_METRIC_GROUP = "VIOLATED_MEASURE_METRIC_GROUP";
  public static final String VIOLATED_MEASURE_METRIC_UNIT = "VIOLATED_MEASURE_METRIC_UNIT";
  public static final String VIOLATED_TRIGGER_VALUE_SOURCE_TYPE = "VIOLATED_TRIGGER_VALUE_SOURCE_TYPE";
  public static final String VIOLATED_TRIGGER_VALUE_SOURCE_NAME = "VIOLATED_TRIGGER_VALUE_SOURCE_NAME";
  public static final String VIOLATED_TRIGGER_VALUE_SOURCE_HOST = "VIOLATED_TRIGGER_VALUE_SOURCE_HOST";
  public static final String VIOLATED_TRIGGER_VALUE = "VIOLATED_TRIGGER_VALUE";
  public static final String fVIOLATION_HEADER = "VIOLATION_HEADER_%d";
  public static final String fVIOLATION_MESSAGE = "VIOLATION_MESSAGE_%d";
  public static final String INCIDENT_STARTED_ENDED = "INCIDENT_STARTED_ENDED";
  public static final String IMAGE_WARNING_OK = "IMAGE_WARNING_OK";

  public void set(final String name, final Threshold threshold) {
    final Value v = (threshold != null) ? threshold.getValue() : null;
    final Double d = (v.isSet()) ? v.getValue() : null;
    data.put(name, d != null ? d.toString() : null);
  }

  public static AppMonActionData fromEnvironment(final ActionEnvironment env, final SimpleDateFormat dateFormat) {
    final List<Incident> list = new ArrayList<>();
    final AppMonActionData actionData = new AppMonActionData();
    final Collection<Incident> incidents = env.getIncidents();
    if ((incidents != null) && !incidents.isEmpty()) {
      for (final Incident incident : incidents) {
        if (incident == null) {
          continue;
        }
        list.add(incident);
      }
    }
    final Incident firstIncident = list.size() > 0 ? list.get(0) : null;
    final Sources s = new Sources(list);
    final Map<SourceType, SourceReferences> sources = s.sources;

    actionData.set(AppMonActionData.SYSTEM_PROFILE, env.getSystemProfileName());

    if (s != null) {
      SourceReferences sr = sources.get(SourceType.Agent);
      if (sr != null) {
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.AGENT_NAME, AppMonActionData.fAGENT_NAME, true);
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.AGENT_HOST, AppMonActionData.fAGENT_HOST, false);
        actionData.set(AppMonActionData.ALL_AGENTS, sr.getAllNameHosts());
        actionData.set(AppMonActionData.ALL_AGENT_NAMES, sr.getAllNames());
        actionData.set(AppMonActionData.ALL_AGENT_HOSTS, sr.getAllHosts());
      }
      sr = sources.get(SourceType.AgentGroup);
      if (sr != null) {
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.AGENT_GROUP_NAME, AppMonActionData.fAGENT_GROUP_NAME, true);
        actionData.set(AppMonActionData.ALL_AGENT_GROUP_NAMES, sr.getAllNames());
      }
      sr = sources.get(SourceType.Monitor);
      if (sr != null) {
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.MONITOR_NAME, AppMonActionData.fMONITOR_NAME, true);
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.MONITOR_HOST, AppMonActionData.fMONITOR_HOST, false);
        actionData.set(AppMonActionData.ALL_MONITORS, sr.getAllNameHosts());
        actionData.set(AppMonActionData.ALL_MONITOR_NAMES, sr.getAllNames());
        actionData.set(AppMonActionData.ALL_MONITOR_HOSTS, sr.getAllHosts());
        final List<String> hosts = sr.getSourceHosts();
        actionData.set(AppMonActionData.MONITOR_HOST, ((hosts != null) && (hosts.size() > 0)) ? hosts.get(0) : null);
      }
      sr = sources.get(SourceType.Collector);
      if (sr != null) {
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.COLLECTOR_NAME, AppMonActionData.fCOLLECTOR_NAME, true);
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.COLLECTOR_HOST, AppMonActionData.fCOLLECTOR_HOST, false);
        actionData.set(AppMonActionData.ALL_COLLECTORS, sr.getAllNameHosts());
        actionData.set(AppMonActionData.ALL_COLLECTOR_NAMES, sr.getAllNames());
        actionData.set(AppMonActionData.ALL_COLLECTOR_HOSTS, sr.getAllHosts());
      }
      sr = sources.get(SourceType.Server);
      if (sr != null) {
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.SERVER_NAME, AppMonActionData.fSERVER_NAME, true);
        AppMonActionData.setSourceTypeVariables(actionData, sr, AppMonActionData.SERVER_HOST, AppMonActionData.fSERVER_HOST, false);
        actionData.set(AppMonActionData.ALL_SERVER_NAMES, sr.getAllHosts());
      }
    }
    if (firstIncident != null) {
      final Violation violation = AppMonActionData.getViolation(firstIncident);
      // populate fields which are coming from the Incident instance, ViolatedMeasure instance,
      final IncidentRule rule = firstIncident.getIncidentRule();
      final Sensitivity sensitivity = (rule != null) ? rule.getSensitivity() : null;
      final Key key = firstIncident.getKey();
      final Measure m = (violation != null) ? violation.getViolatedMeasure() : null;
      final Source source = (m != null) ? m.getSource() : null;
      final Metric metric = (m != null) ? m.getMetric() : null;
      final TriggerValue tv = AppMonActionData.getTriggerValue(violation);
      final boolean isOpen = firstIncident.isOpen();
      actionData.set(AppMonActionData.DYNATRACE_SERVER_NAME, firstIncident.getServerName());
      actionData.set(AppMonActionData.START_TIME, AppMonActionData.getDateAsString(firstIncident.getStartTime(), dateFormat));
      actionData.set(AppMonActionData.END_TIME, AppMonActionData.getDateAsString(firstIncident.getEndTime(), dateFormat));
      actionData.set(AppMonActionData.MESSAGE, firstIncident.getMessage());
      actionData.set(AppMonActionData.RULE_NAME, (rule != null) ? rule.getName() : null);
      actionData.set(AppMonActionData.RULE_DESCRIPTION, (rule != null) ? rule.getDescription() : null);
      actionData.set(AppMonActionData.SENSITIVITY, (sensitivity != null) ? sensitivity.getType().name() : null);
      actionData.set(AppMonActionData.SESSION_ID, firstIncident.getRecordedSessionId());
      actionData.set(AppMonActionData.SESSION_NAME, firstIncident.getRecordedSessionName());
      actionData.set(AppMonActionData.DURATION, AppMonActionData.getDurationAsString(firstIncident));
      actionData.set(AppMonActionData.IS_OPEN, isOpen);
      if (isOpen) {
        actionData.set(AppMonActionData.INCIDENT_STARTED_ENDED, "started");
        actionData.set(AppMonActionData.IMAGE_WARNING_OK, "res/notification_email_warning.png");
      }
      else {
        actionData.set(AppMonActionData.INCIDENT_STARTED_ENDED, "ended");
        actionData.set(AppMonActionData.IMAGE_WARNING_OK, "res/notification_email_ok.png");
      }
      actionData.set(AppMonActionData.IS_CLOSED, firstIncident.isClosed());
      actionData.set(AppMonActionData.SEVERITY, AppMonActionData.getSeverityString(firstIncident));
      actionData.set(AppMonActionData.KEY, key.getUUID());
      actionData.set(AppMonActionData.STATE, String.valueOf(firstIncident.getState()));
      if (m != null) {
        actionData.set(AppMonActionData.APPLICATION, m.getApplication());
        final String measureName = m.getName();
        actionData.set(AppMonActionData.VIOLATED_MEASURE_NAME, measureName);
        actionData.set(AppMonActionData.VIOLATED_MEASURE_DESCRIPTION, m.getDescription());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_UNIT, m.getUnit());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_CONFIGURATION, m.getConfigurationSummary());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_TRESHOLD_UPPER_SEVERE, m.getUpperSevere());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_TRESHOLD_LOWER_SEVERE, m.getLowerSevere());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_TRESHOLD_UPPER_WARNING, m.getUpperWarning());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_TRESHOLD_LOWER_WARNING, m.getLowerWarning());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_SPLITTINGS, LibStr.merge(m.getSplittings(), ",", ""));
      }
      if (tv != null) {
        actionData.set(AppMonActionData.VIOLATED_MEASURE_VALUE, tv.getValue().getValue());
        actionData.set(AppMonActionData.VIOLATED_TRIGGER_VALUE, tv.getValue().getValue());
      }
      if (metric != null) {
        actionData.set(AppMonActionData.VIOLATED_MEASURE_METRIC_NAME, metric.getName());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_METRIC_DESCRIPTION, metric.getDescription());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_METRIC_GROUP, metric.getGroup());
        actionData.set(AppMonActionData.VIOLATED_MEASURE_METRIC_UNIT, metric.getUnit());
      }
      if (source != null) {
        actionData.set(AppMonActionData.VIOLATED_TRIGGER_VALUE_SOURCE_TYPE, source.getSourceType().name());
        actionData.set(AppMonActionData.VIOLATED_TRIGGER_VALUE_SOURCE_NAME, AppMonActionData.getSourceName(source));
        actionData.set(AppMonActionData.VIOLATED_TRIGGER_VALUE_SOURCE_HOST, AppMonActionData.getSourceHost(source));
      }
      // Setup VIOLATION_HEADER, VIOLATION_MESSAGE entries
      final Map<String, List<String>> map = AppMonActionData.getViolations(firstIncident);
      final Set<Entry<String, List<String>>> set = (map != null) ? map.entrySet() : null;
      final List<String> violHeaders = new ArrayList<>();
      final List<String> violMessages = new ArrayList<>();
      final int sizeOfViolationHeader = 5;
      if ((set != null) && !set.isEmpty()) {
        int i = -1;
        for (final Entry<String, List<String>> entry : set) {
          // pick up only top "sizeOfViolationHeader" violations
          if (++i >= sizeOfViolationHeader) {
            break;
          }
          violHeaders.add(entry.getKey());
          final List<String> data = entry.getValue();
          violMessages.add((i < data.size()) ? data.get(i) : null);
        }
        actionData.set(AppMonActionData.fVIOLATION_HEADER, violHeaders, sizeOfViolationHeader);
        actionData.set(AppMonActionData.fVIOLATION_MESSAGE, violMessages, sizeOfViolationHeader);
      }
    }
    return actionData;
  }

  public static String getDateAsString(final Timestamp time, final SimpleDateFormat dateFormat) {
    String timeString;
    if ((time != null) && (time.getTimestampInMs() > 0)) {
      if (dateFormat != null) {
        try {
          synchronized (dateFormat) {
            timeString = dateFormat.format(time.getTimestampInMs());
          }
        }
        catch (final Exception e) {
          timeString = null;
        }
      }
      else {
        // use date format from default locale
        timeString = new Date(time.getTimestampInMs()).toString();
      }
    }
    else {
      timeString = null;
    }
    return timeString;
  }

  public static Violation getViolation(final Incident incident) {
    if (incident == null) { return null; }
    final Collection<Violation> violations = incident.getViolations();
    if ((violations != null) && !violations.isEmpty()) {
      for (final Violation violation : violations) {
        if (violation == null) {
          continue;
        }
        final Measure measure = violation.getViolatedMeasure();
        if (measure == null) {
          continue;
        }
        final Source source = measure.getSource();
        // skip violations which have no agent name or no monitor name in the
        if (source instanceof AgentSource) {
          // agent
          final String s = ((AgentSource)source).getName();
          if (LibStr.isNotEmptyOrNull(s)) { return violation; }
        }
        else if (source instanceof MonitorSource) {
          // monitor
          if (measure.getName().indexOf(BaseConstants.AT) != -1) { return violation; }
        }
      }
      // pick up the first violation in the violations
      return violations.iterator().next();
    }
    return null;
  }

  public static String getDurationAsString(final Incident incident) {
    String duration;
    if (incident.isClosed()) {
      final long durationInSeconds = incident.getDuration().getDurationInMs() / 1000;
      if (durationInSeconds < 0) {
        duration = BaseConstants.QMARK;
      }
      else {
        duration = DurationFormat.formatDuration(durationInSeconds);
      }
    }
    else {
      duration = null;
    }
    return duration;
  }

  public static Map<String, List<String>> getViolations(final Incident incident) {
    final Map<String, List<String>> result = new HashMap<>();
    if ((incident == null) || (incident.getViolations() == null)) { return result; }
    String headerName = null;
    List<String> values;
    String location = "";
    violation: for (final Violation violation : incident.getViolations()) {
      // skip nulls
      if (violation == null) {
        continue;
      }
      final Measure violatedMeasure = violation.getViolatedMeasure();
      final String violatedMeasureName = (violatedMeasure != null) ? violatedMeasure.getName() : null;
      final Metric metric = (violatedMeasure != null) ? violatedMeasure.getMetric() : null;
      final String metricName = (metric != null) ? metric.getName() : null;
      if ((violatedMeasure != null) && LibStr.isNotEmptyOrNull(violatedMeasureName) && (metric != null) && (metricName != null)) {
        final Source source = violatedMeasure.getSource();
        // for AgentGroup we have special algorithm
        if ((source != null) && !source.getSourceType().equals(SourceType.Monitor)) {
          headerName = violatedMeasureName;
        }
        else {
          // Skip violation if it has the same violated measure name as metric name has and number
          // of violations for a
          // given incident more than 1
          if (metricName.equals(violatedMeasureName) && (incident.getViolations().size() > 1)) {
            continue;
          }
          else {
            headerName = metricName.trim();
            if (violatedMeasureName.length() > headerName.length()) {
              location = violatedMeasureName.substring(headerName.length()).trim();
            }
          }
        }
      }
      else if ((violatedMeasure != null) && (metric != null) && LibStr.isEmptyOrNull(metric.getName())) {
        headerName = metric.getName();
      }
      else {
        headerName = null;
      }
      // skip to next violation if headerName is already in the map
      for (final String s : result.keySet()) {
        if (headerName.length() > s.length()) {
          if (headerName.indexOf(s) != -1) {
            // will replace s with headerName
            result.remove(s);
          }
        }
        else if (headerName.length() < s.length()) {
          if (s.indexOf(headerName) != -1) {
            // skip this violation
            continue violation;
          }
        }
        else if (headerName.equals(s)) {
          // skip violation
          continue violation;
        }
      }
      values = new ArrayList<>();
      if (location.length() > 2) {
        // remove parenthesis
        location = new StringBuilder(location.substring(1, location.length() - 1)).append(BaseConstants.COLON_WS).toString();
      }
      final Threshold violatedThreshold = violation.getViolatedThreshold();
      final Collection<TriggerValue> tvaluesCollection = (violatedThreshold != null) ? violation.getTriggerValues() : null;
      if ((violatedThreshold != null) && (tvaluesCollection != null)) {
        for (final TriggerValue triggerValue : tvaluesCollection) {
          final StringBuilder triggerValueAsString = new StringBuilder(location);
          final Double dTriggerValue = triggerValue.getValue().getValue();
          final String measuredValue = new StringBuilder(Messages.DEFAULT_DECIMAL_FORMAT.format(dTriggerValue)).append(BaseConstants.WS).append(violatedMeasure.getUnit()).toString();
          final double thresholdValue = violatedThreshold.getValue().getValue();
          final String thresholdvalueString = new StringBuilder(Messages.DEFAULT_DECIMAL_FORMAT.format(thresholdValue)).append(BaseConstants.WS).append(violatedMeasure.getUnit()).toString();
          final Threshold.Type ttype = violatedThreshold.getType();
          if (ttype == Threshold.Type.UpperSevere) {
            triggerValueAsString.append(MessageFormat.format(Messages.FMT_MEASURED_VALUE_WAS_LOWER_THAN_THRESHOLD_EMAIL_TEXT, measuredValue, thresholdvalueString));
          }
          else if (ttype == Threshold.Type.UpperWarning) {
            triggerValueAsString.append(MessageFormat.format(Messages.FMT_MEASURED_VALUE_WAS_LOWER_THAN_THRESHOLD_EMAIL_TEXT, measuredValue, thresholdvalueString));
          }
          else if (ttype == Threshold.Type.LowerSevere) {
            triggerValueAsString.append(MessageFormat.format(Messages.FMT_MEASURED_VALUE_WAS_HIGHER_THAN_THRESHOLD_EMAIL_TEXT, measuredValue, thresholdvalueString));
          }
          else if (ttype == Threshold.Type.LowerWarning) {
            triggerValueAsString.append(MessageFormat.format(Messages.FMT_MEASURED_VALUE_WAS_HIGHER_THAN_THRESHOLD_EMAIL_TEXT, measuredValue, thresholdvalueString));
          }
          // do not add values if they are already there
          final String s = triggerValueAsString.toString();
          if (!values.contains(s)) {
            values.add(s);
          }
        }
        result.put(headerName, values);
      }
    }
    return result;
  }

  public static void setSourceTypeVariables(final AppMonActionData record, final SourceReferences sr, final String keyName, final String keyFormat, final boolean isNames) {
    List<String> list;
    // Populate Variable field
    if (isNames) {
      // NAME variables are getting populated
      list = sr.getSourceNames();
    }
    else {
      // HOST variables are getting populated
      list = sr.getSourceHosts();
    }
    record.set(keyFormat, list, 5);
    if (list.size() > 0) {
      record.set(keyName, list.get(0));
    }
  }

  public static String getSeverityString(final Incident incident) {
    if (incident != null) {
      final Severity severity = incident.getSeverity();
      switch (severity) {
        case Error:
          return Messages.STRING_SEVERE;
        case Warning:
          return Messages.STRING_WARNING;
        case Informational:
          return Messages.STRING_INFORMATIONAL;
      }
    }
    return null;
  }

  public static String getSeverityString(final List<Incident> incidents) {
    String result = null;
    int severity = 0;
    for (final Incident incident : incidents) {
      switch (incident.getSeverity()) {
        case Error:
          severity = 3;
          break;
        case Warning:
          if (severity < 2) {
            severity = 2;
          }
          break;
        case Informational:
          if (severity < 1) {
            severity = 1;
          }
          break;
      }
      if (severity == 3) {
        break;
      }
    }
    switch (severity) {
      case 3:
        result = Messages.STRING_SEVERE;
        break;
      case 2:
        result = Messages.STRING_WARNING;
        break;
      case 1:
        result = Messages.STRING_INFORMATIONAL;
        break;
      default:
        result = BaseConstants.QMARK;
    }
    return result;
  }

  public static TriggerValue getTriggerValue(final Violation v) {
    if (v == null) { return null; }
    final Collection<TriggerValue> t = v.getTriggerValues();
    if ((t != null) && !t.isEmpty()) {
      final TriggerValue tv = t.iterator().next();
      if ((tv != null) && (tv.getValue() != null)) { return tv; }
    }
    return null;
  }

  public static String getSourceName(final Source source) {
    String a = null;
    switch (source.getSourceType()) {
      case Agent:
        a = ((AgentSource)source).getName();
        break;
      case AgentGroup:
        a = Arrays.toString(((AgentGroupSource)source).getAgentGroupNames().toArray());
        // remove brackets
        a = a.substring(1, a.length() - 1);
        break;
      case Monitor:
        a = ((MonitorSource)source).getName();
        break;
      case Collector:
        a = ((CollectorSource)source).getName();
        break;
      case Server:
        a = ((ServerSource)source).getName();
        break;
    }
    return a;
  }

  public static String getSourceHost(final Source source) {
    String a = null;
    switch (source.getSourceType()) {
      case Agent:
        a = ((AgentSource)source).getHost();
        break;
      case Monitor:
        a = ((MonitorSource)source).getName();
        // extract host
        final int i = a.indexOf('@');
        if (i != -1) {
          a = a.substring(i);
        }
        break;
      case Collector:
        a = ((CollectorSource)source).getHost();
        break;
      case Server:
        a = ((ServerSource)source).getName();
        break;
      default:
        break;
    }
    return a;
  }

}
