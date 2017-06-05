package tetris.config;

/**
 * Created by hkoba on 2016/08/27.
 */
public interface IPlayConfig extends IDelayConfig, ISpeedConfig {
    char getNextMino();
    boolean canHold();
    boolean isGhost(int level);

    int initLevel(long seed);
    int getNextLevel(int curLevel, int deleteLines);
    int getTargetLevel(int level);

    /**
     * 着地後の消えるまでのターン数
     * @param level
     * @return 0:消えない
     */
    int getHideTurn(int level);
}
