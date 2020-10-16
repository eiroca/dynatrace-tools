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

import java.text.SimpleDateFormat;
import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import net.eiroca.library.core.Helper;

public abstract class AppMonPluginAction extends AppMonPlugin implements Action {

  private static final String CONFIG_DATEFORMAT = "dateFormat";

  protected static String name = "Dynatrace Action Plugin";

  @Override
  public Status setup(final ActionEnvironment env) throws Exception {
    final AppMonContext<ActionEnvironment> context = new AppMonContext<>(AppMonPluginAction.name, env);
    final Status status = new Status(Status.StatusCode.Success);
    context.info(AppMonPluginAction.name, " setup: ", status.getShortMessage());
    return status;
  }

  @Override
  public void teardown(final ActionEnvironment env) throws Exception {
    final AppMonContext<ActionEnvironment> context = new AppMonContext<>(AppMonPluginAction.name, env);
    final Status status = new Status(Status.StatusCode.Success);
    context.info(AppMonPluginAction.name, " teardown: ", status.getShortMessage());
  }

  @Override
  public Status execute(final ActionEnvironment env) throws Exception {
    final AppMonContext<ActionEnvironment> context = new AppMonContext<>(AppMonPluginAction.name, env);
    final SimpleDateFormat dateFormat = new SimpleDateFormat(context.getConfigString(AppMonPluginAction.CONFIG_DATEFORMAT, "yyyyMMddHHmmss"));
    final AppMonActionData data = AppMonActionData.fromEnvironment(env, dateFormat);
    context.info("Action Data", Helper.NL, data.toString());
    final Status status = execute(context, data);
    context.info(AppMonPluginAction.name, " execute: ", status);
    return status;
  }

  abstract public Status execute(final AppMonContext<ActionEnvironment> context, final AppMonActionData data) throws Exception;

}
