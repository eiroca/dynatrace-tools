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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private static final Map<String, String> ALIAS = new HashMap<>();
  static {
    DynatracePluginUtils.ALIAS.put("number", null);
    DynatracePluginUtils.ALIAS.put("boolean", null);
    DynatracePluginUtils.ALIAS.put("counter", null);
    DynatracePluginUtils.ALIAS.put("counter", null);
    DynatracePluginUtils.ALIAS.put("purepath", null);
    DynatracePluginUtils.ALIAS.put("rate", "number");
    DynatracePluginUtils.ALIAS.put("request", null);
    DynatracePluginUtils.ALIAS.put("requests", null);
    DynatracePluginUtils.ALIAS.put("operation", null);
    DynatracePluginUtils.ALIAS.put("operations", null);
  }

  public static void addMetadata(final String groupName, final String metricName, final MetricMetadata metadata) {
    SortedMap<String, MetricMetadata> group = DynatracePluginUtils.definition.get(groupName);
    if (group == null) {
      group = new TreeMap<>();
      DynatracePluginUtils.definition.put(groupName, group);
    }
    group.put(metricName, metadata);
  }

  public static void main(final String[] args) {
    final String baseName = "net.eiroca.dynatrace.plugins.AdvancedMonitor";
    final String dynaMonitorName = baseName + ".monitor";
    for (final String monitorname : ServerMonitors.registry.getNames()) {
      if (monitorname.equals(ServerMonitors.registry.defaultName())) {
        continue;
      }
      IServerMonitor monitor;
      try {
        monitor = ServerMonitors.build(monitorname);
      }
      catch (final Exception e) {
        e.printStackTrace();
        continue;
      }
      final List<MetricGroup> groups = new ArrayList<>();
      monitor.loadMetricGroup(groups);
      for (final MetricGroup mg : groups) {
        final String mgName = mg.getName();
        for (final IMetric<?> m : mg.getMetrics()) {
          final MetricMetadata meta = m.getMetadata();
          final String mName = meta.getDisplayName();
          if (mName.contains("Rows rocessed")) {
            // System.err.println(monitorname);
          }
          DynatracePluginUtils.addMetadata(mgName, mName, meta);
        }
      }
    }
    final StringBuilder sb = new StringBuilder(1024);
    int num = 0;
    for (final Entry<String, SortedMap<String, MetricMetadata>> groups : DynatracePluginUtils.definition.entrySet()) {
      num++;
      final String mgName = groups.getKey();
      final String sectionName = baseName + ".metricgroup_" + num;
      sb.append("<extension point=\"com.dynatrace.diagnostics.pdk.monitormetricgroup\" id=\"" + sectionName + "\" name=\"" + mgName + "\">").append(LibStr.NL);
      sb.append(" <metricgroup monitorid=\"" + dynaMonitorName + "\">").append(LibStr.NL);
      for (final Entry<String, MetricMetadata> metrics : groups.getValue().entrySet()) {
        final MetricMetadata meta = metrics.getValue();
        sb.append("  <metric");
        DynatracePluginUtils.addAttribute(sb, "name", meta.getDisplayName(), "?", (Map<String, String>)null);
        DynatracePluginUtils.addAttribute(sb, "description", meta.getDescription(), meta.getInternalName(), null);
        DynatracePluginUtils.addAttribute(sb, "unit", meta.getUnit(), null, DynatracePluginUtils.ALIAS);
        DynatracePluginUtils.addAttribute(sb, "defaultrate", meta.getRate(), null, DynatracePluginUtils.ALIAS);
        DynatracePluginUtils.addAttribute(sb, "hidedisplayaggregation", meta.getNoagg(), null, DynatracePluginUtils.ALIAS);
        if (meta.getCalcDelta()) {
          DynatracePluginUtils.addAttribute(sb, "calculatedelta", "true", null, null);
        }
        sb.append(" />").append(LibStr.NL);
      }
      sb.append(" </metricgroup>").append(LibStr.NL);
      sb.append("</extension>").append(LibStr.NL);
    }
    System.out.println(sb.toString());
  }

  private static void addAttribute(final StringBuilder sb, final String name, String val, final String defVal, final Map<String, String> renamed) {
    if (val == null) {
      val = defVal;
    }
    if (val == null) { return; }
    if (renamed != null) {
      val = renamed.containsKey(val) ? renamed.get(val) : val;
    }
    if (val == null) { return; }
    sb.append(' ').append(name).append('=').append('"').append(val).append('"');
  }

}
