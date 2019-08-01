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
package net.eiroca.library.dynatrace.lib;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
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
import net.eiroca.library.server.ServerResponse;
import net.eiroca.library.system.Logs;

public class DynatraceServer {

  protected static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  protected static final Logger logger = Logs.getLogger();

  protected CloseableHttpClient httpClient;
  protected HttpContext context = new BasicHttpContext();
  protected String authHeader;
  protected String server;

  public boolean isDryRun = false;

  public DynatraceServer(final String server, final String authHeader, final HttpHost proxy) {
    this.server = server;
    try {
      httpClient = HttpClientHelper.createAcceptAllClient(proxy);
    }
    catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
      DynatraceServer.logger.error("Unable to create HttpClient", e);
    }
    this.authHeader = authHeader;
  }

  public String getServer() {
    return server;
  }

  public void prepareJsonCall(final HttpEntityEnclosingRequestBase method, final String json) {
    method.setHeader(HttpHeaders.ACCEPT, String.format(HttpClientHelper.APPLICATION_JSON, "utf-8"));
    method.setHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8");
    method.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    if (json != null) {
      method.setHeader(HttpHeaders.CONTENT_TYPE, String.format(HttpClientHelper.APPLICATION_JSON, "utf-8"));
      final HttpEntity entity = new StringEntity(json, "utf-8");
      method.setEntity(entity);
    }
  }

  public ServerResponse makePut(final String url, final String json) {
    return makeCall(new HttpPut(url), json);
  }

  public ServerResponse makePost(final String url, final String json) {
    return makeCall(new HttpPost(url), json);
  }

  public ServerResponse makeCall(final HttpEntityEnclosingRequestBase method, final String json) {
    CloseableHttpResponse response = null;
    prepareJsonCall(method, json);
    final ServerResponse result = new ServerResponse(999, "");
    final String methodType = method.getMethod();
    try {
      DynatraceServer.logger.debug(methodType + " URI: " + method.getURI());
      DynatraceServer.logger.trace(methodType + " Request: " + json);
      if (!isDryRun) {
        response = httpClient.execute(method, context);
        result.status = response.getStatusLine().getStatusCode();
        final HttpEntity responseEntity = response.getEntity();
        result.message = ((responseEntity != null) ? EntityUtils.toString(responseEntity) : "");
      }
      else {
        result.status = 200;
        result.message = "DRY_RUN";
      }
      DynatraceServer.logger.debug(methodType + " Result: " + result.status);
      DynatraceServer.logger.trace(methodType + " Response: " + result.message);
    }
    catch (final IOException e) {
      DynatraceServer.logger.error("Unable to execute request", e);
      result.status = 998;
      result.message = Helper.getExceptionAsString(e);
    }
    finally {
      Helper.close(response);
    }
    return result;
  }

}
