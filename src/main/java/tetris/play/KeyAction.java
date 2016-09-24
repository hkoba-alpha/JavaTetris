package tetris.play;

import org.newdawn.slick.command.Command;

/**
 * Created by hkoba on 2016/08/30.
 */
public enum KeyAction implements Command {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_DROP,
    HARD_DROP,
    BUTTON_HOLD,
    TURN_RIGHT,
    TURN_LEFT;

    private int count;
    private boolean pushFlag;

    public int getCount() {
        return count;
    }
    public boolean isPush() {
        return pushFlag;
    }

    public void event(boolean push) {
        pushFlag = push;
        count = 0;
    }

    public void nextFrame() {
        if (pushFlag) {
            count++;
        }
    }

    public void resetCount() {
        count = 0;
    }
}
