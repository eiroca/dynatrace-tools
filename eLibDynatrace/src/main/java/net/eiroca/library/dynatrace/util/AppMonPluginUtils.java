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
package net.eiroca.library.dynatrace.util;

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

/** Generate metric part of plugin xml */
public class AppMonPluginUtils {

  static SortedMap<String, SortedMap<String, MetricMetadata>> definition = new TreeMap<>();
  private static final Map<String, String> ALIAS = new HashMap<>();
  static {
    AppMonPluginUtils.ALIAS.put("number", "number");
    AppMonPluginUtils.ALIAS.put("num", "number");
    AppMonPluginUtils.ALIAS.put("unit", "number");
    AppMonPluginUtils.ALIAS.put("boolean", "number");
    AppMonPluginUtils.ALIAS.put("counter", "number");
    AppMonPluginUtils.ALIAS.put("purepath", "number");
    AppMonPluginUtils.ALIAS.put("rate", "number");
    AppMonPluginUtils.ALIAS.put("request", "number");
    AppMonPluginUtils.ALIAS.put("requests", "number");
    AppMonPluginUtils.ALIAS.put("operation", "number");
    AppMonPluginUtils.ALIAS.put("operations", "number");
  }

  public static void addMetadata(final String groupName, final String metricName, final MetricMetadata metadata) {
    SortedMap<String, MetricMetadata> group = AppMonPluginUtils.definition.get(groupName);
    if (group == null) {
      group = new TreeMap<>();
      AppMonPluginUtils.definition.put(groupName, group);
    }
    group.put(metricName, metadata);
  }

  public static void main(final String[] args) {
    final String baseName = "net.eiroca.dynatrace.plugins.AdvancedMonitor";
    for (final String monitorname : ServerMonitors.registry.getNames()) {
      if (monitorname.equals(ServerMonitors.registry.defaultName())) {
        continue;
      }
      AppMonPluginUtils.processMonitor(monitorname);
    }
    final StringBuilder sb = new StringBuilder(1024);
    int num = 0;
    for (final Entry<String, SortedMap<String, MetricMetadata>> groups : AppMonPluginUtils.definition.entrySet()) {
      num++;
      AppMonPluginUtils.exportEntry(sb, groups, baseName, num);
    }
    System.out.println(sb.toString());
  }

  private static void exportEntry(final StringBuilder sb, final Entry<String, SortedMap<String, MetricMetadata>> groups, final String baseName, final int num) {
    final String mgName = groups.getKey();
    final String sectionName = baseName + ".metricgroup_" + num;
    sb.append("<extension point=\"com.dynatrace.diagnostics.pdk.monitormetricgroup\" id=\"" + sectionName + "\" name=\"" + mgName + "\">").append(LibStr.NL);
    sb.append(" <metricgroup monitorid=\"" + baseName + ".monitor\">").append(LibStr.NL);
    for (final Entry<String, MetricMetadata> metrics : groups.getValue().entrySet()) {
      final MetricMetadata meta = metrics.getValue();
      sb.append("  <metric");
      AppMonPluginUtils.addAttribute(sb, "name", meta.getDisplayName(), "?", (Map<String, String>)null);
      AppMonPluginUtils.addAttribute(sb, "description", meta.getDescription(), meta.getInternalName(), null);
      AppMonPluginUtils.addAttribute(sb, "unit", meta.getUnit(), "number", AppMonPluginUtils.ALIAS);
      AppMonPluginUtils.addAttribute(sb, "defaultrate", meta.getRate(), null, AppMonPluginUtils.ALIAS);
      AppMonPluginUtils.addAttribute(sb, "hidedisplayaggregation", meta.getNoagg(), null, AppMonPluginUtils.ALIAS);
      if (meta.getCalcDelta()) {
        AppMonPluginUtils.addAttribute(sb, "calculatedelta", "true", null, null);
      }
      sb.append(" />").append(LibStr.NL);
    }
    sb.append(" </metricgroup>").append(LibStr.NL);
    sb.append("</extension>").append(LibStr.NL);
  }

  private static void processMonitor(final String monitorname) {
    IServerMonitor monitor;
    try {
      monitor = ServerMonitors.build(monitorname);
    }
    catch (final Exception e) {
      e.printStackTrace();
      return;
    }
    final List<IMetric<?>> metrics = new ArrayList<>();
    final List<MetricGroup> groups = new ArrayList<>();
    final MetricGroup monitorMG = monitor.getMetricGroup();
    monitorMG.loadGroups(groups, true);
    for (final MetricGroup mg : groups) {
      final String mgName = mg.getName();
      metrics.clear();
      mg.loadMetrics(metrics, false);
      for (final IMetric<?> m : metrics) {
        final MetricMetadata meta = m.getMetadata();
        final String mName = meta.getDisplayName();
        if (mName.contains("Rows rocessed")) {
          // System.err.println(monitorname);
        }
        AppMonPluginUtils.addMetadata(mgName, mName, meta);
      }
    }
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
