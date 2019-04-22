/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
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
package net.eiroca.library.dynatrace.tools;

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
import org.slf4j.Logger;
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
import net.eiroca.library.core.LibStr;
import net.eiroca.library.regex.LibRegEx;
import net.eiroca.library.rule.RegExRule;
import net.eiroca.library.rule.RuleManager;
import net.eiroca.library.rule.context.LookupRuleGroup;
import net.eiroca.library.rule.context.RegExRuleGroup;
import net.eiroca.library.system.Logs;

public class DynatraceProcessor {

  protected static Logger logger = Logs.getLogger();

  private static SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  public final static String CONF_PATH = ".\\conf\\";
  public final static String INPUT_PATH = ".\\input\\";
  public final static String OUTPUT_PATH = ".\\output\\";

  private static final String OUTPUT_STATS_LOOKUP = DynatraceProcessor.OUTPUT_PATH + "stats-lookup.csv";
  private static final String OUTPUT_STATS_MISSING_KEY = DynatraceProcessor.OUTPUT_PATH + "missing-key.csv";
  public static final String OUTPUT_STATS_REPLACEMENT = DynatraceProcessor.OUTPUT_PATH + "stats-replacement.csv";
  public static final String OUTPUT_STATS_RULES = DynatraceProcessor.OUTPUT_PATH + "stats-rules.csv";

  public static final String FLD_SERVICE = "service";
  public static final String DEFAULT_SERVICE = "";

  public final RuleManager lookupManager = new RuleManager("lookup");
  public LookupRuleGroup system2service = null;

  public final RuleManager replaceManager = new RuleManager("replace");
  public RegExRuleGroup replace_DTdescription = null;
  public RegExRuleGroup replace_DT_message = null;

  public final RuleManager ruleManager = new RuleManager("rule");
  public RegExRuleGroup agentsRule = null;

  private static final int ONE_HOUR = 3600 * 1000;

  private static final int ONE_DAY = 24 * 60 * 60 * 1000;

  private static final String NODE_AGENT_INSTANCE_NAME = "agentInstanceName";
  private static final String NODE_AGENTINFORMATION = "agentinformation";

  private static final String MSG_AGENT_CONNECTION_LOST = "Agent connection lost";
  private static final String MSG_HOST_DISK_UNHEALTHY = "Host Disk Unhealthy";
  private static final String MSG_RESPONSE_TIME_DEGRADED_FOR_SLOW_REQUESTS = "Response time degraded for slow requests";

  private static final String FMT_AGENT_STATUS = "Dynatrace agents status alert";
  private static final String FMT_STATUS_ALERT = "%s status alert";

  protected static final DateFormat HHMM = new SimpleDateFormat("HH:mm");
  protected static final Date START = new Date(0);
  protected static final Date END = new Date(DynatraceProcessor.ONE_DAY + 1);

  private final Map<String, ServiceStatus> services = new HashMap<>();

  private Alert agentAlert = null;

  private final String defProfile;
  private final String defRule;

  private final static Map<String, AlertSeverity> dyna2severity = new HashMap<>();
  private final static Map<AlertSeverity, String> severity2dyna = new HashMap<>();
  private final static Map<String, AlertState> dyna2state = new HashMap<>();
  private final static Map<AlertState, String> state2dyna = new HashMap<>();

