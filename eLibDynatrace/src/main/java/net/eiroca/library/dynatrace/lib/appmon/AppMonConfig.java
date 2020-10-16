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
package net.eiroca.library.dynatrace.lib.appmon;

import net.eiroca.library.rule.RuleManager;
import net.eiroca.library.rule.context.LookupRuleGroup;
import net.eiroca.library.rule.context.RegExRuleGroup;

public class AppMonConfig {

  public static final String CONF_PATH = ".\\conf\\";
  public static final String INPUT_PATH = ".\\input\\";
  public static final String OUTPUT_PATH = ".\\output\\";

  public static final String OUTPUT_STATS_LOOKUP = AppMonConfig.OUTPUT_PATH + "stats-lookup.csv";
  public static final String OUTPUT_STATS_MISSING_KEY = AppMonConfig.OUTPUT_PATH + "missing-key.csv";
  public static final String OUTPUT_STATS_REPLACEMENT = AppMonConfig.OUTPUT_PATH + "stats-replacement.csv";
  public static final String OUTPUT_STATS_RULES = AppMonConfig.OUTPUT_PATH + "stats-rules.csv";

  public static final RuleManager replaceManager = new RuleManager("replace");
  public static final RuleManager lookupManager = new RuleManager("lookup");
  public static final RuleManager ruleManager = new RuleManager("rule");

  public static RegExRuleGroup fix_severity = null;

  public static LookupRuleGroup system2service = null;

  public static RegExRuleGroup replace_description = null;
  public static RegExRuleGroup replace_message = null;

  public static RegExRuleGroup rules_agents = null;

  public static void exportStats() {
    AppMonConfig.lookupManager.saveStats(AppMonConfig.OUTPUT_STATS_LOOKUP);
    AppMonConfig.lookupManager.saveMissingKey(AppMonConfig.OUTPUT_STATS_MISSING_KEY);
    AppMonConfig.replaceManager.saveStats(AppMonConfig.OUTPUT_STATS_REPLACEMENT);
    AppMonConfig.ruleManager.saveStats(AppMonConfig.OUTPUT_STATS_RULES);
  }

  public static void loadConf() {
    AppMonConfig.fix_severity = AppMonConfig.replaceManager.addRegExRules("DT_fixSeverity", AppMonConfig.CONF_PATH + "DT_fixSeverity.csv");
    AppMonConfig.system2service = AppMonConfig.lookupManager.addLookupRule("system2service", AppMonConfig.CONF_PATH + "system2service.csv");
    AppMonConfig.replace_description = AppMonConfig.replaceManager.addRegExRules("DT_description", AppMonConfig.CONF_PATH + "DT_description.csv");
    AppMonConfig.replace_message = AppMonConfig.replaceManager.addRegExRules("DT_message", AppMonConfig.CONF_PATH + "DT_message.csv");
    AppMonConfig.rules_agents = AppMonConfig.replaceManager.addRegExRules("AgentRules", AppMonConfig.CONF_PATH + "agentsRules.csv");
  }

}
