package org.pcap4j.test.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IcmpV6CommonPacket;
import org.pcap4j.packet.IcmpV6DestinationUnreachablePacket;
import org.pcap4j.packet.IcmpV6DestinationUnreachablePacket.IcmpV6DestinationUnreachableHeader;
import org.pcap4j.packet.IcmpV6EchoRequestPacket;
import org.pcap4j.packet.IcmpV6EchoRequestPacket.Builder;
import org.pcap4j.packet.IllegalPacket;
import org.pcap4j.packet.IllegalRawDataException;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.IpV6SimpleFlowLabel;
import org.pcap4j.packet.IpV6SimpleTrafficClass;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.SimpleBuilder;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IcmpV6Code;
import org.pcap4j.packet.namednumber.IcmpV6Type;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("javadoc")
public class IcmpV6DestinationUnreachablePacketTest extends AbstractPacketTest {

  private static final Logger logger =
      LoggerFactory.getLogger(IcmpV6DestinationUnreachablePacketTest.class);

  private final IcmpV6DestinationUnreachablePacket packet;
  private final int unused;

  public IcmpV6DestinationUnreachablePacketTest() {
    this.unused = 12345;

    Builder echob = new Builder();
    echob
        .identifier((short) 100)
        .sequenceNumber((short) 10)
        .payloadBuilder(
            new UnknownPacket.Builder().rawData((new byte[] {(byte) 0, (byte) 1, (byte) 2})));

    Inet6Address srcAddr;
    Inet6Address dstAddr;
    try {
      srcAddr = (Inet6Address) InetAddress.getByName("2001:db8::3:2:1");
      dstAddr = (Inet6Address) InetAddress.getByName("2001:db8::3:2:2");
    } catch (UnknownHostException e) {
      throw new AssertionError();
    }
    IcmpV6CommonPacket.Builder icmpV6b = new IcmpV6CommonPacket.Builder();
    icmpV6b
        .type(IcmpV6Type.ECHO_REQUEST)
        .code(IcmpV6Code.NO_CODE)
        .srcAddr(srcAddr)
        .dstAddr(dstAddr)
        .payloadBuilder(echob)
        .correctChecksumAtBuild(true);

    IpV6Packet.Builder ipv6b = new IpV6Packet.Builder();
    ipv6b
        .version(IpVersion.IPV6)
        .trafficClass(IpV6SimpleTrafficClass.newInstance((byte) 0x12))
        .flowLabel(IpV6SimpleFlowLabel.newInstance(0x12345))
        .nextHeader(IpNumber.ICMPV6)
        .hopLimit((byte) 100)
        .srcAddr(srcAddr)
        .dstAddr(dstAddr)
        .correctLengthAtBuild(true)
        .payloadBuilder(icmpV6b);

    IcmpV6DestinationUnreachablePacket.Builder b = new IcmpV6DestinationUnreachablePacket.Builder();
    b.unused(unused).payload(ipv6b.build());
    this.packet = b.build();
  }

  @Override
  protected Packet getPacket() {
    return packet;
  }

  @Override
  protected Packet getWholePacket() {
    Inet6Address srcAddr;
    Inet6Address dstAddr;
    try {
      srcAddr = (Inet6Address) InetAddress.getByName("2001:db8::3:2:2");
      dstAddr = (Inet6Address) InetAddress.getByName("2001:db8::3:2:1");
    } catch (UnknownHostException e) {
      throw new AssertionError();
    }
    IcmpV6CommonPacket.Builder icmpV6b = new IcmpV6CommonPacket.Builder();
    icmpV6b
        .type(IcmpV6Type.DESTINATION_UNREACHABLE)
        .code(IcmpV6Code.PORT_UNREACHABLE)
        .srcAddr(srcAddr)
        .dstAddr(dstAddr)
        .payloadBuilder(new SimpleBuilder(packet))
        .correctChecksumAtBuild(true);

    IpV6Packet.Builder ipv6b = new IpV6Packet.Builder();
    ipv6b
        .version(IpVersion.IPV6)
        .trafficClass(IpV6SimpleTrafficClass.newInstance((byte) 0x12))
        .flowLabel(IpV6SimpleFlowLabel.newInstance(0x12345))
        .nextHeader(IpNumber.ICMPV6)
        .hopLimit((byte) 100)
        .srcAddr(srcAddr)
        .dstAddr(dstAddr)
        .correctLengthAtBuild(true)
        .payloadBuilder(icmpV6b);

    EthernetPacket.Builder eb = new EthernetPacket.Builder();
    eb.dstAddr(MacAddress.getByName("fe:00:00:00:00:02"))
        .srcAddr(MacAddress.getByName("fe:00:00:00:00:01"))
        .type(EtherType.IPV6)
        .payloadBuilder(ipv6b)
        .paddingAtBuild(true);
    return eb.build();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    logger.info(
        "########## "
            + IcmpV6DestinationUnreachablePacketTest.class.getSimpleName()
            + " START ##########");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Test
  public void testNewPacket() {
    IcmpV6DestinationUnreachablePacket p;
    try {
      p =
          IcmpV6DestinationUnreachablePacket.newPacket(
              packet.getRawData(), 0, packet.getRawData().length);
    } catch (IllegalRawDataException e) {
      throw new AssertionError(e);
    }
    assertEquals(packet, p);

    assertTrue(p.getPayload().contains(IpV6Packet.class));
    assertTrue(p.getPayload().contains(IcmpV6CommonPacket.class));
    assertTrue(p.getPayload().contains(IcmpV6EchoRequestPacket.class));
    assertTrue(p.getPayload().contains(UnknownPacket.class));
    assertEquals(p.getPayload().get(UnknownPacket.class).length(), 3);
    assertFalse(p.getPayload().contains(IllegalPacket.class));
  }

  @Test
  public void testGetHeader() {
    IcmpV6DestinationUnreachableHeader h = packet.getHeader();
    assertEquals(unused, h.getUnused());
  }
}
