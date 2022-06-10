package icu.whereis.common.utils;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringKit {
    public static String getTimestamp(String format) {
        Date d = new Date();
        SimpleDateFormat sbf = new SimpleDateFormat(format);
        String timestamp = sbf.format(d);
        return timestamp;
    }

    /**
     * 展示一个对话框
     * @param str
     * @return
     */
    public static int showMessage(String str) {
        Object[] options = null;
        JOptionPane pane = new JOptionPane(str, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null, null);
        // Configure via set methods
        JDialog dialog = pane.createDialog(null, "消息");
        // the line below is added to the example from the docs
        dialog.setModal(false); // this says not to block background components
        dialog.setVisible(true);
        Object selectedValue = pane.getValue();
        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        //If there is not an array of option buttons:
        if(options == null) {
            if(selectedValue instanceof Integer)
                return ((Integer)selectedValue).intValue();
            return JOptionPane.CLOSED_OPTION;
        }
        //If there is an array of option buttons:
        for(int counter = 0, maxCounter = options.length;
            counter < maxCounter; counter++) {
            if(options[counter].equals(selectedValue))
                return counter;
        }
        return JOptionPane.CLOSED_OPTION;
    }
}
