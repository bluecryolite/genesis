package logs;

import javax.swing.*;

/**
 * 在TextArea中记录日志
 */
public class FormLog implements ILog {
    private JTextArea _container;

    /**
     * 构造函数
     * @param container 记录日志的TextArea控件
     */
    public FormLog(JTextArea container) {
        _container = container;
    }

    /**
     * 清空日志
     */
    @Override
    public void clear() {
        _container.setText("");
    }

    /**
     * 当前行写日志
     * @param info 日志内容
     */
    @Override
    public void info(String info) {
        _container.append(info);
    }

    /**
     * 当前行写完日志后换行
     * @param info 日志内容
     */
    @Override
    public void line(String info) {
        info(info);
        line();
    }

    /**
     * 写一个空行
     */
    @Override
    public void line() {
        info("\r\n");
    }
}
