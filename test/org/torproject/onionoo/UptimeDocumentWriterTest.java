/* Copyright 2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.torproject.onionoo.docs.GraphHistory;
import org.torproject.onionoo.docs.UptimeDocument;
import org.torproject.onionoo.docs.UptimeStatus;
import org.torproject.onionoo.updater.DescriptorType;
import org.torproject.onionoo.util.ApplicationFactory;
import org.torproject.onionoo.util.DateTimeHelper;
import org.torproject.onionoo.writer.UptimeDocumentWriter;

public class UptimeDocumentWriterTest {

  private static final long TEST_TIME = DateTimeHelper.parse(
      "2014-03-23 12:00:00");

  private DummyTime dummyTime;

  @Before
  public void createDummyTime() {
    this.dummyTime = new DummyTime(TEST_TIME);
    ApplicationFactory.setTime(this.dummyTime);
  }

  private DummyDescriptorSource descriptorSource;

  @Before
  public void createDummyDescriptorSource() {
    this.descriptorSource = new DummyDescriptorSource();
    ApplicationFactory.setDescriptorSource(this.descriptorSource);
  }

  private DummyDocumentStore documentStore;

  @Before
  public void createDummyDocumentStore() {
    this.documentStore = new DummyDocumentStore();
    ApplicationFactory.setDocumentStore(this.documentStore);
  }

  @Test
  public void testNoStatuses() {
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    writer.writeDocuments();
    assertEquals("Without providing any data, nothing should be written "
        + "to disk.", 0,
        this.documentStore.getPerformedStoreOperations());
  }

  private static final String ALL_RELAYS_FINGERPRINT = null;

  private static final String GABELMOO_FINGERPRINT =
      "F2044413DAC2E02E3D6BCF4735A19BCA1DE97281";

  private void addStatusOneWeekSample(String allRelaysUptime,
      String gabelmooUptime) {
    UptimeStatus status = new UptimeStatus();
    status.fromDocumentString(allRelaysUptime);
    this.documentStore.addDocument(status, ALL_RELAYS_FINGERPRINT);
    status = new UptimeStatus();
    status.fromDocumentString(gabelmooUptime);
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    this.descriptorSource.addFingerprint(DescriptorType.RELAY_CONSENSUSES,
        GABELMOO_FINGERPRINT);
  }

  private void assertOneWeekGraph(UptimeDocument document, int graphs,
      String first, String last, int count, List<Integer> values) {
    this.assertGraph(document, graphs, "1_week", first, last,
        (int) (DateTimeHelper.ONE_HOUR / DateTimeHelper.ONE_SECOND),
        count, values);
  }

  private void assertOneMonthGraph(UptimeDocument document, int graphs,
      String first, String last, int count, List<Integer> values) {
    this.assertGraph(document, graphs, "1_month", first, last,
        (int) (DateTimeHelper.FOUR_HOURS / DateTimeHelper.ONE_SECOND),
        count, values);
  }

  private void assertGraph(UptimeDocument document, int graphs,
      String graphName, String first, String last, int interval,
      int count, List<Integer> values) {
    assertEquals("Should contain exactly " + graphs + " graphs.", graphs,
        document.getUptime().size());
    assertTrue("Should contain a graph for " + graphName + ".",
        document.getUptime().containsKey(graphName));
    GraphHistory history = document.getUptime().get(graphName);
    assertEquals("First data point should be " + first + ".", first,
        history.getFirst());
    assertEquals("Last data point should be " + last + ".", last,
        history.getLast());
    assertEquals("Interval should be " + interval + " seconds.", interval,
        (int) history.getInterval());
    assertEquals("Factor should be 1.0 / 999.0.", 1.0 / 999.0,
        (double) history.getFactor(), 0.01);
    assertEquals("There should be one data point per hour.", count,
        (int) history.getCount());
    assertEquals("Count should be the same as the number of values.",
        count, history.getValues().size());
    if (values == null) {
      for (int value : history.getValues()) {
        assertEquals("All values should be 999.", 999, value);
      }
    } else {
      assertEquals("Values are not as expected.", values,
          history.getValues());
    }
  }

  @Test
  public void testOneHourUptime() {
    this.addStatusOneWeekSample("r 2014-03-23-11 1\n",
        "r 2014-03-23-11 1\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    assertEquals("Should not contain any graph.", 0,
        document.getUptime().size());
  }

  @Test
  public void testTwoHoursUptime() {
    this.addStatusOneWeekSample("r 2014-03-23-10 2\n",
        "r 2014-03-23-10 2\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneWeekGraph(document, 1, "2014-03-23 10:30:00",
        "2014-03-23 11:30:00", 2, null);
  }

  @Test
  public void testTwoHoursUptimeSeparatedByNull() {
    this.addStatusOneWeekSample("r 2014-03-23-09 1\nr 2014-03-23-11 1\n",
        "r 2014-03-23-09 1\nr 2014-03-23-11 1\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    assertEquals("Should not contain any graph.", 0,
        document.getUptime().size());
  }

  @Test
  public void testTwoHoursUptimeSeparatedByZero() {
    this.addStatusOneWeekSample("r 2014-03-23-09 3\n",
        "r 2014-03-23-09 1\nr 2014-03-23-11 1\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneWeekGraph(document, 1, "2014-03-23 09:30:00",
        "2014-03-23 11:30:00", 3,
        Arrays.asList(new Integer[] { 999, 0, 999 }));
  }

  @Test
  public void testTwoHoursUptimeThenDowntime() {
    this.addStatusOneWeekSample("r 2014-03-23-09 3\n",
        "r 2014-03-23-09 2\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneWeekGraph(document, 1, "2014-03-23 09:30:00",
        "2014-03-23 11:30:00", 3,
        Arrays.asList(new Integer[] { 999, 999, 0 }));
  }

  @Test
  public void testOneWeekUptime() {
    this.addStatusOneWeekSample("r 2014-03-16-12 168\n",
        "r 2014-03-16-12 168\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneWeekGraph(document, 1, "2014-03-16 12:30:00",
        "2014-03-23 11:30:00", 168, null);
  }

  @Test
  public void testOneWeekOneHourUptime() {
    this.addStatusOneWeekSample("r 2014-03-16-11 169\n",
        "r 2014-03-16-11 169\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneWeekGraph(document, 2, "2014-03-16 12:30:00",
        "2014-03-23 11:30:00", 168, null);
    this.assertOneMonthGraph(document, 2, "2014-03-16 10:00:00",
        "2014-03-23 10:00:00", 43, null);
  }

  @Test
  public void testOneMonthPartialIntervalOnline() {
    this.addStatusOneWeekSample("r 2014-03-16-08 8\n",
        "r 2014-03-16-11 5\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneMonthGraph(document, 2, "2014-03-16 10:00:00",
        "2014-03-16 14:00:00", 2, null);
  }

  @Test
  public void testOneMonthPartialIntervalOnOff() {
    this.addStatusOneWeekSample("r 2014-03-16-08 8\n",
        "r 2014-03-16-10 1\nr 2014-03-16-12 1\n");
    UptimeDocumentWriter writer = new UptimeDocumentWriter();
    ApplicationFactory.getDescriptorSource().readDescriptors();
    writer.writeDocuments();
    assertEquals("Should write exactly one document.", 1,
        this.documentStore.getPerformedStoreOperations());
    UptimeDocument document = this.documentStore.getDocument(
        UptimeDocument.class, GABELMOO_FINGERPRINT);
    this.assertOneMonthGraph(document, 2, "2014-03-16 10:00:00",
        "2014-03-16 14:00:00", 2,
        Arrays.asList(new Integer[] { 499, 249 }));
  }
}