  static {
    DynatraceProcessor.dyna2severity.put("informational", AlertSeverity.INFO);
    DynatraceProcessor.dyna2severity.put("warning", AlertSeverity.WARN);
    DynatraceProcessor.dyna2severity.put("severe", AlertSeverity.SEVERE);
    DynatraceProcessor.severity2dyna.put(AlertSeverity.INFO, "informational");
    DynatraceProcessor.severity2dyna.put(AlertSeverity.WARN, "warning");
    DynatraceProcessor.severity2dyna.put(AlertSeverity.SEVERE, "severe");
    DynatraceProcessor.severity2dyna.put(AlertSeverity.CRITICAL, "severe");
    //
    DynatraceProcessor.dyna2state.put("InProgress", AlertState.INPROGRESS);
    DynatraceProcessor.dyna2state.put("Confirmed", AlertState.CONFIRMED);
    DynatraceProcessor.dyna2state.put("Created", AlertState.NEW);
    //
    DynatraceProcessor.state2dyna.put(AlertState.INPROGRESS, "InProgress");
    DynatraceProcessor.state2dyna.put(AlertState.CONFIRMED, "Confirmed");
    DynatraceProcessor.state2dyna.put(AlertState.NEW, "Created");
    DynatraceProcessor.state2dyna.put(AlertState.CLOSED, "Created");
  }

  public DynatraceProcessor(final String defProfile, final String defRule) {
    this.defProfile = defProfile;
    this.defRule = defRule;
  }

