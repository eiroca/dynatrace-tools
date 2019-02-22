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
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IAction;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.actions.LocalCommandAction;
import net.eiroca.library.diagnostics.actions.SSHCommandAction;
import net.eiroca.library.diagnostics.actions.WebServiceAction;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.dynatrace.sdk.AbstractActionPlugin;
import net.eiroca.library.dynatrace.sdk.DynatraceActionData;
import net.eiroca.library.dynatrace.sdk.DynatraceContext;

public class AdvancedAction extends AbstractActionPlugin {

  static {
    AbstractActionPlugin.name = "Advanced Action";
  }

  private static final String CONFIG_ACTIONTYPE = "actionType";

  private static Map<String, Class<?>> actions = new HashMap<>();
  static {
    AdvancedAction.actions.put("WebService", WebServiceAction.class);
    AdvancedAction.actions.put("SSH command", SSHCommandAction.class);
    AdvancedAction.actions.put("Local command", LocalCommandAction.class);
  }

  @Override
  public Status execute(final DynatraceContext<ActionEnvironment> context, final DynatraceActionData data) throws Exception {
    context.debug(context.getRunner(), " executing action");
    Status status = new Status(Status.StatusCode.Success);
    final String actionType = context.getConfigString(AdvancedAction.CONFIG_ACTIONTYPE);
    final Class<?> actionClass = AdvancedAction.actions.get(actionType);
    if (actionClass == null) {
      status = new Status(Status.StatusCode.ErrorInternalConfigurationProblem, "Unknown type: " + actionType);
    }
    else {
      @SuppressWarnings("unchecked")
      final IAction<ActionData, ReturnObject> action = (IAction<ActionData, ReturnObject>)actionClass.newInstance();
      try {
        action.setup(context);
        action.execute(data);
      }
      catch (final CommandException err) {
        status = fromException(err);
      }
      finally {
        Helper.close(action);
      }
    }
    return status;
  }

}
