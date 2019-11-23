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
package net.eiroca.library.dynatrace.lib.appmon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.eiroca.ext.library.gson.GsonUtil;

public class ServiceStatus {

  public String name;
  public String description;
  public ServiceState state = ServiceState.OK;
  public Date lastChange = null;
  public List<Alert> alerts = new ArrayList<>();
  public Map<AlertSeverity, Integer> statusCount = new HashMap<>();
  public Map<AlertSeverity, Date> statusDate = new HashMap<>();
  public Alert alert;

  public ServiceStatus(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String toString() {
    return GsonUtil.toJSON(this);
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
      final Date startDate = alert.start;
      Date lastDate = statusDate.get(severity);
      if ((lastDate == null) || ((startDate != null) && lastDate.after(startDate))) {
        lastDate = startDate;
      }
      statusDate.put(severity, lastDate);
    }
    final int infoCount = ServiceStatus.val(statusCount.get(AlertSeverity.INFO), 0);
    final int warnCount = ServiceStatus.val(statusCount.get(AlertSeverity.WARN), 0);
    final int errCount = ServiceStatus.val(statusCount.get(AlertSeverity.SEVERE), 0);
    final int criticalCount = ServiceStatus.val(statusCount.get(AlertSeverity.CRITICAL), 0);
    final Date now = new Date();
    final Date infoDate = ServiceStatus.val(statusDate.get(AlertSeverity.INFO), now);
    final Date warnDate = ServiceStatus.val(statusDate.get(AlertSeverity.WARN), now);
    final Date errDate = ServiceStatus.val(statusDate.get(AlertSeverity.SEVERE), now);
    final Date criticalDate = ServiceStatus.val(statusDate.get(AlertSeverity.CRITICAL), now);
    state = ServiceState.OK;
    if (infoCount > 9) {
      state = ServiceState.WARNING;
      lastChange = infoDate;
    }
    if (warnCount > 0) {
      lastChange = warnDate;
      if (warnCount > 1) {
        state = ServiceState.ERROR;
      }
      else if (infoCount > 2) {
        state = ServiceState.ERROR;
      }
      else {
        state = ServiceState.WARNING;
      }
    }
    if (errCount > 0) {
      lastChange = errDate;
      if (errCount > 1) {
        state = ServiceState.FAILED;
      }
      else if (warnCount > 3) {
        state = ServiceState.FAILED;
      }
      else {
        state = ServiceState.ERROR;
      }
    }
    if (criticalCount > 0) {
      state = ServiceState.FAILED;
      lastChange = criticalDate;
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

  private static Date val(final Date val, final Date def) {
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
    statusDate.clear();
  }
}
