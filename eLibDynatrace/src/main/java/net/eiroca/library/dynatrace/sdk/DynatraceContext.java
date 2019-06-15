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
package net.eiroca.library.dynatrace.sdk;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.dynatrace.diagnostics.global.Constants;
import com.dynatrace.diagnostics.pdk.PluginEnvironment;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.IContext;

public class DynatraceContext<T extends PluginEnvironment> implements IContext {

  private static final String CONFIG_DTTAGGING = "dtTagging";
  private static final String CONFIG_DTTIMENRNAME = "dtTimerName";

  protected static final Level[] logLevels = new Level[] {
      Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE
  };
  private static final int BOUNUS_LEVEL = 2;

  protected String name;
  protected Logger logger;
  protected Level defaultLevel = Level.INFO;
  protected int bonusLevel = BOUNUS_LEVEL;
  protected T env;
  protected String runner;

  public DynatraceContext(final String contextName) {
    this(contextName, BOUNUS_LEVEL, null);
  }

  public DynatraceContext(final String contextName, final T env) {
    this(contextName, BOUNUS_LEVEL, env);
  }

  public DynatraceContext(final String contextName, final int bonusLevel, final T env) {
    runner = Thread.currentThread().getName();
    name = contextName;
    this.bonusLevel = bonusLevel;
    logger = Logger.getLogger(contextName);
    if (env != null) {
      setEnv(env);
    }
  }

  public String getConfigString(final String propName) {
    if (URLFetcherConfig.CONFIG_TAGNAME.equals(propName)) { return Constants.HEADER_DYNATRACE; }
    if (URLFetcherConfig.CONFIG_TAGVALUE.equals(propName)) { return env.getConfigString(DynatraceContext.CONFIG_DTTIMENRNAME) == null ? null : "NA=" + env.getConfigString(DynatraceContext.CONFIG_DTTIMENRNAME); }
    return env.getConfigString(propName);
  }

  public void setEnv(final T env) {
    this.env = env;
  }

  public String getRunner() {
    return runner;
  }

  private Level getLevel(final LogLevel level) {
    int importance = level.ordinal() + bonusLevel;
    if (importance < 0) {
      importance = 0;
    }
    if (importance >= DynatraceContext.logLevels.length) {
      importance = DynatraceContext.logLevels.length - 1;
    }
    // return DynatraceContext.logLevels[importance];
    return Level.INFO;
  }

  // IConfig

  @Override
  public String getConfigString(final String propName, final String defValue) {
    final String result = getConfigString(propName);
    return result != null ? result : defValue;
  }

  @Override
  public int getConfigInt(final String propName, final int defValue) {
    final Long result = env.getConfigLong(propName);
    return result != null ? result.intValue() : defValue;
  }

  @Override
  public long getConfigLong(final String propName, final long defValue) {
    final Long result = env.getConfigLong(propName);
    return result != null ? result : defValue;
  }

  @Override
  public boolean getConfigBoolean(final String propName, final boolean defValue) {
    if (URLFetcherConfig.CONFIG_TAGGING.equals(propName)) { return env.getConfigBoolean(DynatraceContext.CONFIG_DTTAGGING); }
    final Boolean result = env.getConfigBoolean(propName);
    return result != null ? result : defValue;
  }

  @Override
  public String getConfigPassword(final String propName) {
    return env.getConfigPassword(propName);
  }

  @Override
  public File getConfigFile(final String propName) {
    return env.getConfigFile(propName);
  }

  @Override
  public URL getConfigUrl(final String propName) {
    return env.getConfigUrl(propName);
  }

  @Override
  public boolean hasConfig(String key) {
    return true;
  }

  // ILog

  @Override
  public boolean isLoggable(final LogLevel level) {
    return logger.isLoggable(getLevel(level));
  }

  @Override
  public void trace(final Object... msg) {
    final Level l = getLevel(LogLevel.trace);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void debug(final Object... msg) {
    final Level l = getLevel(LogLevel.debug);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void info(final Object... msg) {
    final Level l = getLevel(LogLevel.info);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void warn(final Object... msg) {
    final Level l = getLevel(LogLevel.warn);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void error(final Object... msg) {
    final Level l = getLevel(LogLevel.error);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void fatal(final Object... msg) {
    final Level l = getLevel(LogLevel.fatal);
    if (logger.isLoggable(l)) {
      logger.log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void log(final LogLevel priority, final String msg) {
    logger.log(getLevel(priority), msg);
  }

  @Override
  public void logF(final LogLevel priority, final String format, final Object... args) {
    final Level l = getLevel(priority);
    if (logger.isLoggable(l)) {
      logger.log(l, MessageFormat.format(format, args));
    }
  }

}
