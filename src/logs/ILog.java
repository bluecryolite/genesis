package logs;

/**
 * 日志接口
 */
public interface ILog {

    /**
     * 当前行写日志
     * @param info 日志内容
     */
    void info(String info);

    /**
     * 清空日志
     */
    void clear();

    /**
     * 当前行写完日志后换行
     * @param info 日志内容
     */
    void line(String info);

    /**
     * 写一个空行
     */
    void line();
}
