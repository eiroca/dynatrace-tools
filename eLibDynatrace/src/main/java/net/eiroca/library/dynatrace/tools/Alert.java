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

import java.util.Date;
import java.util.UUID;
import com.google.gson.JsonParser;
import net.eiroca.ext.library.gson.GsonUtil;

public class Alert implements Comparable<Alert> {

  public String id;
  public String system;
  public AlertSeverity severity;
  public AlertState state;
  public String message;
  public String description;
  public String rule;
  public Date start;
  public Date end;

  public String agent;
  public String host;

  public String measureName;
  public String measureUnit;
  public Double measureValue;
  public Double measureLowerLimit;
  public Double measureLowerLimitWarning;
  public Double measureUpperLimit;
  public Double measureUpperLimitWarning;
  public Double measureStatus;

  final transient JsonParser parser = new JsonParser();

  public Alert(final String id) {
    this.id = id;
  }

  public Alert() {
    newId();
    start = new Date();
  }

  @Override
  public String toString() {
    return GsonUtil.toJSON(this);
  }

  @Override
  public int compareTo(final Alert o) {
    if (severity.ordinal() > o.severity.ordinal()) {
      return -1;
    }
    else if (severity.ordinal() < o.severity.ordinal()) {
      return 1;
    }
    else {
      return (start != null) ? -start.compareTo(o.start) : -1;
    }
  }

  public void newId() {
    id = "$" + UUID.randomUUID().toString();
  }
}
