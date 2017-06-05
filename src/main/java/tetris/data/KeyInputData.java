package tetris.data;

import tetris.play.KeyAction;

/**
 * Created by hkoba on 2016/09/24.
 */
public class KeyInputData {
    public final KeyAction MOVE_LEFT = new KeyAction();
    public final KeyAction MOVE_RIGHT = new KeyAction();
    public final KeyAction MOVE_DROP = new KeyAction();
    public final KeyAction HARD_DROP = new KeyAction();
    public final KeyAction TURN_LEFT = new KeyAction();
    public final KeyAction TURN_RIGHT = new KeyAction();
    public final KeyAction HOLD_BUTTON = new KeyAction();
    public final KeyAction START_BUTTON = new KeyAction();

    public void nextFrame() {
        MOVE_LEFT.nextFrame();
        MOVE_RIGHT.nextFrame();
        MOVE_DROP.nextFrame();
        HARD_DROP.nextFrame();
        TURN_LEFT.nextFrame();
        TURN_RIGHT.nextFrame();
        HOLD_BUTTON.nextFrame();
        START_BUTTON.nextFrame();
    }
}
