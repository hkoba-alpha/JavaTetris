package tetris.play;

import org.newdawn.slick.*;
import org.newdawn.slick.command.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hkoba on 2016/09/22.
 */
public class KeyConfigPlay extends BasicGameState implements InputProviderListener {
    private long buttonFlag;

    private Map<KeyAction, Integer> keyMap = new HashMap<>();
    private Map<KeyAction, String> keyNameMap = new HashMap<>();

    /**
     * -1:キーボード
     * 0-:コントローラ番号
     */
    private int inputNumber = -1;

    private enum ConfigMode {
        SELECT_CONTROLLER,
        KEY_INPUT,
        ENTER_WAIT
    }

    private enum KeyConfigData {
        KEY_LEFT("MOVE LEFT ", KeyAction.MOVE_LEFT),
        KEY_RIGHT("MOVE RIGHT", KeyAction.MOVE_RIGHT),
        KEY_DROP("SOFT DROP ", KeyAction.MOVE_DROP),
        KEY_HARD("HARD DROP ", KeyAction.HARD_DROP),
        TURN_LEFT("TURN LEFT ", KeyAction.TURN_LEFT),
        TURN_RIGHT("TURN RIGHT", KeyAction.TURN_RIGHT),
        KEY_HOLD("HOLD      ", KeyAction.BUTTON_HOLD);

        private KeyAction keyAction;
        private String keyName;

        KeyConfigData(String nm, KeyAction action) {
            keyAction = action;
            keyName = nm;
        }

    }

    private ConfigMode configMode;

    private List<Field> inputKeyList = new ArrayList<>();

    public KeyConfigPlay() {
        for (Field field: Input.class.getDeclaredFields()) {
            if (field.getName().startsWith("KEY_")) {
                inputKeyList.add(field);
            }
        }
        configMode = ConfigMode.KEY_INPUT;
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        container.getInput().removeAllListeners();

        InputProvider provider = new InputProvider(container.getInput());
        provider.addListener(this);

        container.getInput().initControllers();

        provider.bindCommand(new KeyControl(Input.KEY_LEFT), KeyAction.MOVE_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_RIGHT), KeyAction.MOVE_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_UP), KeyAction.HARD_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_DOWN), KeyAction.MOVE_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_Z), KeyAction.TURN_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_X), KeyAction.TURN_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_SPACE), KeyAction.BUTTON_HOLD);

        provider.bindCommand(new ControllerButtonControl(0, 11-3), KeyAction.MOVE_LEFT);
        provider.bindCommand(new ControllerButtonControl(0, 9-3), KeyAction.MOVE_RIGHT);
        provider.bindCommand(new ControllerButtonControl(0, 10-3), KeyAction.MOVE_DROP);
        provider.bindCommand(new ControllerButtonControl(0, 8-3), KeyAction.HARD_DROP);
        provider.bindCommand(new ControllerButtonControl(0, 18-3), KeyAction.TURN_LEFT);
        provider.bindCommand(new ControllerButtonControl(0, 17-3), KeyAction.TURN_RIGHT);
        provider.bindCommand(new ControllerButtonControl(0, 15-3), KeyAction.BUTTON_HOLD);

        int cnt = container.getInput().getControllerCount();
        System.out.println("count=" + cnt);
        /*
        cnt = container.getInput().getAxisCount(0);
        System.out.println("axis count=" + cnt);
        for (int i = 0; i < cnt; i++) {
            System.out.println("name[" + i + "]=" + container.getInput().getAxisName(0, 0));
        }
        */
    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        int y = 40;
        g.setColor(Color.white);
        boolean firstFlag = true;
        for (KeyConfigData data: KeyConfigData.values()) {
            Integer code = keyMap.get(data.keyAction);
            String text = "";
            if (code != null) {
                text = keyNameMap.get(data.keyAction);
            } else {
                firstFlag = false;
            }
            g.drawString(data.keyName + " " + text, 50, y);
            y += 32;
        }
        if (firstFlag) {
            g.drawString("[PUSH RETURN KEY]", 50, y);
        }
    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        if (configMode == ConfigMode.ENTER_WAIT) {
            if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
                game.enterState(0);
            } else if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
                configMode = ConfigMode.KEY_INPUT;
                container.getInput().clearKeyPressedRecord();
                keyNameMap.clear();
                keyMap.clear();
            }
        } else if (configMode == ConfigMode.KEY_INPUT) {
            // 入力
            for (Field field: inputKeyList) {
                try {
                    int key = (Integer)field.get(null);
                    if (container.getInput().isKeyPressed(key)) {
                        for (KeyConfigData data: KeyConfigData.values()) {
                            if (!keyMap.containsKey(data.keyAction)) {
                                keyMap.put(data.keyAction, key);
                                keyNameMap.put(data.keyAction, field.getName().substring(4));
                                break;
                            }
                        }
                        if (keyMap.size() == KeyConfigData.values().length) {
                            // 終了
                            configMode = ConfigMode.ENTER_WAIT;
                        }
                        container.getInput().clearKeyPressedRecord();
                        break;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        /*
        long flag = 0;
        int button = -1;
        for (int i = 0; i < 64; i++) {
            if (container.getInput().isControlPressed(i, 0)) {
                button = i;
                break;
            }
        }
        if (button >= 0) {
            System.out.println("push button: " + button);
        }
        */
    }

    @Override
    public void controlPressed(Command command) {
        ((KeyAction)command).event(true);
    }

    @Override
    public void controlReleased(Command command) {
        ((KeyAction)command).event(false);
    }
}
