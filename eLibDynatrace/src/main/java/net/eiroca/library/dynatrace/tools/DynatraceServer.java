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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.Logs;

public class DynatraceServer {

  public static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  private static Logger logger = Logs.getLogger();

  private final String API_PROTOCOL = "https://";
  private final String API_PORT = ":8021";
  private final String API_PREFIX = "/api";
  private String API_VERSION = "/v2";

  String server;
  CloseableHttpClient httpClient;
  HttpContext context = new BasicHttpContext();
  final String authHeader;

  public DynatraceServer(final String server, final int version, final String authHeader, final HttpHost proxy) {
    this.server = server;
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
    try {
      httpClient = HttpClientHelper.createAcceptAllClient(proxy);
    }
    catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
      DynatraceServer.logger.error("Unable to create HttpClient", e);
    }
    this.authHeader = authHeader;
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

  public void prepareJsonCall(final HttpEntityEnclosingRequestBase method, final String json) {
    method.setHeader(HttpHeaders.CONTENT_TYPE, String.format(HttpClientHelper.APPLICATION_JSON, "utf-8"));
    method.setHeader(HttpHeaders.ACCEPT, String.format(HttpClientHelper.APPLICATION_JSON, "utf-8"));
    method.setHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8");
    method.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    final HttpEntity entity = new StringEntity(json, "utf-8");
    method.setEntity(entity);
  }

  void closeAlert(final Alert alert) {
    alert.end = new Date();
    alert.state = AlertState.CLOSED;
    CloseableHttpResponse response = null;
    final HttpEntityEnclosingRequestBase httpPut = new HttpPut(getAlertUrl(alert.id));
    final String json = DynatraceProcessor.toDynaTraceJson(alert).toString();
    prepareJsonCall(httpPut, json);
    try {
      response = httpClient.execute(httpPut, context);
      final int responseCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity2 = response.getEntity();
      DynatraceServer.logger.debug("Closing " + alert.id + " " + responseCode + " - " + ((entity2 != null) ? EntityUtils.toString(entity2) : ""));
    }
    catch (final IOException e) {
      DynatraceServer.logger.error("Unable to change alert", e);
    }
    finally {
      Helper.close(response);
    }
  }

  public void updateAlert(final Alert alert) {
    final String url = getAlertUrl(alert.id);
    CloseableHttpResponse response = null;
    final HttpEntityEnclosingRequestBase httpPut = new HttpPut(url);
    final String json = DynatraceProcessor.toDynaTraceJson(alert).toString();
    prepareJsonCall(httpPut, json);
    try {
      DynatraceServer.logger.trace("PUT Body=" + json);
      DynatraceServer.logger.trace("Invoking " + httpPut);
      response = httpClient.execute(httpPut, context);
      final int responseCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity2 = response.getEntity();
      DynatraceServer.logger.debug("Update " + alert.id + " " + responseCode + " - " + ((entity2 != null) ? EntityUtils.toString(entity2) : ""));
    }
    catch (final IOException e) {
      DynatraceServer.logger.error("Unable to change alert", e);
    }
    finally {
      Helper.close(response);
    }
  }

  public void createAlert(final Alert alert) {
    final String json = DynatraceProcessor.toDynaTraceJson(alert).toString();
    final String url = getAlertUrl();
    final HttpEntityEnclosingRequestBase httpPost = new HttpPost(url);
    prepareJsonCall(httpPost, json);
    CloseableHttpResponse response = null;
    DynatraceServer.logger.trace("POST Body=" + json);
    try {
      DynatraceServer.logger.trace("Invoking " + httpPost);
      response = httpClient.execute(httpPost, context);
      final int responseCode = response.getStatusLine().getStatusCode();
      if (responseCode < 400) {
        final Header locationHeader = response.getLastHeader(HttpHeaders.LOCATION);
        DynatraceServer.logger.trace("Location: " + locationHeader);
        final String location = locationHeader != null ? locationHeader.getValue() : "";
        final int sepPos = location.lastIndexOf("/");
        if (sepPos > 0) {
          alert.id = location.substring(sepPos + 1);
        }
      }
      final HttpEntity entity2 = response.getEntity();
      DynatraceServer.logger.debug("Create " + alert.system + " " + alert.id + " " + responseCode + " - " + EntityUtils.toString(entity2));
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
