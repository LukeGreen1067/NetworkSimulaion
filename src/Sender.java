import java.util.ArrayList;
import java.util.Arrays;

class Sender extends TransportLayer {

    NetworkSimulator simulator;
    private int seqnum;
    private int acknum;
    private String name;
    TransportLayerPacket pkttosend;
    private boolean readyToSend = true;
    private ArrayList<byte[]> queue;

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
        seqnum=-1;
        acknum=-1;

    }

    //send a packet from layer5 to layer3
    public void rdt_send(byte[] data) {

        if (readyToSend) {

            if(queue.size() > 0) {
                queue.add(data);
                data = queue.get(0);
                queue.remove(0);
            }

            readyToSend = false;
            seqnum++;
            if (seqnum==2) seqnum=0;
            int checksum = genchecksum(data);
            pkttosend = new TransportLayerPacket(data, seqnum, acknum, checksum);
            //  System.out.format("checksum->"+ pkttosend.getchecksum()+" seq->"+pkttosend.getSeqnum()+"\n");
            //simulator.sendToApplicationLayer(this,pkttosend.getData());
            System.out.println("Sender --> Sending packet with seq:"+seqnum);
            simulator.sendToNetworkLayer(this, pkttosend);
            simulator.startTimer(this,100.0);


        } else {
            System.out.println("Sender --> New data from layer5 added to queue - sender in Stop state.");
            queue.add(data);
        }


    }


    //since we are simulating one way sending the sender will only get ACK packets
    public void rdt_receive(TransportLayerPacket pkt) {

        acknum = pkt.getAcknum();
        if (acknum < 0) {
            System.out.format("Sender-> Received corrupted ack. resending packet with seq:"+seqnum + "\n");
            simulator.stopTimer(this);
            simulator.sendToNetworkLayer(this, pkttosend);
            simulator.startTimer(this, 100.0);

        } else {
            readyToSend = true;
            System.out.format("Sender-> Received ack: " + acknum + "\n");
            simulator.stopTimer(this);
            byte[] queueNext = takeFromQueue();
            if (queueNext != null) {
                readyToSend = false;
                seqnum++;
                if (seqnum == 2) seqnum = 0;
                int checksum = genchecksum(queueNext);
                pkttosend = new TransportLayerPacket(queueNext, seqnum, acknum, checksum);
                //  System.out.format("checksum->"+ pkttosend.getchecksum()+" seq->"+pkttosend.getSeqnum()+"\n");
                //simulator.sendToApplicationLayer(this,pkttosend.getData());
                System.out.println("Sender --> Sending packet with seq:" + seqnum);
                simulator.sendToNetworkLayer(this, pkttosend);
                simulator.startTimer(this, 100.0);
            }
        }
    }

    public void timerInterrupt() {
        System.out.format("Sender-> timeOut waiting for ack -> resend pkt with seq="+pkttosend.getSeqnum()+"\n");
        // System.out.format("timeout checksum->"+ pkttosend.getchecksum()+" seq->"+pkttosend.getSeqnum()+" new->"+getCRC32Checksum(pkttosend.getData())+"\n");
        //simulator.sendToApplicationLayer(this,pkttosend.getData());
        simulator.startTimer(this,100.0);
        simulator.sendToNetworkLayer(this, pkttosend);


    }

}
