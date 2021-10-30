/*
import java.util.zip.CRC32;
import java.util.zip.Checksum;
*/

class Sender extends TransportLayer {

    NetworkSimulator simulator;
    private int seqnum;
    private int acknum;
    private String name;
    TransportLayerPacket pkttosend;
int counter=0;
    public Sender(String n, NetworkSimulator sim) {
        super(n,sim);
        simulator = sim;
        name=n;
    }
/*   public static long Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
       crc32.update(bytes, 0, bytes.length);
       return crc32.getValue();
   }*/
public static int Checksum(byte[] bytes) {

    int crc = 0xFFFF;
    int val = 0;

    int len = bytes.length;

    for(int pos = 0; pos< len; pos++){
        crc ^=(int)(0x00ff & bytes[pos]);
        for(int i = 8; i != 0; i--){ // loop over each bit
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
    }

    //send a packet from layer5 to layer3
    public void rdt_send(byte[] data) {

        if (seqnum==acknum) {//if all have been ack then we can send next packet from application
            seqnum++;
         /*   long checksum = Checksum(data);*/
            /*byte[] checksum =  Checksum(data);*/
            int checksum =  Checksum(data);
            pkttosend = new TransportLayerPacket(data, seqnum, acknum, checksum);
            System.out.format(" data->"+data +" checksum->"+ pkttosend.getchecksum()+" seq->"+pkttosend.getSeqnum()+"\n");
            simulator.startTimer(this,100.0);
            simulator.sendToNetworkLayer(this, pkttosend);
        } else {
            System.out.format("sender-> got a layer5 packet to send before all previously sent have been ack.\n");
            System.out.format("sender-> pkt will never be sent.\n");
            //System.exit(0);
        }


    }


    //since we are simulating one way sending the sender will only get ACK packets
    public void rdt_receive(TransportLayerPacket pkt) {

        acknum = pkt.getAcknum();
        if (acknum < 0) {
            System.out.format("Sender-> Received corrupted ack. resending\n");
            simulator.stopTimer(this);
            simulator.startTimer(this, 100.0);
            simulator.sendToNetworkLayer(this, pkttosend);
        } else {
            System.out.format("Sender-> Received ack: " + acknum + "\n");
            simulator.stopTimer(this);
        }
    }
    public void timerInterrupt() {
        System.out.format("Sender-> timeOut waiting for ack -> resend pkt with seq="+pkttosend.getSeqnum()+"\n");
        System.out.format("timeout checksum->"+ pkttosend.getchecksum()+" seq->"+pkttosend.getSeqnum()+ " new checksum->"+Checksum(pkttosend.getData())+"\n");
        simulator.startTimer(this,100.0);
        simulator.sendToNetworkLayer(this, pkttosend);
      counter++;
        if (counter>5) System.exit(0);

    }

}
