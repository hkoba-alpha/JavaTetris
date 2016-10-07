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
    }

    public static void main(String[] args) {
        LibraryLoader.init();

        Main main = new Main();
        try {
            AppGameContainer container = new AppGameContainer(main);
            container.setDisplayMode(512, 448, false);
            container.setTargetFrameRate(60);
            container.setVSync(true);
            container.setShowFPS(false);
            //container.setDefaultFont(new UnicodeFont(new Font("Dialog", Font.BOLD, 14)));
            container.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
