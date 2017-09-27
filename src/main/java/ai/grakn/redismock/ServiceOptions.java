package ai.grakn.redismock;

/**
 * Created by Xiaolu on 2015/4/22.
 */
public class ServiceOptions {

    private int closeSocketAfterSeveralCommands = 0;

    public ServiceOptions() {}

    public void setCloseSocketAfterSeveralCommands(int count) {
        this.closeSocketAfterSeveralCommands = count;
    }

    public int getCloseSocketAfterSeveralCommands() {
        return closeSocketAfterSeveralCommands;
    }
}
