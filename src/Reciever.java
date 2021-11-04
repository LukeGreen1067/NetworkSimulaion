
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
    }

    //send a packet from layer5 to layer3 - not used for one way sending - only used in bidirectional
    public void rdt_send(byte[] data) {

    }

    //send packet from layer3 to layer5 and also send an ACK back to sender
    public void rdt_receive(TransportLayerPacket pkt) {

        byte[] data = pkt.getData();
        int checksum = genchecksum(data);
        int checksuminpkt = pkt.getchecksum();
        //System.out.format(" checksum in receive ->"+checksum+" checksiumnpck->"+checksuminpkt+" seq->"+pkt.getSeqnum()+ " data->" + data.toString()+"\n");
        if ((checksum!=checksuminpkt) || (pkt.getSeqnum() < 0) ) {
            // error packet is corrupted. do not send  ack

            System.out.format(" Receiver->corrupted pkt\n");
        } else {
            System.out.format(" Receiver-> sending pkt to application layer" + "\n");

            simulator.sendToApplicationLayer(this, data);
            int acknum = pkt.getSeqnum();
            pkt.setAcknum(acknum);
           // System.out.format(" Receiver-> sending feedback pkt to network layer with " +  "ack-> "+ acknum +"\n");

            simulator.sendToNetworkLayer(this, pkt);
        }
    }

    public void timerInterrupt() {

    }
}
