/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.dynatrace.diagnostics.pdk.AgentGroupSource;
import com.dynatrace.diagnostics.pdk.AgentSource;
import com.dynatrace.diagnostics.pdk.CollectorSource;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Measure;
import com.dynatrace.diagnostics.pdk.Metric;
import com.dynatrace.diagnostics.pdk.MonitorSource;
import com.dynatrace.diagnostics.pdk.ServerSource;
import com.dynatrace.diagnostics.pdk.Source;
import com.dynatrace.diagnostics.pdk.SourceType;
import com.dynatrace.diagnostics.pdk.Violation;
import com.dynatrace.diagnostics.pdk.Violation.TriggerValue;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;
import net.eiroca.library.core.LibStr;

public class Sources {

  private static final String EMPTY_STRING = "";

  private final static String REGEX_SPLIT_ON_WORDS = "[,\\(\\)\\{\\}\\'\\\"]";
  private final static String REGEX_SPLIT_AT_SIGN = "[@]";
  private final static String REGEX_REMOVE_CHARS = "[\\(\\)\\{\\}\\\"]";

  public Map<SourceType, SourceReferences> sources = new HashMap<>();

  public Sources(final List<Incident> incidents) {
    if ((incidents != null) && !incidents.isEmpty()) {
      Sources.parseIncidents(sources, incidents);
    }
  }

  private static void parseIncidents(final Map<SourceType, SourceReferences> sources, final List<Incident> incidents) {
    for (final Incident incident : incidents) {
      // parse message field to extract source-type, source-name, and host-name of the incident only
      // if there are 0
      // violations
      final Collection<Violation> violations = incident.getViolations();
      if (violations == null) {
        continue;
      }
      if (violations.isEmpty()) {
        final String message = incident.getMessage();
        Sources.parseMessage(sources, message);
      }
      else {
        for (final Violation violation : violations) {
          if (violation == null) {
            continue;
          }
          Sources.parseViolation(sources, violation);
          // add sources for each trigger value
          final Collection<TriggerValue> triggerValues = violation.getTriggerValues();
          if ((triggerValues != null) && !triggerValues.isEmpty()) {
            for (final TriggerValue triggerValue : triggerValues) {
              final Source source = triggerValue.getSource();
              if (source == null) {
                continue;
              }
              Sources.updateSourceReferences(sources, source);
            }
          }
        }
      }
    }
  }

  private static void parseMessage(final Map<SourceType, SourceReferences> sources, final String message) {
    if (LibStr.isEmptyOrNull(message)) { return; }
    if (message.indexOf("@") != -1) {
      Sources.parseMessageAT(sources, message);
    }
    else if (message.indexOf("'") != -1) {
      Sources.parseMessageSingleQuote(sources, message);
    }
  }

  public static void parseMessageAT(final Map<SourceType, SourceReferences> sources, final String message) {
    String name = null;
    String host = null;
    final Scanner sc = new Scanner(message);
    sc.useDelimiter(Sources.REGEX_SPLIT_ON_WORDS);
    String word;
    Scanner scanner;
    while (sc.hasNext()) {
      word = sc.next().trim();
      if (word.indexOf('@') != -1) {
        // extract agent name and agent host
        scanner = new Scanner(word);
        scanner.useDelimiter(Sources.REGEX_SPLIT_AT_SIGN);
        // first word should be agent name
        if (scanner.hasNext()) {
          name = scanner.next().trim().replaceAll(Sources.REGEX_REMOVE_CHARS, "");
        }
        // second word should be agent host
        if (scanner.hasNext()) {
          host = scanner.next().trim().replaceAll(Sources.REGEX_REMOVE_CHARS, "");
          // remove port if there is a colon character in the host
          if (host.indexOf(':') != -1) {
            host = host.split(":")[0].trim();
          }
        }
        scanner.close();
        // populate source name and source host
        if (LibStr.isNotEmptyOrNull(name) || LibStr.isNotEmptyOrNull(host)) {
          try {
            Sources.addSource(SourceType.Agent, sources, name, host);
          }
          catch (final RuntimeException e) {
            sc.close();
            throw e;
          }
        }
      }
    }
    sc.close();
  }

  public static void parseMessageSingleQuote(final Map<SourceType, SourceReferences> sources, final String message) {
    String hosts;
    String host;
    // split message using single quote
    final String[] sa = message.split(BaseConstants.SQUOTE);
    if (sa.length == 3) {
      hosts = sa[1];
      // extract host names from the hosts
      final Scanner sc = new Scanner(hosts);
      while (sc.hasNext()) {
        host = sc.next().trim().replaceAll(Sources.REGEX_REMOVE_CHARS, "");
        // remove port if there is a colon character in the host
        if (host.indexOf(':') != -1) {
          host = host.split(":")[0].trim();
        }
        // populate host if it is not empty using incident source server
        if ((host != null) && !host.isEmpty()) {
          // server name is added to the server hosts for servers
          try {
            Sources.addSource(SourceType.Server, sources, Sources.EMPTY_STRING, host);
          }
          catch (final RuntimeException e) {
            sc.close();
            throw e;
          }
        }
      }
      sc.close();
    }
  }

