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
package net.eiroca.library.dynatrace.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.eiroca.ext.library.gson.JSonUtil;

public class ServiceStatus {

  public String name;
  public String description;
  public ServiceState state = ServiceState.OK;
  public List<Alert> alerts = new ArrayList<>();
  public Map<AlertSeverity, Integer> statusCount = new HashMap<>();
  public Alert alert;

  public ServiceStatus(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String toString() {
    return JSonUtil.toJSON(this);
  }

  public void updateAlert(final Alert alert) {
    alerts.add(alert);
  }

  public void updateStatus() {
    for (final Alert alert : alerts) {
      final AlertSeverity severity = alert.severity;
      final Integer cnt = statusCount.get(severity);
      if (cnt == null) {
        statusCount.put(severity, 1);
      }
      else {
        statusCount.put(severity, cnt + 1);
      }
    }
    final int infoCount = ServiceStatus.val(statusCount.get(AlertSeverity.INFO), 0);
    final int warnCount = ServiceStatus.val(statusCount.get(AlertSeverity.WARN), 0);
    final int errCount = ServiceStatus.val(statusCount.get(AlertSeverity.SEVERE), 0);
    final int criticalCount = ServiceStatus.val(statusCount.get(AlertSeverity.CRITICAL), 0);
    if (infoCount > 50) {
      state = ServiceState.WARNING;
    }
    if (warnCount > 0) {
      state = ServiceState.WARNING;
    }
    if (errCount > 0) {
      if (warnCount > 0) {
        state = ServiceState.ERROR;
      }
      else {
        state = ServiceState.WARNING;
      }
    }
    if (errCount > 1) {
      state = ServiceState.ERROR;
    }
    if (criticalCount > 0) {
      state = ServiceState.ERROR;
    }
    if (criticalCount > 10) {
      state = ServiceState.FAILED;
    }
  }

  private static int val(final Integer val, final int def) {
    if (val == null) {
      return def;
    }
    else {
      return val;
    }
  }

  public void reset() {
    state = ServiceState.OK;
    alerts.clear();
    statusCount.clear();
  }
}
