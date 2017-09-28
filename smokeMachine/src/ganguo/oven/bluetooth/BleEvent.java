package ganguo.oven.bluetooth;

/**
 * Created by Tony on 1/18/15.
 */
public class BleEvent {
    private BleCommand command;
    private Object target;

    public BleEvent(BleCommand command) {
        this.command = command;
    }

    public BleEvent(BleCommand command, Object target) {
        this.command = command;
        this.target = target;
    }

    public BleCommand getCommand() {
        return command;
    }

    public void setCommand(BleCommand command) {
        this.command = command;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}