  private static void parseViolation(final Map<SourceType, SourceReferences> sources, final Violation violation) {
    final Measure measure = violation.getViolatedMeasure();
    // add source of the violated measure
    if (measure == null) { return; }
    Source source = measure.getSource();
    if (source == null) { return; }
    // process all sources except Monitor
    if (!source.getSourceType().equals(SourceType.Monitor)) {
      Sources.updateSourceReferences(sources, source);
    }
    else {
      // For monitors add hosts embedded into the violated measures
      final Metric metric = measure.getMetric();
      final String violatedMeasureName = measure.getName();
      final String metricName = metric.getName();
      MonitorSource monitor;
      source = measure.getSource();
      if ((metric != null) && LibStr.isNotEmptyOrNull(violatedMeasureName) && LibStr.isNotEmptyOrNull(metricName)) {
        monitor = (MonitorSource)source;
        if (!violatedMeasureName.equals(metricName)) {
          if ((violatedMeasureName.length() > metricName.length()) && violatedMeasureName.startsWith(metricName)) {
            String location = violatedMeasureName.substring(metricName.length()).trim();
            int i;
            if ((location.length() > 2) && ((location.indexOf('@') != -1) && (location.indexOf('(') != -1) && (location.indexOf(')') != -1))) {
              // remove '@' and parenthesis
              location = new StringBuilder(location.substring(2, location.length() - 1)).toString();
              // get location
              i = location.indexOf('@');
              if ((i != -1) && (location.length() > 1)) {
                location = location.substring(i + 1);
              }
              // remove port if there is a colon character in the host
              if (location.indexOf(':') != -1) {
                location = location.split(":")[0].trim();
              }
              // check if this is a new host
              if (LibStr.isNotEmptyOrNull(location)) {
                SourceReferences sr = sources.get(source.getSourceType());
                if (sr == null) {
                  sr = new SourceReferences();
                  sources.put(source.getSourceType(), sr);
                }
                final List<String> hosts = sr.getSourceHosts();
                if (!hosts.contains(location)) {
                  hosts.add(location);
                  // add monitor-name and monitor location (i.e. monitor host) to the all variables
                  Sources.addSource(source.getSourceType(), sources, monitor.getName(), location);
                }
              }
            }
          }
        }
      }
    }
  }

  public static void updateSourceReferences(final Map<SourceType, SourceReferences> sources, final Source source) {
    switch (source.getSourceType()) {
      case Agent:
        final AgentSource agent = (AgentSource)source;
        Sources.addSource(SourceType.Agent, sources, agent.getName(), agent.getHost());
        break;
      case AgentGroup:
        final AgentGroupSource agentGroup = (AgentGroupSource)source;
        Sources.addSource(SourceType.AgentGroup, sources, agentGroup.getAgentGroupNames());
        break;
      case Monitor:
        final MonitorSource monitor = (MonitorSource)source;
        Sources.addSource(SourceType.Monitor, sources, monitor.getName(), Sources.EMPTY_STRING);
        break;
      case Collector:
        final CollectorSource collector = (CollectorSource)source;
        Sources.addSource(SourceType.Collector, sources, collector.getName(), collector.getHost());
        break;
      case Server:
        final ServerSource server = (ServerSource)source;
        Sources.addSource(SourceType.Server, sources, Sources.EMPTY_STRING, server.getName());
        break;
      default:
        break;
    }
  }

  public static void addSource(final SourceType type, final Map<SourceType, SourceReferences> map, final String name, final String host) {
    SourceReferences sourceReferences = map.get(type);
    if (sourceReferences == null) {
      sourceReferences = new SourceReferences();
      map.put(type, sourceReferences);
    }
    sourceReferences.add(name, host);
    // add host entry to the SourceReference.allHosts field of the SourceType.Server type
    if (LibStr.isNotEmptyOrNull(host) && !type.name().equals(SourceType.Server.name())) {
      sourceReferences = map.get(SourceType.Server);
      if (sourceReferences == null) {
        sourceReferences = new SourceReferences();
        map.put(SourceType.Server, sourceReferences);
      }
      sourceReferences.add(name, host);
    }
  }

  public static void addSource(final SourceType type, final Map<SourceType, SourceReferences> map, final Collection<String> names) {
    SourceReferences sourceReferences = map.get(type);
    if (sourceReferences == null) {
      sourceReferences = new SourceReferences();
      map.put(type, sourceReferences);
    }
    if ((names != null) && !names.isEmpty()) {
      for (final String name : names) {
        if ((name != null) && !name.isEmpty() && !name.equals(BaseConstants.AT)) {
          sourceReferences.add(name, null);
        }
      }
    }
  }

}
