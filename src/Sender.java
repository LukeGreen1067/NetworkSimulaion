import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Sender extends TransportLayer {

    NetworkSimulator simulator;
    private int seqnum;
    private int acknum;
    private String name;
    TransportLayerPacket pkttosend;
    private boolean readyToSend = true;
    private ArrayList<byte[]> queue;
    private List<TransportLayerPacket> window;
    private List<TransportLayerPacket> buffer;
    private int i;
    private int base;
    private int nextSeqNum;
    private int expectedACK;

    public Sender(String n, NetworkSimulator sim) {
        super(n,sim);
        simulator = sim;
        name=n;
    }

    private byte[] takeFromQueue(){
        if(queue.size() > 0) {
            byte nextData[] = queue.get(0);
            queue.remove(0);
            return nextData;
        }
        return null;
    }


    public static int genchecksum(byte[] buf) {

        int crc =  0xFFFF;
        int val = 0;

        int len=buf.length;

        for (int pos = 0; pos < len; pos++) {
            crc ^= (int)(0x00ff & buf[pos]);
            for (int i = 8; i != 0; i--) {    // Loop over each bit
                if ((crc & 0x0001) != 0) {      // If the LSB is set
                    crc >>= 1;                    // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                }
                else                            // Else LSB is not set
                    crc >>= 1;                    // Just shift right
            }
        }

        val =  (crc & 0xff) << 8;
        val =  val + ((crc >> 8) & 0xff);

        return val;

    }

    public void init() {
        queue = new ArrayList<>();
        buffer = new ArrayList<>();
        window = new ArrayList<>();

        expectedACK = 1;
        i = 3;
        base = 1;
        nextSeqNum = 1;
        seqnum=0;
        acknum=1;
    }

    //send a packet from layer5 to layer3
    public void rdt_send(byte[] data) {

        if (nextSeqNum < base + i) {

            if(queue.size() > 0) {
                queue.add(data);
                data = queue.get(0);
                queue.remove(0);
            }
            seqnum++;
            //if (seqnum==2) seqnum=0;
            int checksum = genchecksum(data);
            pkttosend = new TransportLayerPacket(data, seqnum, acknum, checksum);
            System.out.println("Sender --> Sending packet with msg:"+new String(data));
            window.add(pkttosend);
            simulator.sendToNetworkLayer(this, pkttosend);
            if(base == nextSeqNum){
                simulator.startTimer(this,100.0);
            }
            nextSeqNum++;
        }
        else {
            seqnum++;
            System.out.println("Storing in buffer...");
            queue.add(data);
            int checksum = genchecksum(data);
            pkttosend = new TransportLayerPacket(data, seqnum, acknum, checksum);
            nextSeqNum++;
            buffer.add(pkttosend);
        }


    }


    //since we are simulating one way sending the sender will only get ACK packets
    public void rdt_receive(TransportLayerPacket pkt) {
        byte[] data = pkt.getData();
        int checksum = genchecksum(data);
        int checksuminpkt = pkt.getchecksum();

        if ((checksum == checksuminpkt) && (pkt.getAcknum() >= expectedACK)) { // could be wrong with acknum
            expectedACK = pkt.getAcknum() + 1;
            System.out.println("Sender received ACK: " + pkt.getAcknum());
            base = pkt.getAcknum() + 1;
            if(base == nextSeqNum){
                simulator.stopTimer(this);
            }
            else{
                window.remove(0);
                simulator.stopTimer(this);
                simulator.startTimer(this, 100.0);
            }
            if(buffer.size() > 0){
                TransportLayerPacket p = buffer.get(0);
                window.add(p);
                buffer.remove(0);
                simulator.sendToNetworkLayer(this,p);
                System.out.println("Packet sent from buffer: " + new String(p.getData()));
            }
        }
        else {
            System.out.println("Wait for timeout packet corruption detected");
        }
    }

    public void timerInterrupt() {
        System.out.format("Sender-> Timer expired resending packets in window\n");
        simulator.startTimer(this,100.0);
        for (TransportLayerPacket pkt:window) {
            System.out.format("Sender-> Sending: " + new String(pkt.getData()));
            simulator.sendToNetworkLayer(this, pkt);
        }


    }

}
