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
package net.eiroca.library.dynatrace.plugin.appmon;

import java.util.ArrayList;
import java.util.List;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.metrics.MetricGroup;

public abstract class AppMonPluginMonitor extends AppMonPlugin implements Monitor {

  protected static String name = "Dynatrace Monitor Plugin";
  protected static Class<?> monitorClass = null;

  @Override
  public Status setup(final MonitorEnvironment env) throws Exception {
    final AppMonContext<MonitorEnvironment> context = new AppMonContext<>(AppMonPluginMonitor.name, env);
    final Status status = new Status(Status.StatusCode.Success);
    context.info(AppMonPluginMonitor.name, " setup: ", status.getShortMessage());
    return status;
  }

  @Override
  public void teardown(final MonitorEnvironment env) throws Exception {
    final AppMonContext<MonitorEnvironment> context = new AppMonContext<>(AppMonPluginMonitor.name, env);
    final Status status = new Status(Status.StatusCode.Success);
    context.info(AppMonPluginMonitor.name, " teardown: ", status.getShortMessage());
  }

  @Override
  public Status execute(final MonitorEnvironment env) throws Exception {
    final AppMonContext<MonitorEnvironment> context = new AppMonContext<>(AppMonPluginMonitor.name, env);
    final String host = env.getHost().getAddress();
    final Status status = monitor(context, host);
    context.info(AppMonPluginMonitor.name, " execute: ", status);
    return status;
  }

  public Status monitor(final AppMonContext<MonitorEnvironment> context, final String host) throws Exception {
    Status status = new Status(Status.StatusCode.Success);
    if (AppMonPluginMonitor.monitorClass != null) {
      final IServerMonitor monitor = (IServerMonitor)AppMonPluginMonitor.monitorClass.newInstance();
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
      context.info(context.getRunner(), " end: ", status.getShortMessage());
    }
    return status;
  }

}
