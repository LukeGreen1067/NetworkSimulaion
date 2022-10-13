import java.util.zip.CRC32;
import java.util.zip.Checksum;

class Receiver extends TransportLayer {


    NetworkSimulator simulator;
    private int seqnum;
    private int acknum;
    private String name;
    private int expectedSeq;
    private int Last;

    public Receiver(String n, NetworkSimulator sim) {
        super(n,sim);
        simulator = sim;
        name=n;
    }

    public static int genchecksum(byte[] bytes) {

        int crc = 0xFFFF;
        int val = 0;

        int len = bytes.length;

        for(int pos = 0; pos< len; pos++){
            crc ^=(int)(0x00ff & bytes[pos]);
            for(int i = 8; i != 0; i--){
                if((crc & 0x0001) != 0){
                    crc >>=1;   
                    crc ^= 0xA001;
                }
                else
                    crc >>=1;
            }
        }
        val = (crc & 0xff) << 8;
        val = val +((crc >> 8) & 0xff);

        return val;
    }

    public void init() {

        seqnum=0;
        acknum=0;
        expectedSeq = 1;
        Last = 0;
    }

    //send a packet from layer5 to layer3 - not used for one way sending - only used in bidirectional
    public void rdt_send(byte[] data) {

    }


    //send packet from layer3 to layer5 and also send an ACK back to sender
    @Override
    public void rdt_receive(TransportLayerPacket pkt) {

        byte[] data = pkt.getData();
        int checksum = genchecksum(data);
        int checksuminpkt = pkt.getchecksum();
        if ((checksum==checksuminpkt) && expectedSeq == pkt.getSeqnum()) {
            System.out.format(" Receiver-> Packet Received: " + new String(data));
            simulator.sendToApplicationLayer(this, data);
            byte[] ackdata = "ACK".getBytes();
            int checksumack = genchecksum(ackdata);
            System.out.println("Receiver-> ack: "+expectedSeq+" being sent");
            TransportLayerPacket ackpkt = new TransportLayerPacket(ackdata, expectedSeq, checksumack);
            simulator.sendToNetworkLayer(this, ackpkt);
            expectedSeq++;
            Last++;
        } else {
            System.out.format(" Receiver-> corrupted/lost pkt\n" + "Last ack: " + Last);
            byte[] ackdata = "ACK".getBytes();
            int checksumack = genchecksum(ackdata);
            TransportLayerPacket ackpkt = new TransportLayerPacket(ackdata, Last, checksumack);
            simulator.sendToNetworkLayer(this, ackpkt);
        }
    }

    public void timerInterrupt() {

    }

}
