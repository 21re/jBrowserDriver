/* 
 * jBrowserDriver (TM)
 * Copyright (C) 2014-2015 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * Cincinnati, Ohio, USA
 *
 * You can redistribute this program and/or modify it under the terms of the GNU Affero General Public
 * License version 3 as published by the Free Software Foundation.
 *
 * "ScreenSlicer", "jBrowserDriver", "Machine Publishers", and "automatic, zero-config web scraping"
 * are trademarks of Machine Publishers, LLC.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License version 3 for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License version 3 along with this
 * program. If not, see http://www.gnu.org/licenses/
 * 
 * For general details about how to investigate and report license violations, please see
 * https://www.gnu.org/licenses/gpl-violation.html and email the author, ops@machinepublishers.com
 */
package com.machinepublishers.jbrowserdriver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

class Logs implements org.openqa.selenium.logging.Logs {
  private static final boolean CONSOLE = "true".equals(System.getProperty("jbd.logconsole"));
  private static final int MAX_LOGS = Integer.parseInt(System.getProperty("jbd.maxlogs", "5000"));
  private final LinkedList<LogEntry> entries = new LinkedList<LogEntry>();
  private static final Map<Long, Logs> logMap = new HashMap<Long, Logs>();

  private Logs() {}

  static Logs newInstance(long settingsId) {
    Logs logs = new Logs();
    synchronized (logMap) {
      logMap.put(settingsId, logs);
    }
    return logs;
  }

  static Logs logsFor(long settingsId) {
    synchronized (logMap) {
      return logMap.get(settingsId);
    }
  }

  static void close(long settingsId) {
    synchronized (logMap) {
      logMap.remove(settingsId).clear();
    }
  }

  void clear() {
    synchronized (entries) {
      entries.clear();
    }
  }

  void trace(String message) {
    final LogEntry entry = new LogEntry(Level.FINEST, System.currentTimeMillis(), message);
    synchronized (entries) {
      entries.add(entry);
      if (entries.size() > MAX_LOGS) {
        entries.removeFirst();
      }
    }
    if (CONSOLE) {
      System.out.println(entry);
    }
  }

  void warn(String message) {
    final LogEntry entry = new LogEntry(Level.WARNING, System.currentTimeMillis(), message);
    synchronized (entries) {
      entries.add(entry);
      if (entries.size() > MAX_LOGS) {
        entries.removeFirst();
      }
    }
    if (CONSOLE) {
      System.err.println(entry);
    }
  }

  void exception(Throwable t) {
    final LogEntry entry;
    StringWriter writer = null;
    try {
      writer = new StringWriter();
      t.printStackTrace(new PrintWriter(writer));
      entry = new LogEntry(Level.WARNING, System.currentTimeMillis(), writer.toString());
      synchronized (entries) {
        entries.add(entry);
        if (entries.size() > MAX_LOGS) {
          entries.removeFirst();
        }
      }
    } catch (Throwable t2) {
      if (CONSOLE) {
        System.err.println("While logging a message, an error occurred: " + t2.getMessage());
      }
      return;
    } finally {
      Util.close(writer);
    }
    if (CONSOLE) {
      System.err.println(entry);
    }
  }

  @Override
  public LogEntries get(String s) {
    synchronized (entries) {
      LogEntries logEntries = new LogEntries(entries);
      entries.clear();
      return logEntries;
    }
  }

  @Override
  public Set<String> getAvailableLogTypes() {
    return new HashSet<String>(Arrays.asList(new String[] { "all" }));
  }
}
