public class TransportLayerPacket {

    // Maybe remove these
    // You may need extra fields
    private int seqnum;
    private int acknum;

    byte[] data;

    // You may need extra methods

    public TransportLayerPacket(TransportLayerPacket pkt) {
        // complete this method
    }

    public void setSeqnum(int seqnum) {
        this.seqnum = seqnum;
    }

    public void setAcknum(int acknum) {
        this.acknum = acknum;
    }

    public byte[] getData() {
        return data;
    }

}
