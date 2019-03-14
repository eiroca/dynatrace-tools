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
package net.eiroca.library.dynatrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.diagnostics.ServerMonitors;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.MetricMetadata;

public class DynatracePluginUtils {

  static SortedMap<String, SortedMap<String, MetricMetadata>> definition = new TreeMap<>();

  public static void addMetadata(String groupName, String metricName, MetricMetadata metadata) {
    SortedMap<String, MetricMetadata> group = definition.get(groupName);
    if (group == null) {
      group = new TreeMap<String, MetricMetadata>();
      definition.put(groupName, group);
    }
    group.put(metricName, metadata);
  }

  public static void main(final String[] args) {
    String baseName = "net.eiroca.dynatrace.plugins.AdvancedMonitor";
    String dynaMonitorName = baseName + ".monitor";
    for (String monitorname : ServerMonitors.registry.getNames()) {
      if (monitorname.equals(ServerMonitors.registry.defaultName())) continue;
      IServerMonitor monitor;
      try {
        monitor = ServerMonitors.build(monitorname);
      }
      catch (Exception e) {
        e.printStackTrace();
        continue;
      }
      final List<MetricGroup> groups = new ArrayList<>();
      monitor.loadMetricGroup(groups);
      for (MetricGroup mg : groups) {
        String mgName = mg.getName();
        for (IMetric<?> m : mg.getMetrics()) {
          MetricMetadata meta = m.getMetadata();
          String mName = meta.getDisplayName();
          if ("Probe - Result".equals(mName) ) {
            System.err.println(monitorname);
          }
          addMetadata(mgName, mName, meta);
        }
      }
    }
    StringBuilder sb = new StringBuilder(1024);
    int num = 0;
    for (Entry<String, SortedMap<String, MetricMetadata>> groups : definition.entrySet()) {
      num++;
      String mgName = groups.getKey();
      String sectionName = baseName + ".metricgroup_" + num;
      sb.append("<extension point=\"com.dynatrace.diagnostics.pdk.monitormetricgroup\" id=\"" + sectionName + "\" name=\"" + mgName + "\">").append(LibStr.NL);
      sb.append(" <metricgroup monitorid=\"" + dynaMonitorName + "\">").append(LibStr.NL);
      for (Entry<String, MetricMetadata> metrics : groups.getValue().entrySet()) {
        String mName = metrics.getKey();
        MetricMetadata meta = metrics.getValue();
        sb.append("  <metric");
        sb.append(" name=\"" + meta.getDisplayName() + "\"");
        sb.append(" description=\"" + meta.getInternalName() + "\"");
        sb.append(" unit=\"number\"");
        sb.append(" defaultrate=\"purepath\"");
        sb.append(" />").append(LibStr.NL);
      }
      sb.append(" </metricgroup>").append(LibStr.NL);
      sb.append("</extension>").append(LibStr.NL);
    }
    System.out.println(sb.toString());
  }

}
