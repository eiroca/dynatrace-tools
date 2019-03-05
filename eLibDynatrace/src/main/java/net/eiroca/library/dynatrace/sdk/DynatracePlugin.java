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
package net.eiroca.library.dynatrace.sdk;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;

public class DynatracePlugin {

  final public static void publishMeasures(final DynatraceContext<MonitorEnvironment> context, final List<MetricGroup> groups) {
    if (groups == null) { return; }
    for (final MetricGroup g : groups) {
      context.debug("processing group: ", g.getName());
      g.refresh();
      for (final Measure m : g.getMetrics()) {
        DynatracePlugin.exportMeasure(context, g, m);
      }
    }
  }

  final public static void exportMeasure(final DynatraceContext<MonitorEnvironment> context, final MetricGroup g, final Measure m) {
    final String group = g.getName();
    final String key = m.getName();
    final double value = m.getValue();
    context.debug("processing metric: ", key);
    final Collection<MonitorMeasure> measures = context.env.getMonitorMeasures(group, key);
    if (measures != null) {
      for (final MonitorMeasure measure : measures) {
        if (m.hasValue()) {
          measure.setValue(value);
          context.trace(group, ".", key, "=", value);
        }
        if (m.hasSplittings()) {
          context.debug("processing metric splitting");
          double sum = 0;
          for (final Entry<String, Measure> s : m.getSplittings().entrySet()) {
            final String splitGroup = s.getKey();
            final Measure ms = s.getValue();
            sum = 0;
            for (final Entry<String, Measure> sm : ms.getSplittings().entrySet()) {
              final String splitName = sm.getKey();
              final Measure splitValue = sm.getValue();
              final double val = splitValue.getValue();
              sum = sum + val;
              context.trace(group, ".", key, "(", splitGroup, ",", splitName, ")=", splitValue);
              final MonitorMeasure dynamicMeasureData = context.env.createDynamicMeasure(measure, splitGroup, splitName);
              dynamicMeasureData.setValue(val);
            }
          }
          if (!m.hasValue()) {
            measure.setValue(sum);
            context.trace(group, ".", key, "=", sum);
          }
        }
      }
    }
  }

  protected Status fromException(final CommandException err) {
    StatusCode statusCode;
    switch (err.getErrorCode()) {
      case Infrastructure:
        statusCode = Status.StatusCode.ErrorInfrastructure;
        break;
      case Configuration:
        statusCode = Status.StatusCode.ErrorInternalConfigurationProblem;
        break;
      case Internal:
        statusCode = Status.StatusCode.ErrorInternal;
        break;
      case Invalid:
        statusCode = Status.StatusCode.ErrorTargetService;
        break;
      default:
        statusCode = Status.StatusCode.ErrorInternal;
        break;
    }
    final Status status = new Status(statusCode);
    status.setMessage(Helper.getExceptionAsString(err, false));
    status.setShortMessage("Action exception");
    status.setException(err);
    return status;
  }

}