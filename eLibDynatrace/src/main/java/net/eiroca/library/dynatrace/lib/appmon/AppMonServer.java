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

import java.io.IOException;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.dynatrace.lib.DynatraceServer;
import net.eiroca.library.server.ServerResponse;

public class AppMonServer extends DynatraceServer {

  private final String API_PROTOCOL = "https://";
  private final String API_PORT = ":8021";
  private final String API_PREFIX = "/api";
  private String API_VERSION = "/v2";

  public AppMonServer(final String server, final int version, final String authHeader, final HttpHost proxy) {
    super(server, authHeader, proxy);
    if (version == 72) {
      API_VERSION = "/v4";
    }
    else if (version == 71) {
      API_VERSION = "/v3";
    }
    else if (version == 70) {
      API_VERSION = "/v2";
    }
    else {
      API_VERSION = "/v1";
    }
  }

  private StringBuilder baseAgentsUrl() {
    final StringBuilder sb = new StringBuilder();
    sb.append(API_PROTOCOL).append(server).append(API_PORT).append("/rest/management/agents");
    return sb;
  }

  public String getAgentsUrl() {
    final StringBuilder sb = baseAgentsUrl();
    return sb.toString();
  }

  private StringBuilder baseAlertUrl() {
    final StringBuilder sb = new StringBuilder();
    sb.append(API_PROTOCOL).append(server).append(API_PORT).append(API_PREFIX).append(API_VERSION).append("/alerts");
    return sb;
  }

  public String getAlertUrl() {
    final StringBuilder sb = baseAlertUrl();
    return sb.toString();
  }

  public String getAlertUrl(final String id) {
    final StringBuilder sb = baseAlertUrl();
    sb.append("/").append(id);
    return sb.toString();
  }

  public String getAlertsUrl(final String systemProfile, final String incidentRule, final String state, final Date startDate, final Date endDate) {
    final StringBuilder sb = baseAlertUrl();
    boolean first = true;
    first = HttpClientHelper.appendParam(sb, "systemprofile", LibStr.urlEncode(systemProfile), first);
    first = HttpClientHelper.appendParam(sb, "incidentrule", LibStr.urlEncode(incidentRule), first);
    first = HttpClientHelper.appendParam(sb, "state", state, first);
    first = HttpClientHelper.appendParam(sb, "from", startDate != null ? DynatraceServer.ISO8601.format(startDate) : null, first);
    first = HttpClientHelper.appendParam(sb, "to", endDate != null ? DynatraceServer.ISO8601.format(endDate) : null, first);
    return sb.toString();
  }

  public String getDashboardUrl(final String dashboard, final String type) {
    final StringBuilder sb = new StringBuilder(256);
    sb.append(API_PROTOCOL).append(server).append(API_PORT).append("/rest/management/reports/create/");
    sb.append(LibStr.urlEncode(dashboard).replaceAll("\\+", "%20"));
    boolean first = true;
    first = HttpClientHelper.appendParam(sb, "type", type, first);
    return sb.toString();
  }

  void closeAlert(final Alert alert) {
    alert.end = new Date();
    alert.state = AlertState.CLOSED;
    final String url = getAlertUrl(alert.id);
    final String json = AppMonProcessor.toDynaTraceJson(alert).toString();
    final ServerResponse response = makePut(url, json);
    DynatraceServer.logger.debug("Closing " + alert.id + " " + response.status + " - " + response.message);
  }

  public void updateAlert(final Alert alert) {
    final String url = getAlertUrl(alert.id);
    final String json = AppMonProcessor.toDynaTraceJson(alert).toString();
    final ServerResponse response = makePut(url, json);
    DynatraceServer.logger.debug("Updating " + alert.id + " " + response.status + " - " + response.message);
  }

  public void createAlert(final Alert alert) {
    final String url = getAlertUrl();
    final String json = AppMonProcessor.toDynaTraceJson(alert).toString();
    final HttpEntityEnclosingRequestBase httpPost = new HttpPost(url);
    prepareJsonCall(httpPost, json);
    CloseableHttpResponse response = null;
    DynatraceServer.logger.trace("POST Body=" + json);
    try {
      DynatraceServer.logger.trace("Invoking " + httpPost);
      int responseCode = 200;
      String responseData = "DRY_RUN";
      if (!isDryRun) {
        response = httpClient.execute(httpPost, context);
        responseCode = response.getStatusLine().getStatusCode();
        if (responseCode < 400) {
          final Header locationHeader = response.getLastHeader(HttpHeaders.LOCATION);
          DynatraceServer.logger.trace("Location: " + locationHeader);
          final String location = locationHeader != null ? locationHeader.getValue() : "";
          final int sepPos = location.lastIndexOf("/");
          if (sepPos > 0) {
            alert.id = location.substring(sepPos + 1);
          }
        }
        final HttpEntity responseEntity = response.getEntity();
        responseData = ((responseEntity != null) ? EntityUtils.toString(responseEntity) : "");
      }
      DynatraceServer.logger.debug("Create " + alert.system + " " + alert.id + " " + responseCode + " - " + responseData);
    }
    catch (final IOException e) {
      DynatraceServer.logger.warn("IOException", e);
    }
    finally {
      Helper.close(response);
    }
  }

  public String readAlert(final String id) {
    final String url = getAlertUrl(id);
    DynatraceServer.logger.trace("Invoking " + url);
    final String json = HttpClientHelper.getJson(httpClient, url, authHeader, "utf-8");
    return json;
  }

  public String readAlertsList(final String systemProfile, final String incidentRule, final String state, final Date startDate, final Date endDate) {
    final String url = getAlertsUrl(systemProfile, incidentRule, state, startDate, endDate);
    DynatraceServer.logger.trace("Invoking " + url);
    final String json = HttpClientHelper.getJson(httpClient, url, authHeader, "utf-8");
    return json;
  }

  public String readAgents() {
    final String url = getAgentsUrl();
    DynatraceServer.logger.trace("Invoking " + url);
    final String xml = HttpClientHelper.getData(httpClient, url, null, authHeader, null);
    return xml;
  }

  public String getDashboard(final String dashboard, final String type) {
    final String url = getDashboardUrl(dashboard, type);
    DynatraceServer.logger.trace("Invoking " + url);
    final String data = HttpClientHelper.getData(httpClient, url, null, authHeader, null);
    return data;
  }

}
