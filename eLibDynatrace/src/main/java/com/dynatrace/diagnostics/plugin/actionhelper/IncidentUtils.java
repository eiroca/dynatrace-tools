package com.dynatrace.diagnostics.plugin.actionhelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;
import com.dynatrace.diagnostics.pdk.Duration;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.IncidentRule;
import com.dynatrace.diagnostics.pdk.Sensitivity;
import com.dynatrace.diagnostics.pdk.Sensitivity.Type;

/**
 * @author eugene.turetsky
 *
 */
public class IncidentUtils {

  public static final int FIRST_ELEMENT_COLLECTION = 0;
  public static final long DURATION_IS_NULL = Long.MIN_VALUE;

  static int seq = 0;
  static String hostname;

  static {
    try {
      IncidentUtils.hostname = InetAddress.getLocalHost().getCanonicalHostName();
    }
    catch (final UnknownHostException e) {
      IncidentUtils.hostname = new Random(System.currentTimeMillis()).nextInt(100000) + ".localhost";
    }
  }

  public static Type getSensitivityType(final List<Incident> incidents) {
    // Custom, Immediate, Medium, Low, PerViolation, After60s;
    // TODO using the first element of Incidents collection. Are there any examples when Incidents collection has more
    // than one incident?
    Type sensitivityType = null;
    Sensitivity sensitivity;
    if ((incidents != null) && !incidents.isEmpty() && (incidents.get(IncidentUtils.FIRST_ELEMENT_COLLECTION) != null)) {
      final IncidentRule incidentRule = incidents.get(IncidentUtils.FIRST_ELEMENT_COLLECTION).getIncidentRule();
      if ((incidentRule != null) && ((sensitivity = incidentRule.getSensitivity()) != null)) {
        sensitivityType = sensitivity.getType();
      }
    }
    return sensitivityType;
  }

  public static Type getSensitivityType(final Incident incident) {
    // Custom, Immediate, Medium, Low, PerViolation, After60s;
    // TODO using the first element of Incidents collection. Are there any examples when Incidents collection has more
    // than one incident?
    Type sensitivityType = null;
    Sensitivity sensitivity;
    if (incident != null) {
      final IncidentRule incidentRule = incident.getIncidentRule();
      if ((incidentRule != null) && ((sensitivity = incidentRule.getSensitivity()) != null)) {
        sensitivityType = sensitivity.getType();
      }
    }
    return sensitivityType;
  }

  public static long getDuration(final List<Incident> incidents) {
    if ((incidents != null) && !incidents.isEmpty()) {
      final Incident incident = incidents.get(IncidentUtils.FIRST_ELEMENT_COLLECTION);
      final Duration duration = incident.getDuration();
      if ((incident != null) && (duration != null)) { return duration.getDurationInMs(); }
    }
    return IncidentUtils.DURATION_IS_NULL;
  }

  public static long getDuration(final Incident incident) {
    final Duration duration = (incident != null) ? incident.getDuration() : null;
    return (duration != null) ? duration.getDurationInMs() : IncidentUtils.DURATION_IS_NULL;
  }

  public static String getIncidentRuleName(final List<Incident> incidents) {
    String incidentRuleName = "";
    if ((incidents != null) && !incidents.isEmpty() && (incidents.get(IncidentUtils.FIRST_ELEMENT_COLLECTION) != null)) {
      final IncidentRule incidentRule = incidents.get(IncidentUtils.FIRST_ELEMENT_COLLECTION).getIncidentRule();
      if (incidentRule != null) {
        incidentRuleName = incidentRule.getName();
      }
    }
    return incidentRuleName;
  }

  public static synchronized int getSeq() {
    return (IncidentUtils.seq++) % 100000;
  }

  public static String getContentId() {
    final int c = IncidentUtils.getSeq();
    return c + "." + System.currentTimeMillis() + "@" + IncidentUtils.hostname;
  }

}
