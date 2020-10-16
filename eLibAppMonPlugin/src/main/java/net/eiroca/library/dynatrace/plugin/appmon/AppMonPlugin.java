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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.metrics.datum.StatisticDatum;

public class AppMonPlugin {

  final public static void publishMeasures(final AppMonContext<MonitorEnvironment> context, final List<MetricGroup> groups) {
    if (groups == null) { return; }
    final List<IMetric<?>> metricsList = new ArrayList<>();
    for (final MetricGroup g : groups) {
      if (g.metricCount() > 0) {
        context.debug("processing group: ", g.getName());
        metricsList.clear();
        g.refresh(false);
        g.loadMetrics(metricsList, false);
        for (final IMetric<?> m : metricsList) {
          AppMonPlugin.exportMeasure(context, g, m);
        }
      }
    }
  }

  final public static void exportMeasure(final AppMonContext<MonitorEnvironment> context, final MetricGroup g, final IMetric<?> m) {
    final String group = g.getName();
    final String key = m.getMetadata().getDisplayName();
    final IDatum value = m.getDatum();
    context.debug("processing metric: ", group, ".", key, "=" + value);
    final Collection<MonitorMeasure> measures = context.env.getMonitorMeasures(group, key);
    if (measures != null) {
      for (final MonitorMeasure measure : measures) {
        if (value.hasValue()) {
          measure.setValue(value.getValue());
          context.trace(group, ".", key, "=", value);
        }
        if (m.hasSplittings()) {
          context.debug("processing metric splitting");
          final StatisticDatum d = new StatisticDatum();
          for (final Entry<String, ?> s : m.getSplittings().entrySet()) {
            final String splitGroup = s.getKey();
            final IMetric<?> ms = (IMetric<?>)s.getValue();
            d.init(0);
            for (final Entry<String, ?> sm : ms.getSplittings().entrySet()) {
              final String splitName = sm.getKey();
              final IMetric<?> splitValue = (IMetric<?>)sm.getValue();
              final double val = splitValue.getDatum().getValue();
              d.addValue(val);
              context.trace(group, ".", key, "(", splitGroup, ",", splitName, ")=", splitValue);
              final MonitorMeasure dynamicMeasureData = context.env.createDynamicMeasure(measure, splitGroup, splitName);
              dynamicMeasureData.setValue(val);
            }
          }
          if (!value.hasValue()) {
            final double val = d.getValue(m.getMetadata().getAggregation());
            measure.setValue(val);
            context.trace(group, ".", key, "=", val);
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
