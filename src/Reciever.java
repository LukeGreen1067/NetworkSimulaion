

import java.util.zip.CRC32;
import java.util.zip.Checksum;

class Receiver extends TransportLayer {


    NetworkSimulator simulator;
    private int seqnum;
    private int acknum;
    private String name;

    public Receiver(String n, NetworkSimulator sim) {
        super(n,sim);
        simulator = sim;
        name=n;
    }

    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public void init() {

        seqnum=0;
        acknum=0;
    }

    //send a packet from layer5 to layer3 - not used for one way sending - only used in bidirectional
    public void rdt_send(byte[] data) {
/*
        seqnum++;
        TransportLayerPacket newpkt = new TransportLayerPacket(data, seqnum, acknum, checksum);
        simulator.sendToNetworkLayer(this, newpkt);
   */
    }

    //send packet from layer3 to layer5 and also send an ACK back to sender
    public void rdt_receive(TransportLayerPacket pkt) {


        byte[] data = pkt.getData();
        long checksum = getCRC32Checksum(data);
        long checksuminpkt= pkt.getchecksum();
        System.out.format("checksum in receive ->"+checksum+" checksiumnpck->"+checksuminpkt+" seq->"+pkt.getSeqnum()+"\n");
        if ((checksum!=checksuminpkt) || (pkt.getSeqnum() < 0)) {
            // error packet is corrupted. do not send  ack

            System.out.format("Receiver->corrupted pkt\n");
        } else {

            simulator.sendToApplicationLayer(this, data);

            int acknum = pkt.getSeqnum();
            pkt.setAcknum(acknum);
            simulator.sendToNetworkLayer(this, pkt);
        }
    }

    public void timerInterrupt() {

    }
}
