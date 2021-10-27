public class Main {

    public static void main(String[] args) {
        NetworkSimulator sim = new NetworkSimulator(5, 0.0, 0.0, 200.0, false, 2);

        Sender sender = new Sender("sender", sim);
        // TODO: Set the sender   (sim.setSender)
        sim.setSender(sender);
        // TODO: Set the receiver (sim.setReceiver)
        Receiver receiver = new Receiver("receiver",sim);
        sim.setReceiver(receiver);

        sim.runSimulation();
    }

}
