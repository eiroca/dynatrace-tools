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
package net.eiroca.library.dynatrace.plugin.appmon;

import java.util.ArrayList;
import java.util.List;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;
import net.eiroca.library.core.LibStr;

public class SourceReferences {

  private final List<String> sourceNames = new ArrayList<>();
  private final List<String> sourceHosts = new ArrayList<>();
  private final List<String> sourceNameAtHosts = new ArrayList<>();

  public List<String> getSourceNames() {
    return sourceNames;
  }

  public List<String> getSourceHosts() {
    return sourceHosts;
  }

  public String getAllNames() {
    return LibStr.merge(sourceNames, BaseConstants.COMMA_WS, null);
  }

  public String getAllHosts() {
    return LibStr.merge(sourceHosts, BaseConstants.COMMA_WS, null);
  }

  public String getAllNameHosts() {
    return LibStr.merge(sourceNameAtHosts, BaseConstants.COMMA_WS, null);
  }

  public void add(final String name, final String host) {
    // add source name and source host
    if (LibStr.isNotEmptyOrNull(name) && !name.equals(BaseConstants.DASH)) {
      if (!sourceNames.contains(name)) {
        sourceNames.add(name);
      }
    }
    if (LibStr.isNotEmptyOrNull(host) && !host.equals(BaseConstants.DASH)) {
      if (!sourceHosts.contains(host)) {
        sourceHosts.add(host);
      }
    }
    if ((LibStr.isNotEmptyOrNull(name) && !name.equals(BaseConstants.DASH)) && (LibStr.isNotEmptyOrNull(host) && !host.equals(BaseConstants.DASH))) {
      final String sNameHost = new StringBuilder(name).append(BaseConstants.AT).append(host).toString();
      if (!sourceNameAtHosts.contains(sNameHost)) {
        sourceNameAtHosts.add(sNameHost);
      }
    }
  }
}
