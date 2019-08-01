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
package net.eiroca.dynatrace.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.diagnostics.actions.LocalCommandAction;
import net.eiroca.library.diagnostics.actions.SSHCommandAction;
import net.eiroca.library.diagnostics.actions.WebServiceAction;
import net.eiroca.library.diagnostics.monitors.ApacheServerMonitor;
import net.eiroca.library.diagnostics.monitors.DataPowerMonitor;
import net.eiroca.library.diagnostics.monitors.DatabaseMonitor;
import net.eiroca.library.diagnostics.monitors.ElasticSearchMonitor;
import net.eiroca.library.diagnostics.monitors.FLUMEServerMonitor;
import net.eiroca.library.diagnostics.monitors.GraphiteMonitor;
import net.eiroca.library.diagnostics.monitors.OracleMonitor;
import net.eiroca.library.diagnostics.monitors.PostgreSQLMonitor;
import net.eiroca.library.diagnostics.monitors.RedisMonitor;
import net.eiroca.library.diagnostics.monitors.TCPServerMonitor;
import net.eiroca.library.diagnostics.monitors.WebServerMonitor;
import net.eiroca.library.diagnostics.monitors.eSysAdmServerMonitor;
import net.eiroca.library.dynatrace.plugin.appmon.AppMonContext;
import net.eiroca.library.dynatrace.plugin.appmon.AppMonPlugin;
import net.eiroca.library.dynatrace.plugin.appmon.AppMonPluginMonitor;
import net.eiroca.library.metrics.MetricGroup;

public class AppMonAdvancedMonitor extends AppMonPluginMonitor {

  static {
    AppMonPluginMonitor.name = "Advanced Server Monitor";
  }

  private static final String CONFIG_MONITORTYPE = "monitorType";

  private static Map<String, Class<?>> monitors = new HashMap<>();
  static {
    AppMonAdvancedMonitor.monitors.put("Apache Web Server", ApacheServerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("Database Server", DatabaseMonitor.class);
    AppMonAdvancedMonitor.monitors.put("DataPower Server", DataPowerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("ElasticSearch Server", ElasticSearchMonitor.class);
    AppMonAdvancedMonitor.monitors.put("eSysAdm Server", eSysAdmServerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("FLUME Server", FLUMEServerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("Graphite Server", GraphiteMonitor.class);
    AppMonAdvancedMonitor.monitors.put("Local command", LocalCommandAction.class);
    AppMonAdvancedMonitor.monitors.put("Oracle Database Server", OracleMonitor.class);
    AppMonAdvancedMonitor.monitors.put("PostgreSQL Server", PostgreSQLMonitor.class);
    AppMonAdvancedMonitor.monitors.put("Redis Server", RedisMonitor.class);
    AppMonAdvancedMonitor.monitors.put("SSH command", SSHCommandAction.class);
    AppMonAdvancedMonitor.monitors.put("TCP Server", TCPServerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("Web Server", WebServerMonitor.class);
    AppMonAdvancedMonitor.monitors.put("WebService", WebServiceAction.class);
  }

  @Override
  public Status monitor(final AppMonContext<MonitorEnvironment> context, final String host) throws Exception {
    Status status = new Status(Status.StatusCode.Success);
    final String monitorType = context.getConfigString(AppMonAdvancedMonitor.CONFIG_MONITORTYPE);
    final Class<?> monitorClass = AppMonAdvancedMonitor.monitors.get(monitorType);
    if (monitorClass == null) {
      status = new Status(Status.StatusCode.ErrorInternalConfigurationProblem, "Unknown type: " + monitorType);
    }
    else {
      final IServerMonitor monitor = (IServerMonitor)monitorClass.newInstance();
      try {
        final List<MetricGroup> groups = new ArrayList<>();
        monitor.setup(context);
        monitor.check(host);
        monitor.loadMetricGroup(groups);
        AppMonPlugin.publishMeasures(context, groups);
      }
      catch (final CommandException err) {
        status = fromException(err);
      }
      finally {
        Helper.close(monitor);
      }
    }
    context.info(context.getRunner(), " end: ", status.getShortMessage());
    return status;
  }

}
