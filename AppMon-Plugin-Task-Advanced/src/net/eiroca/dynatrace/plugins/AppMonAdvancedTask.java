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
package net.eiroca.dynatrace.plugins;

import java.util.HashMap;
import java.util.Map;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.TaskEnvironment;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.ITask;
import net.eiroca.library.diagnostics.actions.LocalCommandAction;
import net.eiroca.library.diagnostics.actions.SSHCommandAction;
import net.eiroca.library.diagnostics.actions.WebServiceAction;
import net.eiroca.library.dynatrace.sdk.AbstractTaskPlugin;
import net.eiroca.library.dynatrace.sdk.DynatraceContext;

public class AppMonAdvancedTask extends AbstractTaskPlugin {

  static {
    AbstractTaskPlugin.name = "Advanced Task";
  }

  private static final String CONFIG_TASKTYPE = "taskType";

  private static Map<String, Class<?>> tasks = new HashMap<>();
  static {
    AppMonAdvancedTask.tasks.put("WebService", WebServiceAction.class);
    AppMonAdvancedTask.tasks.put("SSH command", SSHCommandAction.class);
    AppMonAdvancedTask.tasks.put("Local command", LocalCommandAction.class);
  }

  @Override
  public Status execute(final DynatraceContext<TaskEnvironment> context) throws Exception {
    context.debug(context.getRunner(), " executing action");
    Status status = new Status(Status.StatusCode.Success);
    final String taskType = context.getConfigString(AppMonAdvancedTask.CONFIG_TASKTYPE);
    final Class<?> taskClass = AppMonAdvancedTask.tasks.get(taskType);
    if (taskClass == null) {
      status = new Status(Status.StatusCode.ErrorInternalConfigurationProblem, "Unknown type: " + taskType);
    }
    else {
      final ITask task = (ITask)taskClass.newInstance();
      try {
        task.setup(context);
        task.execute();
      }
      catch (final CommandException err) {
        status = fromException(err);
      }
      finally {
        Helper.close(task);
      }
    }
    return status;
  }

}
