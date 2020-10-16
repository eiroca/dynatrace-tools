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
package net.eiroca.library.dynatrace.lib.oneagent;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import net.eiroca.ext.library.gson.GsonCursor;
import net.eiroca.ext.library.gson.SimpleGson;
import net.eiroca.library.dynatrace.lib.DTDevice;
import net.eiroca.library.dynatrace.lib.DTMetricDefinition;
import net.eiroca.library.dynatrace.lib.DynatraceServer;

public class OneAgentServer extends DynatraceServer {

  private static final int CACHE_EXPIRATION = 60;
  private static final int CACHE_SIZE = 1000;

  private static final String FMT_BASEPREFIX = "/api/v{0}";
  private final String basePrefix;

  private final Cache<String, DTDevice> deviceCache;
  private final Cache<String, DTMetricDefinition> metricCache;

  public OneAgentServer(final String server, final String token, final int version, final HttpHost proxy) {
    super(server, "Api-Token " + token, proxy);
    basePrefix = MessageFormat.format(OneAgentServer.FMT_BASEPREFIX, version);
    deviceCache = CacheBuilder.newBuilder().maximumSize(OneAgentServer.CACHE_SIZE).expireAfterAccess(OneAgentServer.CACHE_EXPIRATION, TimeUnit.MINUTES).build();
    metricCache = CacheBuilder.newBuilder().maximumSize(OneAgentServer.CACHE_SIZE).expireAfterAccess(OneAgentServer.CACHE_EXPIRATION, TimeUnit.MINUTES).build();
  }

  public DTDevice createDevice(final String host) {
    return createDevice(host, host, host);
  }

  public DTDevice createDevice(final String deviceID, final String deviceName, final String deviceHost) {
    DTDevice result = null;
    result = deviceCache.asMap().get(deviceID);
    if (result == null) {
      result = new DTDevice(deviceID, deviceName, deviceHost);
      makePost(getUrl("/entity/infrastructure/custom/" + result.getId()), device2json(result));
      deviceCache.put(deviceID, result);
    }
    return result;
  }

  private String device2json(final DTDevice device) {
    final SimpleGson data = new SimpleGson(false);
    final GsonCursor json = new GsonCursor(data);
    json.set("displayName", device.getName());
    json.set("hostNames", device.getHost());
    return data.toString();
  }

  public void addDataPoints(final DTDevice deviceInfo, final JsonObject data) {
    // makePost(getUrl("/entity/infrastructure/custom/" + deviceInfo.getId()), data.toString());
  }

  private String getUrl(final String api) {
    return server + basePrefix + api;
  }

  public DTMetricDefinition getMetricDefinition(final String metricID) {
    // TODO Auto-generated method stub
    return null;
  }

  public DTMetricDefinition createMetric(final String id, final String name) {
    DTMetricDefinition result = null;
    result = metricCache.asMap().get(id);
    if (result == null) {
      result = new DTMetricDefinition(id, name);
      //
      makePut("/timeseries/" + result.getId(), metric2json(result));
      metricCache.put(id, result);
    }
    return result;
  }

  private String metric2json(final DTMetricDefinition result) {
    // TODO Auto-generated method stub
    return null;
  }

}
