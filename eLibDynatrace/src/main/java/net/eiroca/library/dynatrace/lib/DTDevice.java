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
package net.eiroca.library.dynatrace.lib;

import java.util.ArrayList;
import java.util.List;

public class DTDevice extends DTObject {

  private final List<String> hosts = new ArrayList<>();

  public DTDevice(final String host) {
    this(host, host, host);
  }

  public DTDevice(final String deviceID, final String deviceName, final String deviceHost) {
    id = deviceID;
    name = deviceName;
    hosts.add(deviceHost);
  }

  public List<String> getHost() {
    return hosts;
  }

}
