public class TransportLayerPacket {

    // Maybe remove these
    // You may need extra fields
    private int seqnum;
    private int acknum;
    long checksum;
    byte[] data;

    // You may need extra methods

    public TransportLayerPacket(TransportLayerPacket pkt) {
        // constructor that creates a copy of passed pkt
       
            this.seqnum = pkt.getSeqnum();
            this.acknum = pkt.getAcknum();
            this.data = pkt.getData().clone();
            this.checksum = pkt.getchecksum();

    }

    public TransportLayerPacket(byte[] msg, int seq, int ack, long checksum) {
        // constructor that creates a new pkt
       
            this.seqnum = seq;
            this.acknum = ack;
            this.data = msg;
            this.checksum = checksum;

    }

    public void setSeqnum(int seqnum) {
        this.seqnum = seqnum;
    }

    public void setAcknum(int acknum) {
        this.acknum = acknum;
    }

     public int getSeqnum() {
        return seqnum;
    }

    public int getAcknum() {
        return acknum;
    }

    public byte[] getData() {
        return data;
    }

    public long getchecksum(){return checksum;}

}
