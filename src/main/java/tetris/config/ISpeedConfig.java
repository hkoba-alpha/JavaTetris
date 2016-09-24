package tetris.config;

/**
 * Created by hkoba on 2016/08/18.
 */
public interface ISpeedConfig {
    /**
     * 1/256Gの値を返す
     * @param level
     * @return
     */
    int getGravity(int level);
}
