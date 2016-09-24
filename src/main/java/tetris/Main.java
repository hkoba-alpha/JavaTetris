package tetris;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import tetris.play.game.KeyConfigGame;
import tetris.play.game.TetrisPlayGame;

/**
 * Created by hkoba on 2016/08/15.
 */
public class Main extends StateBasedGame {
    public Main() {
        super("テトリス");
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        this.addState(new KeyConfigGame());
        this.addState(new TetrisPlayGame());
        //this.enterState(1);

        /*

        InputProvider provider = new InputProvider(container.getInput());
        provider.addListener(this);

        provider.bindCommand(new ControllerDirectionControl(0, ControllerDirectionControl.LEFT), KeyAction.MOVE_LEFT);
        provider.bindCommand(new ControllerDirectionControl(0, ControllerDirectionControl.RIGHT), KeyAction.MOVE_RIGHT);
        int num = 0;
        for (KeyAction action: KeyAction.values()) {
            num++;
            provider.bindCommand(new ControllerButtonControl(0, num), action);
        }

        provider.bindCommand(new KeyControl(Input.KEY_LEFT), KeyAction.MOVE_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_RIGHT), KeyAction.MOVE_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_UP), KeyAction.HARD_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_DOWN), KeyAction.MOVE_DROP);
        provider.bindCommand(new KeyControl(Input.KEY_Z), KeyAction.TURN_LEFT);
        provider.bindCommand(new KeyControl(Input.KEY_X), KeyAction.TURN_RIGHT);
        provider.bindCommand(new KeyControl(Input.KEY_SPACE), KeyAction.BUTTON_HOLD);
        */
    }

    public static void main(String[] args) {
        LibraryLoader.init();

        Main main = new Main();
        try {
            AppGameContainer container = new AppGameContainer(main);
            container.setDisplayMode(512, 448, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            //container.setDefaultFont(new UnicodeFont(new Font("Dialog", Font.BOLD, 14)));
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
