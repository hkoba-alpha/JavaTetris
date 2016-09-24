package tetris.config;

/**
 * Created by hkoba on 2016/08/18.
 */
public interface IDelayConfig {
    /**
     * 固まった後の次が出てくるまでのフレーム数
     * @param level
     * @return
     */
    int getAre(int level);

    /**
     * ラインを消した後の次が出てくるまでのフレーム数
     * @param level
     * @return
     */
    int getLineAre(int level);

    /**
     * 横ためのフレーム数
     * @param level
     * @return
     */
    int getDas(int level);

    /**
     * 着地から固まるまでのフレーム数
     * @param level
     * @return
     */
    int getLock(int level);

    /**
     * ラインがそろって消したのが落ちるまでのフレーム数
     * @param level
     * @return
     */
    int getLineClear(int level);
}
