/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.dynatrace.lib.appmon;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.ext.library.gson.GsonUtil;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibDate;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.regex.LibRegEx;
import net.eiroca.library.rule.RegExRule;

public class AppMonProcessor extends GenericProcessor {

  private static SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  public static final String FLD_SERVICE = "service";
  public static final String DEFAULT_SERVICE = "";

  private static final String NODE_AGENT_INSTANCE_NAME = "agentInstanceName";
  private static final String NODE_AGENTINFORMATION = "agentinformation";

  private static final String FMT_AGENT_STATUS = "Dynatrace agents status alert";
  private static final String FMT_STATUS_ALERT = "%s status alert";

  protected static final DateFormat HHMM = new SimpleDateFormat("HH:mm");
  protected static final Date START = new Date(0);
  protected static final Date END = new Date(LibDate.ONE_DAY + 1);

  private final Map<String, ServiceStatus> services = new HashMap<>();

  private Alert agentAlert = null;

  private final String defProfile;
  private final String defRule;

  private final static Map<String, AlertSeverity> dyna2severity = new HashMap<>();
  private final static Map<AlertSeverity, String> severity2dyna = new HashMap<>();
  private final static Map<String, AlertState> dyna2state = new HashMap<>();
  private final static Map<AlertState, String> state2dyna = new HashMap<>();

  static {
    AppMonProcessor.dyna2severity.put("informational", AlertSeverity.INFO);
    AppMonProcessor.dyna2severity.put("warning", AlertSeverity.WARN);
    AppMonProcessor.dyna2severity.put("severe", AlertSeverity.SEVERE);
    //
    AppMonProcessor.severity2dyna.put(AlertSeverity.INFO, "informational");
    AppMonProcessor.severity2dyna.put(AlertSeverity.WARN, "warning");
    AppMonProcessor.severity2dyna.put(AlertSeverity.SEVERE, "severe");
    AppMonProcessor.severity2dyna.put(AlertSeverity.CRITICAL, "severe");
    //
    AppMonProcessor.dyna2state.put("InProgress", AlertState.INPROGRESS);
    AppMonProcessor.dyna2state.put("Confirmed", AlertState.CONFIRMED);
    AppMonProcessor.dyna2state.put("Created", AlertState.NEW);
    //
    AppMonProcessor.state2dyna.put(AlertState.INPROGRESS, "InProgress");
    AppMonProcessor.state2dyna.put(AlertState.CONFIRMED, "Confirmed");
    AppMonProcessor.state2dyna.put(AlertState.NEW, "Created");
    AppMonProcessor.state2dyna.put(AlertState.CLOSED, "Created");
  }

  public AppMonProcessor(final String defProfile, final String defRule) {
    this.defProfile = defProfile;
    this.defRule = defRule;
  }

  public void readAlerts(final AppMonServer[] servers, final Date startDate, final Date endDate, final List<Alert> alerts) {
    alerts.clear();
    for (final AppMonServer server : servers) {
      final String alertsList = server.readAlertsList(null, null, null, startDate, endDate);
      importAlerts(server, alerts, alertsList);
    }
  }

  synchronized ServiceStatus getServiceStatusByName(final String serviceName) {
    ServiceStatus result = services.get(serviceName);
    if (result == null) {
      result = new ServiceStatus(serviceName, null);
      services.put(serviceName, result);
    }
    return result;
  }

  public void processAlerts(final List<Alert> alerts) {
    Collections.sort(alerts);
    final List<ServiceStatus> impactedServices = new ArrayList<>();
    for (final Alert alert : alerts) {
      impactedServices.clear();
      if (alert.isNew()) {
        getImpactServices(alert, impactedServices);
      }
      for (final ServiceStatus service : impactedServices) {
        service.updateAlert(alert);
      }
    }
  }

  public void updateServices() {
    for (final ServiceStatus s : services.values()) {
      s.updateStatus();
      GenericProcessor.logger.info("STATUS=" + s);
    }
  }

  void getImpactServices(final Alert alert, final List<ServiceStatus> impactedServices) {
    final ServiceStatus serviceStatus = getImpactServices(alert);
    if (serviceStatus != null) {
      impactedServices.add(serviceStatus);
    }
  }

  public ServiceStatus getImpactServices(final Alert alert) {
    String serviceName = alert.system;
    final Map<String, String> def = AppMonConfig.system2service.lookup(serviceName);
    if (def == null) {
      serviceName = null;
    }
    else {
      serviceName = def.get(AppMonProcessor.FLD_SERVICE);
    }
    if (serviceName == null) {
      serviceName = AppMonProcessor.DEFAULT_SERVICE;
    }
    return LibStr.isNotEmptyOrNull(serviceName) ? getServiceStatusByName(serviceName) : null;
  }

