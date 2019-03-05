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

import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import com.dynatrace.diagnostics.pdk.Task;
import com.dynatrace.diagnostics.pdk.TaskEnvironment;

public abstract class AbstractTaskPlugin extends DynatracePlugin implements Task {

  protected static String name = "Dynatrace Task Plugin";

  @Override
  public Status setup(final TaskEnvironment env) throws Exception {
    final DynatraceContext<TaskEnvironment> context = new DynatraceContext<>(AbstractTaskPlugin.name, env);
    final Status status = new Status(StatusCode.Success);
    context.info(AbstractTaskPlugin.name, " setup: ", status.getShortMessage());
    return status;
  }

  @Override
  public void teardown(final TaskEnvironment env) throws Exception {
    final DynatraceContext<TaskEnvironment> context = new DynatraceContext<>(AbstractTaskPlugin.name, env);
    final Status status = new Status(Status.StatusCode.Success);
    context.info(AbstractTaskPlugin.name, " teardown: ", status.getShortMessage());
  }

  @Override
  public Status execute(final TaskEnvironment env) throws Exception {
    final DynatraceContext<TaskEnvironment> context = new DynatraceContext<>(AbstractTaskPlugin.name, env);
    final Status status = execute(context);
    context.info(AbstractTaskPlugin.name, " execute: ", status);
    return status;
  }

  abstract public Status execute(DynatraceContext<TaskEnvironment> context) throws Exception;

}
