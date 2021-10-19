public abstract class TransportLayer {

    String name;
    NetworkSimulator simulator;

    public TransportLayer(String name, NetworkSimulator simulator) {
        this.name = name;
        this.simulator = simulator;
    }

    public abstract void init();

    public abstract void rdt_send(byte[] data);

    public abstract void rdt_receive(TransportLayerPacket pkt);

    public abstract void timerInterrupt();

    public String getName() {
        return this.name;
    }
}
