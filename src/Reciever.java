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



    public void init() {

        seqnum=0;
        acknum=0;
    }

    //send a packet from layer5 to layer3 - not used for one way sending
    public void rdt_send(byte[] data) {

        seqnum++;
        TransportLayerPacket newpkt = new TransportLayerPacket(data, seqnum, acknum);
        simulator.sendToNetworkLayer(this, newpkt);
    }

    //send packet from layer3 to layer5 and also send an ACK back to sender
    public void rdt_receive(TransportLayerPacket pkt) {

        byte[] data = pkt.getData();
        simulator.sendToApplicationLayer(this,data);

        int acknum=pkt.getSeqnum();
        pkt.setAcknum(acknum);
        simulator.sendToNetworkLayer(this,pkt);
    }

    public void timerInterrupt() {

    }
}
