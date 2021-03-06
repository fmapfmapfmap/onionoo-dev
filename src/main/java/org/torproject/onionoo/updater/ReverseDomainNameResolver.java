/* Copyright 2013 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo.updater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torproject.onionoo.util.FormattingUtils;
import org.torproject.onionoo.util.Time;
import org.torproject.onionoo.util.TimeFactory;

public class ReverseDomainNameResolver {

  Time time;

  public ReverseDomainNameResolver() {
    this.time = TimeFactory.getTime();
  }

  static final long RDNS_LOOKUP_MAX_REQUEST_MILLIS = 10L * 1000L;
  static final long RDNS_LOOKUP_MAX_DURATION_MILLIS = 5L * 60L * 1000L;
  private static final long RDNS_LOOKUP_MAX_AGE_MILLIS =
      12L * 60L * 60L * 1000L;
  private static final int RDNS_LOOKUP_WORKERS_NUM = 5;

  private Map<String, Long> addressLastLookupTimes;

  Set<String> rdnsLookupJobs;

  Map<String, String> rdnsLookupResults;

  List<Long> rdnsLookupMillis;

  long startedRdnsLookups;

  private List<RdnsLookupWorker> rdnsLookupWorkers;

  public void setAddresses(Map<String, Long> addressLastLookupTimes) {
    this.addressLastLookupTimes = addressLastLookupTimes;
  }

  public void startReverseDomainNameLookups() {
    this.startedRdnsLookups = this.time.currentTimeMillis();
    this.rdnsLookupJobs = new HashSet<String>();
    for (Map.Entry<String, Long> e :
        this.addressLastLookupTimes.entrySet()) {
      if (e.getValue() < this.startedRdnsLookups
          - RDNS_LOOKUP_MAX_AGE_MILLIS) {
        this.rdnsLookupJobs.add(e.getKey());
      }
    }
    this.rdnsLookupResults = new HashMap<String, String>();
    this.rdnsLookupMillis = new ArrayList<Long>();
    this.rdnsLookupWorkers = new ArrayList<RdnsLookupWorker>();
    for (int i = 0; i < RDNS_LOOKUP_WORKERS_NUM; i++) {
      RdnsLookupWorker rdnsLookupWorker = new RdnsLookupWorker(this);
      this.rdnsLookupWorkers.add(rdnsLookupWorker);
      rdnsLookupWorker.setDaemon(true);
      rdnsLookupWorker.start();
    }
  }

  public void finishReverseDomainNameLookups() {
    for (RdnsLookupWorker rdnsLookupWorker : this.rdnsLookupWorkers) {
      try {
        rdnsLookupWorker.join();
      } catch (InterruptedException e) {
        /* This is not something that we can take care of.  Just leave the
         * worker thread alone. */
      }
    }
  }

  public Map<String, String> getLookupResults() {
    synchronized (this.rdnsLookupResults) {
      return new HashMap<String, String>(this.rdnsLookupResults);
    }
  }

  public long getLookupStartMillis() {
    return this.startedRdnsLookups;
  }

  public String getStatsString() {
    StringBuilder sb = new StringBuilder();
    sb.append("    " + FormattingUtils.formatDecimalNumber(
        rdnsLookupMillis.size()) + " lookups performed\n");
    if (rdnsLookupMillis.size() > 0) {
      Collections.sort(rdnsLookupMillis);
      sb.append("    " + FormattingUtils.formatMillis(
          rdnsLookupMillis.get(0)) + " minimum lookup time\n");
      sb.append("    " + FormattingUtils.formatMillis(
          rdnsLookupMillis.get(rdnsLookupMillis.size() / 2))
          + " median lookup time\n");
      sb.append("    " + FormattingUtils.formatMillis(
          rdnsLookupMillis.get(rdnsLookupMillis.size() - 1))
          + " maximum lookup time\n");
    }
    return sb.toString();
  }
}

