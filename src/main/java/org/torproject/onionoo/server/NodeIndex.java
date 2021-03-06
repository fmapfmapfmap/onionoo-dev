package org.torproject.onionoo.server;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;

import org.torproject.onionoo.docs.SummaryDocument;

class NodeIndex {

  private String relaysPublishedString;
  public void setRelaysPublishedMillis(long relaysPublishedMillis) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.relaysPublishedString =
        dateTimeFormat.format(relaysPublishedMillis);
  }
  public String getRelaysPublishedString() {
    return relaysPublishedString;
  }

  private String bridgesPublishedString;
  public void setBridgesPublishedMillis(long bridgesPublishedMillis) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.bridgesPublishedString =
        dateTimeFormat.format(bridgesPublishedMillis);
  }
  public String getBridgesPublishedString() {
    return bridgesPublishedString;
  }

  private List<String> relaysByConsensusWeight;
  public void setRelaysByConsensusWeight(
      List<String> relaysByConsensusWeight) {
    this.relaysByConsensusWeight = relaysByConsensusWeight;
  }
  public List<String> getRelaysByConsensusWeight() {
    return relaysByConsensusWeight;
  }


  private Map<String, SummaryDocument> relayFingerprintSummaryLines;
  public void setRelayFingerprintSummaryLines(
      Map<String, SummaryDocument> relayFingerprintSummaryLines) {
    this.relayFingerprintSummaryLines = relayFingerprintSummaryLines;
  }
  public Map<String, SummaryDocument> getRelayFingerprintSummaryLines() {
    return this.relayFingerprintSummaryLines;
  }

  private Map<String, SummaryDocument> bridgeFingerprintSummaryLines;
  public void setBridgeFingerprintSummaryLines(
      Map<String, SummaryDocument> bridgeFingerprintSummaryLines) {
    this.bridgeFingerprintSummaryLines = bridgeFingerprintSummaryLines;
  }
  public Map<String, SummaryDocument> getBridgeFingerprintSummaryLines() {
    return this.bridgeFingerprintSummaryLines;
  }

  private Map<String, Set<String>> relaysByCountryCode = null;
  public void setRelaysByCountryCode(
      Map<String, Set<String>> relaysByCountryCode) {
    this.relaysByCountryCode = relaysByCountryCode;
  }
  public Map<String, Set<String>> getRelaysByCountryCode() {
    return relaysByCountryCode;
  }

  private Map<String, Set<String>> relaysByASNumber = null;
  public void setRelaysByASNumber(
      Map<String, Set<String>> relaysByASNumber) {
    this.relaysByASNumber = relaysByASNumber;
  }
  public Map<String, Set<String>> getRelaysByASNumber() {
    return relaysByASNumber;
  }

  private Map<String, Set<String>> relaysByFlag = null;
  public void setRelaysByFlag(Map<String, Set<String>> relaysByFlag) {
    this.relaysByFlag = relaysByFlag;
  }
  public Map<String, Set<String>> getRelaysByFlag() {
    return relaysByFlag;
  }

  private Map<String, Set<String>> bridgesByFlag = null;
  public void setBridgesByFlag(Map<String, Set<String>> bridgesByFlag) {
    this.bridgesByFlag = bridgesByFlag;
  }
  public Map<String, Set<String>> getBridgesByFlag() {
    return bridgesByFlag;
  }

  private Map<String, Set<String>> relaysByContact = null;
  public void setRelaysByContact(
      Map<String, Set<String>> relaysByContact) {
    this.relaysByContact = relaysByContact;
  }
  public Map<String, Set<String>> getRelaysByContact() {
    return relaysByContact;
  }

  private Map<String, Set<String>> relaysByFamily = null;
  public void setRelaysByFamily(Map<String, Set<String>> relaysByFamily) {
    this.relaysByFamily = relaysByFamily;
  }
  public Map<String, Set<String>> getRelaysByFamily() {
    return this.relaysByFamily;
  }

  private SortedMap<Integer, Set<String>> relaysByFirstSeenDays;
  public void setRelaysByFirstSeenDays(
      SortedMap<Integer, Set<String>> relaysByFirstSeenDays) {
    this.relaysByFirstSeenDays = relaysByFirstSeenDays;
  }
  public SortedMap<Integer, Set<String>> getRelaysByFirstSeenDays() {
    return relaysByFirstSeenDays;
  }

  private SortedMap<Integer, Set<String>> bridgesByFirstSeenDays;
  public void setBridgesByFirstSeenDays(
      SortedMap<Integer, Set<String>> bridgesByFirstSeenDays) {
    this.bridgesByFirstSeenDays = bridgesByFirstSeenDays;
  }
  public SortedMap<Integer, Set<String>> getBridgesByFirstSeenDays() {
    return bridgesByFirstSeenDays;
  }

  private SortedMap<Integer, Set<String>> relaysByLastSeenDays;
  public void setRelaysByLastSeenDays(
      SortedMap<Integer, Set<String>> relaysByLastSeenDays) {
    this.relaysByLastSeenDays = relaysByLastSeenDays;
  }
  public SortedMap<Integer, Set<String>> getRelaysByLastSeenDays() {
    return relaysByLastSeenDays;
  }

  private SortedMap<Integer, Set<String>> bridgesByLastSeenDays;
  public void setBridgesByLastSeenDays(
      SortedMap<Integer, Set<String>> bridgesByLastSeenDays) {
    this.bridgesByLastSeenDays = bridgesByLastSeenDays;
  }
  public SortedMap<Integer, Set<String>> getBridgesByLastSeenDays() {
    return bridgesByLastSeenDays;
  }
}