  public void resetServices() {
    for (final ServiceStatus status : services.values()) {
      status.reset();
    }
  }

  public void raiseAlerts(final AppMonServer server) {
    for (final ServiceStatus serviceStatus : services.values()) {
      boolean isNew = false;
      if (serviceStatus.state != ServiceState.OK) {
        if (serviceStatus.alert == null) {
          serviceStatus.alert = newAlert(serviceStatus.lastChange);
          isNew = true;
        }
        Alert alert = serviceStatus.alert;
        AlertSeverity sev;
        switch (serviceStatus.state) {
          case ERROR:
            sev = AlertSeverity.WARN;
            break;
          case FAILED:
            sev = AlertSeverity.SEVERE;
            break;
          case WARNING:
            sev = AlertSeverity.INFO;
            break;
          default:
            sev = AlertSeverity.INFO;
            break;
        }
        if (!isNew && (alert.severity != sev)) {
          // Cambio severity, chiude vecchio e apre nuovo
          server.closeAlert(alert);
          final Date startDate = alert.start;
          alert = newAlert(startDate);
          serviceStatus.alert = alert;
          isNew = true;
        }
        alert.severity = sev;
        final List<String> rootCause = new ArrayList<>();
        for (final Alert a : serviceStatus.alerts) {
          // if (a.severity == AlertSeverity.INFO) continue;
          // if (a.state != AlertState.NEW) continue;
          String rule = a.rule;
          if (rule == null) {
            rule = "m:" + a.message;
          }
          if (rootCause.contains(rule)) {
            continue;
          }
          rootCause.add(rule);
          if (rootCause.size() >= 3) {
            break;
          }
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rootCause.size(); i++) {
          if (i > 0) {
            sb.append(",\n");
          }
          sb.append(rootCause.get(i));
        }
        alert.description = sb.toString();
        alert.message = String.format(AppMonProcessor.FMT_STATUS_ALERT, serviceStatus.name);
        if (isNew) {
          server.createAlert(alert);
          GenericProcessor.logger.info("NEW_ALERT=" + alert);
        }
      }
      else {
        if (serviceStatus.alert != null) {
          GenericProcessor.logger.info("Closing " + serviceStatus.alert.id);
          server.closeAlert(serviceStatus.alert);
          serviceStatus.alert = null;
        }
      }
    }
  }

  public Alert newAlert(final Date when) {
    final Alert alert = new Alert();
    alert.system = defProfile;
    alert.rule = defRule;
    alert.start = when == null ? new Date() : when;
    alert.end = null;
    return alert;
  }

  public void importAlerts(final AppMonServer server, final List<Alert> alerts, final String json) {
    if (json == null) { return; }
    final JsonObject data = JsonParser.parseString(json).getAsJsonObject();
    final JsonArray records = data.getAsJsonArray("alerts");
    for (int i = 0; i < records.size(); i++) {
      final JsonObject alert = records.get(i).getAsJsonObject();
      final String id = GsonUtil.getString(alert, "id");
      final String alertJson = server.readAlert(id);
      final Alert dynaAlert = fromDynaTraceJson(id, alertJson);
      alerts.add(dynaAlert);
    }
  }

  public void closePending(final AppMonServer server) {
    final String alertsJSon = server.readAlertsList(defProfile, defRule, null, new Date(), null);
    final List<Alert> alerts = new ArrayList<>();
    importAlerts(server, alerts, alertsJSon);
    for (final Alert a : alerts) {
      server.closeAlert(a);
    }
  }

  public boolean readAgents(final AppMonServer[] servers, final List<Agent> agents) {
    boolean result = true;
    agents.clear();
    for (final AppMonServer server : servers) {
      final String xml = server.readAgents();
      result = result & parseAgents(agents, xml);
    }
    return result;
  }

