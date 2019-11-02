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
package net.eiroca.library.dynatrace.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.eiroca.ext.library.gson.GsonCursor;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.dynatrace.lib.DTDevice;
import net.eiroca.library.dynatrace.lib.DTDimensionDefinition;
import net.eiroca.library.dynatrace.lib.DTMetricDefinition;
import net.eiroca.library.dynatrace.lib.oneagent.OneAgentServer;
import net.eiroca.library.metrics.MetricMetadata;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.sdk.GenericProducer;
import net.eiroca.library.sysadm.monitoring.sdk.exporter.GenericExporter;
import net.eiroca.library.system.IContext;

public class OneAgentExporter extends GenericExporter {

  public static String ID = "oneagent";
  //
  public static StringParameter _server = new StringParameter(OneAgentExporter.config, "server", null);
  public static StringParameter _token = new StringParameter(OneAgentExporter.config, "token", null);
  public static IntegerParameter _version = new IntegerParameter(OneAgentExporter.config, "version", 1);
  // Dynamic mapped to parameters
  protected String config_server;
  protected String config_token;
  protected int config_version;

  private OneAgentServer server = null;
  // device, metric, dimension
  private final Map<DTDevice, Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>>> buffer = new HashMap<>();

  public OneAgentExporter() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    super.setup(context);
    GenericExporter.config.convert(context, ID+".", this, "config_");
    GenericExporter.logger.debug("Dynatrace OneAgent: Server ", config_server, " token ", config_token, " version ", config_version);
    if (LibStr.isEmptyOrNull(config_server) || LibStr.isEmptyOrNull(config_token)) {
      GenericExporter.logger.error("Dynatrace OneAgent Server or token cannot be null");
      server = null;
    }
    else {
      server = new OneAgentServer(config_server, config_token, config_version, null);
    }
  }

  @Override
  public boolean beginBulk() {
    final boolean export = (server != null);
    return export;
  }

  @Override
  public String getId() {
    return OneAgentExporter.ID;
  }

  @Override
  public void process(final Event event) {
    MetricMetadata metricInfo = event.getMetricInfo();
    final GsonCursor json = new GsonCursor(event.getData());
    final DTDevice device = getDevice(json);
    if (device == null) { return; }
    final Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> deviceBuffer = getDeviceBuffer(device);
    final DTMetricDefinition metric = getMetric(metricInfo, json);
    if (metric == null) { return; }
    final Map<DTDimensionDefinition, List<Event>> metricBuffer = getMetricBuffer(deviceBuffer, metric);
    final DTDimensionDefinition dimension = getDimension(metricInfo, json);
    final List<Event> dimensionBuffer = getDimensionBuffer(metricBuffer, dimension);
    dimensionBuffer.add(event);
  }

  private Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> getDeviceBuffer(final DTDevice deviceID) {
    Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> result = buffer.get(deviceID);
    if (result == null) {
      result = new HashMap<>();
      buffer.put(deviceID, result);
    }
    return result;
  }

  private Map<DTDimensionDefinition, List<Event>> getMetricBuffer(final Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> deviceBuffer, final DTMetricDefinition metricID) {
    Map<DTDimensionDefinition, List<Event>> result = deviceBuffer.get(metricID);
    if (result == null) {
      result = new HashMap<>();
      deviceBuffer.put(metricID, result);
    }
    return result;
  }

  private List<Event> getDimensionBuffer(final Map<DTDimensionDefinition, List<Event>> metricBuffer, final DTDimensionDefinition dimensionInfo) {
    List<Event> result = metricBuffer.get(dimensionInfo);
    if (result == null) {
      result = new ArrayList<>();
      metricBuffer.put(dimensionInfo, result);
    }
    return result;
  }

  @Override
  public void endBulk() {
    if (buffer.size() > 0) {
      for (final Entry<DTDevice, Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>>> device : buffer.entrySet()) {
        final DTDevice deviceID = device.getKey();
        exportDevice(deviceID, device.getValue());
      }
      buffer.clear();
    }
  }

  private void exportDevice(final DTDevice device, final Map<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> metrics) {
    final JsonObject data = new JsonObject();
    final JsonArray series = new JsonArray();
    data.add("series", series);
    for (final Entry<DTMetricDefinition, Map<DTDimensionDefinition, List<Event>>> metric : metrics.entrySet()) {
      final DTMetricDefinition metricDef = metric.getKey();
      final Map<DTDimensionDefinition, List<Event>> dimensions = metric.getValue();
      for (final Entry<DTDimensionDefinition, List<Event>> dimension : dimensions.entrySet()) {
        final JsonObject serie = new JsonObject();
        series.add(serie);
        serie.addProperty("timeseriesId", metricDef.getId());
        final DTDimensionDefinition dimensionInfo = dimension.getKey();
        if (LibStr.isNotEmptyOrNull(dimensionInfo.getId())) {
          final JsonObject dimensionJson = new JsonObject();
          dimensionJson.addProperty(dimensionInfo.getId(), dimensionInfo.getName());
          serie.add("dimensions", dimensionJson);
        }
        final JsonArray points = new JsonArray();
        serie.add("dataPoints", points);
        final List<Event> measures = dimension.getValue();
        for (final Event event : measures) {
          final JsonArray datum = new JsonArray();
          points.add(datum);
          datum.add(event.getTimestamp()); // timestamp
          datum.add(event.getValue()); // value
        }
      }
    }
    GenericExporter.logger.debug(device.getId() + "->" + data.toString());
    server.addDataPoints(device, data);
  }

  private DTDevice getDevice(final GsonCursor json) {
    final String host = json.getString(GenericProducer.FLD_HOST);
    return LibStr.isNotEmptyOrNull(host) ? new DTDevice(host) : null;
  }

  private DTMetricDefinition getMetric(MetricMetadata metricInfo, final GsonCursor json) {
    StringBuilder name = new StringBuilder(128);
    final String group = json.getString(GenericProducer.FLD_GROUP);
    if (group != null) {
      name.append(group.replace('.', ' ')).append('.');
    }
    final String metric = json.getString(GenericProducer.FLD_METRIC);
    if (metric != null) {
      name.append(metric.replace('.', ' '));
    }
    final String splitGroup = json.getString(GenericProducer.FLD_SPLIT_GROUP);
    if (LibStr.isNotEmptyOrNull(splitGroup)) {
      final String splitName = json.getString(GenericProducer.FLD_SPLIT_NAME);
      if (metricInfo == null) {
        name.append('.').append(splitGroup.replace('.', ' '));
        if (LibStr.isNotEmptyOrNull(splitName)) {
          name.append('.').append(splitName.replace('.', ' '));
        }
      }
      else {
        if (!metricInfo.dimensions().contains(splitGroup)) {
          name.append('.').append(splitGroup.replace('.', ' '));
          if (LibStr.isNotEmptyOrNull(splitName)) {
            name.append('.').append(splitName.replace('.', ' '));
          }
        }
      }
    }
    if (name.length() == 0) { return null; }
    String metricName = name.toString();
    final String id = "net.eiroca:" + metricName.toLowerCase().replace(' ', '_');
    return new DTMetricDefinition(id, metricName);
  }

  private DTDimensionDefinition getDimension(MetricMetadata metricInfo, final GsonCursor json) {
    final String splitGroup = json.getString(GenericProducer.FLD_SPLIT_GROUP);
    final String splitName = json.getString(GenericProducer.FLD_SPLIT_NAME);
    if (LibStr.isEmptyOrNull(splitGroup) || LibStr.isEmptyOrNull(splitName)) { return DTDimensionDefinition.EMPTY; }
    if (metricInfo != null) {
      if (!metricInfo.dimensions().contains(splitGroup)) { return DTDimensionDefinition.EMPTY; }
    }
    return new DTDimensionDefinition(splitGroup, splitName);
  }

}
