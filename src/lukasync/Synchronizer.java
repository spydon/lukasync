package lukasync;

public class Synchronizer {
    private MetaConnection remote;
    private Rest local;

    public Synchronizer(MetaConnection remote, Rest local) {
        this.remote = remote;
        this.local = local;
        POS pos = new POS(remote);
        pos.getContacts();
        try {
            System.out.println("Syncronizing");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