  private boolean parseAgents(final List<Agent> agents, final String xml) {
    if (xml == null) { return false; }
    try {
      final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      final InputSource is = new InputSource(new StringReader(xml));
      final Document doc = dBuilder.parse(is);
      doc.getDocumentElement().normalize();
      final NodeList nList = doc.getElementsByTagName(AppMonProcessor.NODE_AGENTINFORMATION);
      for (int temp = 0; temp < nList.getLength(); temp++) {
        final Node nNode = nList.item(temp);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
          final Element eElement = (Element)nNode;
          final Agent agent = new Agent();
          agent.instanceName = eElement.getElementsByTagName(AppMonProcessor.NODE_AGENT_INSTANCE_NAME).item(0).getTextContent();
          agents.add(agent);
        }
      }
    }
    catch (final Exception e) {
      GenericProcessor.logger.error("Error parsing agents", e);
      return false;
    }
    return true;
  }

  public void validateAgents(final AppMonServer server, final List<Agent> agents) {
    final List<String> violations = new ArrayList<>();
    Collections.sort(agents);
    int minSeverity = Integer.MAX_VALUE;
    for (final String[] def : AppMonConfig.rules_agents.getDefinitions().getData()) {
      int fld = 0;
      final String regEx = def[fld++];
      final int minCount = Helper.getInt(def[fld++], 0);
      final int maxCount = Helper.getInt(def[fld++], Integer.MAX_VALUE);
      final Date minH = Helper.getDate(def[fld++], AppMonProcessor.HHMM, AppMonProcessor.START);
      final Date maxH = Helper.getDate(def[fld++], AppMonProcessor.HHMM, AppMonProcessor.END);
      final long curH = (new Date().getTime()) % LibDate.ONE_DAY;
      if ((curH < minH.getTime()) || (curH > maxH.getTime())) {
        continue;
      }
      final String message = def[fld++];
      final int severity = Helper.getInt(def[fld++], Integer.MAX_VALUE);
      final RegExRule pattern = AppMonConfig.rules_agents.getPattern(regEx);
      int count = 0;
      for (final Agent a : agents) {
        final Matcher m = pattern.getMatcher(a.instanceName);
        if (m.find()) {
          count++;
        }
      }
      if ((count < minCount) || (count > maxCount)) {
        GenericProcessor.logger.warn("Violation: " + message + " " + minCount + "<=" + count + "<=" + maxCount + " H " + (minH.getTime() / LibDate.ONE_HOUR) + "<" + (curH / LibDate.ONE_HOUR) + "<" + (maxH.getTime() / LibDate.ONE_HOUR));
        violations.add(message);
        minSeverity = Math.min(minSeverity, severity);
      }
    }
    if (violations.size() > 0) {
      final String description = LibStr.merge(violations, Helper.NL, "");
      if ((agentAlert == null) || (!agentAlert.description.equals(description))) {
        if (agentAlert != null) {
          server.closeAlert(agentAlert);
        }
        agentAlert = newAlert(null);
        agentAlert.severity = (minSeverity == 1) ? AlertSeverity.SEVERE : AlertSeverity.WARN;
        agentAlert.message = String.format(AppMonProcessor.FMT_AGENT_STATUS, agentAlert.severity);
        agentAlert.description = description;
        server.createAlert(agentAlert);
      }
    }
    else {
      if (agentAlert != null) {
        server.closeAlert(agentAlert);
        agentAlert = null;
      }
    }
  }

  public Alert fromDynaTraceJson(final String id, final String json) {
    final Alert a = new Alert(id);
    final JsonObject data = JsonParser.parseString(json).getAsJsonObject();
    a.system = GsonUtil.getString(data, "systemprofile");
    a.rule = GsonUtil.getString(data, "rule");
    a.message = GsonUtil.getString(data, "message");
    a.description = GsonUtil.getString(data, "description");
    a.start = GsonUtil.getDate(data, "start", AppMonProcessor.ISO8601);
    a.end = GsonUtil.getDate(data, "end", AppMonProcessor.ISO8601);
    final String stateStr = GsonUtil.getString(data, "state");
    a.state = AppMonProcessor.dyna2state.get(stateStr);
    if (a.state == null) {
      a.state = AlertState.NEW;
    }
    if ((a.state == AlertState.NEW) && (a.end != null)) {
      a.state = AlertState.CLOSED;
    }
    final String severityStr = GsonUtil.getString(data, "severity");
    a.severity = AppMonProcessor.dyna2severity.get(severityStr);
    if (a.severity == null) {
      a.severity = AlertSeverity.INFO;
    }
    if (a.message != null) {
      processMessage(a);
    }
    if (a.description != null) {
      processDescription(a);
    }
    a.measureStatus = null;
    if (a.measureValue != null) {
      if (a.measureName == null) {
        a.measureName = "Response time";
      }
      final double value = a.measureValue;
      double limit;
      double warn;
      double delta;
      if (a.measureUpperLimit != null) {
        limit = a.measureUpperLimit;
        warn = a.measureUpperLimitWarning != null ? a.measureUpperLimitWarning : 0;
        delta = (limit - warn);
        a.measureStatus = value < warn ? 0 : value < limit ? value / limit : 1 + ((value - limit) / delta);
      }
      else if (a.measureUpperLimit != null) {
        limit = a.measureUpperLimit;
        warn = a.measureUpperLimitWarning != null ? a.measureUpperLimitWarning : 0;
        delta = (warn - limit);
        a.measureStatus = value > warn ? 0 : value > limit ? value / limit : 1 + ((limit - value) / delta);
      }
    }
    return a;
  }

  private void processDescription(final Alert a) {
    for (final String[] def : AppMonConfig.replace_description.getDefinitions().getData()) {
      final String regEx = def[0];
      final String replace = def[1];
      final RegExRule pattern = AppMonConfig.replace_description.getPattern(regEx);
      final Matcher m = pattern.getMatcher(a.description);
      if (m.find()) {
        pattern.hits++;
        a.measureName = LibRegEx.getField(m, "measurename");
        a.measureUnit = LibRegEx.getField(m, "measureunit");
        a.measureValue = LibRegEx.getDouble(m, "measurevalue");
        a.measureUpperLimit = LibRegEx.getDouble(m, "measureupper");
        a.measureUpperLimitWarning = LibRegEx.getDouble(m, "measureupperwarning");
        a.measureLowerLimit = LibRegEx.getDouble(m, "measurelower");
        a.measureLowerLimitWarning = LibRegEx.getDouble(m, "measurelowerwarning");
        final double ct = (LibRegEx.getDouble(m, "cm", 0) * 60000) + (LibRegEx.getDouble(m, "cs", 0) * 1000) + LibRegEx.getDouble(m, "cms", 0);
        final double et = (LibRegEx.getDouble(m, "em", 0) * 60000) + (LibRegEx.getDouble(m, "es", 0) * 1000) + LibRegEx.getDouble(m, "ems", 0);
        if (ct > 0) {
          a.measureUnit = "ms";
          a.measureValue = ct;
          a.measureUpperLimit = et;
        }
        a.agent = LibRegEx.getField(m, "agent");
        a.host = LibRegEx.getField(m, "host");
        a.description = m.replaceAll(replace);
      }
      break;
    }
  }

  private void processMessage(final Alert a) {
    for (final String[] def : AppMonConfig.replace_message.getDefinitions().getData()) {
      final String regEx = def[0];
      final String replace = def[1];
      final RegExRule pattern = AppMonConfig.replace_message.getPattern(regEx);
      final Matcher m = pattern.getMatcher(a.message);
      if (m.find()) {
        pattern.hits++;
        a.agent = LibRegEx.getField(m, "agent");
        a.host = LibRegEx.getField(m, "host");
        a.message = m.replaceAll(replace);
        break;
      }
    }
  }

  public static JsonObject toDynaTraceJson(final Alert alert) {
    final JsonObject data = new JsonObject();
    data.addProperty("systemprofile", alert.system);
    data.addProperty("severity", AppMonProcessor.severity2dyna.get(alert.severity));
    data.addProperty("state", AppMonProcessor.state2dyna.get(alert.state));
    if (alert.start != null) {
      data.addProperty("start", AppMonProcessor.ISO8601.format(alert.start));
    }
    if (alert.end != null) {
      data.addProperty("end", AppMonProcessor.ISO8601.format(alert.end));
    }
    if (alert.rule != null) {
      data.addProperty("rule", alert.rule);
    }
    if (alert.message != null) {
      data.addProperty("message", alert.message);
    }
    if (alert.description != null) {
      data.addProperty("description", alert.description);
    }
    return data;
  }

  public String getDashboard(final AppMonServer server, final String dashboard, final String type) {
    final String result = server.getDashboard(dashboard, type);
    return result;
  }

  public Collection<ServiceStatus> getServices() {
    return services.values();
  }

  public static String severityToString(final AlertSeverity severity) {
    return (severity != null) ? AppMonProcessor.severity2dyna.get(severity) : null;
  }

  public static AlertSeverity stringToSeverity(final String severity) {
    return (severity != null) ? AppMonProcessor.dyna2severity.get(severity) : null;
  }

  public AlertSeverity findSeverityFix(final String message) {
    AlertSeverity sev = null;
    for (final String[] def : AppMonConfig.fix_severity.getDefinitions().getData()) {
      final String regEx = def[0];
      final String severity = def[1];
      final RegExRule pattern = AppMonConfig.fix_severity.getPattern(regEx);
      final Matcher m = pattern.getMatcher(message);
      if (m.find()) {
        pattern.hits++;
        sev = AppMonProcessor.stringToSeverity(severity);
        if (sev != null) {
          break;
        }
      }
    }
    return sev;
  }

  public void preProcess(final List<Alert> alerts) {
    for (final Alert alert : alerts) {
      if (check(alert, alert.message)) {
        continue;
      }
      if (check(alert, alert.rule)) {
        continue;
      }
    }
  }

  private boolean check(final Alert alert, final String text) {
    if (text != null) {
      final AlertSeverity sev = findSeverityFix(text);
      if (sev != null) {
        alert.severity = sev;
        return true;
      }
    }
    return false;
  }

}