  public void readAlerts(final DynatraceServer[] servers, final Date startDate, final Date endDate, final List<Alert> alerts) {
    alerts.clear();
    for (final DynatraceServer server : servers) {
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
      if (preProcess(alert)) {
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
      DynatraceProcessor.logger.info("STATUS=" + s);
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
    final Map<String, String> def = system2service.lookup(serviceName);
    if (def == null) {
      serviceName = null;
    }
    else {
      serviceName = def.get(DynatraceProcessor.FLD_SERVICE);
    }
    if (serviceName == null) {
      serviceName = DynatraceProcessor.DEFAULT_SERVICE;
    }
    return LibStr.isNotEmptyOrNull(serviceName) ? getServiceStatusByName(serviceName) : null;
  }

  private boolean preProcess(final Alert alert) {
    if (alert == null) { return false; }
    if (alert.state == AlertState.CLOSED) { return false; }
    if (alert.state != AlertState.NEW) { return false; }
    if (LibStr.is(alert.rule, DynatraceProcessor.MSG_RESPONSE_TIME_DEGRADED_FOR_SLOW_REQUESTS)) {
      alert.severity = AlertSeverity.WARN;
    }
    else if (LibStr.is(alert.rule, DynatraceProcessor.MSG_HOST_DISK_UNHEALTHY)) {
      alert.severity = AlertSeverity.WARN;
    }
    if (LibStr.is(alert.message, DynatraceProcessor.MSG_AGENT_CONNECTION_LOST)) {
      alert.severity = AlertSeverity.INFO;
    }
    if (alert.state == AlertState.INPROGRESS) {
      alert.severity = AlertSeverity.WARN;
    }
    return true;
  }

  public void resetServices() {
    for (final ServiceStatus status : services.values()) {
      status.reset();
    }
  }

  public void raiseAlerts(final DynatraceServer server) {
    for (final ServiceStatus serviceStatus : services.values()) {
      boolean isNew = false;
      if (serviceStatus.state != ServiceState.OK) {
        if (serviceStatus.alert == null) {
          serviceStatus.alert = newAlert();
          isNew = true;
        }
        Alert alert = serviceStatus.alert;
        AlertSeverity sev;
        switch (serviceStatus.state) {
          case ERROR:
            sev = AlertSeverity.SEVERE;
            break;
          case FAILED:
            sev = AlertSeverity.CRITICAL;
            break;
          case WARNING:
            sev = AlertSeverity.WARN;
            break;
          default:
            sev = AlertSeverity.INFO;
            break;
        }
        if (!isNew && (alert.severity != sev)) {
          // Cambio severity, chiude vecchio e apre nuovo
          server.closeAlert(alert);
          alert = newAlert();
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
        alert.message = String.format(DynatraceProcessor.FMT_STATUS_ALERT, serviceStatus.name);
        if (isNew) {
          server.createAlert(alert);
          DynatraceProcessor.logger.info("NEW_ALERT=" + alert);
        }
      }
      else {
        if (serviceStatus.alert != null) {
          DynatraceProcessor.logger.info("Closing " + serviceStatus.alert.id);
          server.closeAlert(serviceStatus.alert);
          serviceStatus.alert = null;
        }
      }
    }
  }

  public Alert newAlert() {
    final Alert alert = new Alert();
    alert.system = defProfile;
    alert.rule = defRule;
    alert.start = new Date();
    alert.end = null;
    return alert;
  }

  public void importAlerts(final DynatraceServer server, final List<Alert> alerts, final String json) {
    if (json == null) { return; }
    final JsonParser parser = new JsonParser();
    final JsonObject data = parser.parse(json).getAsJsonObject();
    final JsonArray records = data.getAsJsonArray("alerts");
    for (int i = 0; i < records.size(); i++) {
      final JsonObject alert = records.get(i).getAsJsonObject();
      final String id = GsonUtil.getString(alert, "id");
      final String alertJson = server.readAlert(id);
      final Alert dynaAlert = fromDynaTraceJson(id, alertJson);
      alerts.add(dynaAlert);
    }
  }

  public void closePending(final DynatraceServer server) {
    final String alertsJSon = server.readAlertsList(defProfile, defRule, null, new Date(), null);
    final List<Alert> alerts = new ArrayList<>();
    importAlerts(server, alerts, alertsJSon);
    for (final Alert a : alerts) {
      server.closeAlert(a);
    }
  }

  public boolean readAgents(final DynatraceServer[] servers, final List<Agent> agents) {
    boolean result = true;
    agents.clear();
    for (final DynatraceServer server : servers) {
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
      final NodeList nList = doc.getElementsByTagName(DynatraceProcessor.NODE_AGENTINFORMATION);
      for (int temp = 0; temp < nList.getLength(); temp++) {
        final Node nNode = nList.item(temp);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
          final Element eElement = (Element)nNode;
          final Agent agent = new Agent();
          agent.instanceName = eElement.getElementsByTagName(DynatraceProcessor.NODE_AGENT_INSTANCE_NAME).item(0).getTextContent();
          agents.add(agent);
        }
      }
    }
    catch (final Exception e) {
      DynatraceProcessor.logger.error("Error parsing agents", e);
      return false;
    }
    return true;
  }

  public void validateAgents(final DynatraceServer server, final List<Agent> agents) {
    final List<String> violations = new ArrayList<>();
    Collections.sort(agents);
    int minSeverity = Integer.MAX_VALUE;
    for (final String[] def : agentsRule.getDefinitions().getData()) {
      int fld = 0;
      final String regEx = def[fld++];
      final int minCount = Helper.getInt(def[fld++], 0);
      final int maxCount = Helper.getInt(def[fld++], Integer.MAX_VALUE);
      final Date minH = Helper.getDate(def[fld++], DynatraceProcessor.HHMM, DynatraceProcessor.START);
      final Date maxH = Helper.getDate(def[fld++], DynatraceProcessor.HHMM, DynatraceProcessor.END);
      final long curH = System.currentTimeMillis() % DynatraceProcessor.ONE_DAY;
      if ((curH < minH.getTime()) || (curH > maxH.getTime())) {
        continue;
      }
      final String message = def[fld++];
      final int severity = Helper.getInt(def[fld++], Integer.MAX_VALUE);
      final RegExRule pattern = agentsRule.getPattern(regEx);
      int count = 0;
      for (final Agent a : agents) {
        final Matcher m = pattern.getMatcher(a.instanceName);
        if (m.find()) {
          count++;
        }
      }
      if ((count < minCount) || (count > maxCount)) {
        DynatraceProcessor.logger.warn("Violation: " + message + " " + minCount + "<=" + count + "<=" + maxCount + " H " + (minH.getTime() / DynatraceProcessor.ONE_HOUR) + "<" + (curH / DynatraceProcessor.ONE_HOUR) + "<" + (maxH.getTime() / DynatraceProcessor.ONE_HOUR));
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
        agentAlert = newAlert();
        agentAlert.severity = (minSeverity == 1) ? AlertSeverity.SEVERE : AlertSeverity.WARN;
        agentAlert.message = String.format(DynatraceProcessor.FMT_AGENT_STATUS, agentAlert.severity);
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

  public void exportStats() {
    lookupManager.saveStats(DynatraceProcessor.OUTPUT_STATS_LOOKUP);
    lookupManager.saveMissingKey(DynatraceProcessor.OUTPUT_STATS_MISSING_KEY);
    replaceManager.saveStats(DynatraceProcessor.OUTPUT_STATS_REPLACEMENT);
    ruleManager.saveStats(DynatraceProcessor.OUTPUT_STATS_RULES);
  }

  public void loadConf() {
    system2service = lookupManager.addLookupRule("system2service", DynatraceProcessor.CONF_PATH + "system2service.csv");
    replace_DTdescription = replaceManager.addRegExRules("DT_description", DynatraceProcessor.CONF_PATH + "DT_description.csv");
    replace_DT_message = replaceManager.addRegExRules("DT_message", DynatraceProcessor.CONF_PATH + "DT_message.csv");
    agentsRule = replaceManager.addRegExRules("AgentRules", DynatraceProcessor.CONF_PATH + "agentsRules.csv");
  }

  public Alert fromDynaTraceJson(final String id, final String json) {
    final Alert a = new Alert(id);
    final JsonObject data = a.parser.parse(json).getAsJsonObject();
    a.system = GsonUtil.getString(data, "systemprofile");
    a.rule = GsonUtil.getString(data, "rule");
    a.message = GsonUtil.getString(data, "message");
    a.description = GsonUtil.getString(data, "description");
    a.start = GsonUtil.getDate(data, "start", DynatraceProcessor.ISO8601);
    a.end = GsonUtil.getDate(data, "end", DynatraceProcessor.ISO8601);
    final String stateStr = GsonUtil.getString(data, "state");
    a.state = DynatraceProcessor.dyna2state.get(stateStr);
    if (a.state == null) {
      a.state = AlertState.NEW;
    }
    if ((a.state == AlertState.NEW) && (a.end != null)) {
      a.state = AlertState.CLOSED;
    }
    final String severityStr = GsonUtil.getString(data, "severity");
    a.severity = DynatraceProcessor.dyna2severity.get(severityStr);
    if (a.severity == null) {
      a.severity = AlertSeverity.INFO;
    }
    if (a.message != null) {
      for (final String[] def : replace_DT_message.getDefinitions().getData()) {
        final String regEx = def[0];
        final String replace = def[1];
        final RegExRule pattern = replace_DT_message.getPattern(regEx);
        final Matcher m = pattern.getMatcher(a.message);
        if (m.find()) {
          pattern.hits++;
          a.agent = LibRegEx.getField(m, "agent");
          a.host = LibRegEx.getField(m, "host");
          a.message = m.replaceAll(replace);
        }
        break;
      }
    }
    if (a.description != null) {
      for (final String[] def : replace_DTdescription.getDefinitions().getData()) {
        final String regEx = def[0];
        final String replace = def[1];
        final RegExRule pattern = replace_DTdescription.getPattern(regEx);
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

  public static JsonObject toDynaTraceJson(final Alert alert) {
    final JsonObject data = new JsonObject();
    data.addProperty("systemprofile", alert.system);
    data.addProperty("severity", DynatraceProcessor.severity2dyna.get(alert.severity));
    data.addProperty("state", DynatraceProcessor.state2dyna.get(alert.state));
    if (alert.start != null) {
      data.addProperty("start", DynatraceProcessor.ISO8601.format(alert.start));
    }
    if (alert.end != null) {
      data.addProperty("end", DynatraceProcessor.ISO8601.format(alert.end));
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

  public String getDashboard(final DynatraceServer server, final String dashboard, final String type) {
    final String result = server.getDashboard(dashboard, type);
    return result;
  }

  public Collection<ServiceStatus> getServices() {
    return services.values();
  }

}
