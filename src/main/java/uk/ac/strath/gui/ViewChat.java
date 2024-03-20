package uk.ac.strath.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import uk.ac.strath.GUI;

import javax.swing.*;
import java.awt.*;

public class ViewChat extends View {
    private JPanel mainPanel;
    private JTextPane messages;
    private JButton send;
    private JTextField input;
    private JList onlinePeople;
    private JTextPane currentTime;

    public ViewChat(GUI gui) {
        super("chat");

        input.addActionListener(e -> {
            gui.sendMessage(input.getText());
            input.setText("");
        });

        send.addActionListener(e -> {
            gui.sendMessage(input.getText());
            input.setText("");
        });

        onlinePeople.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    String text = value.toString();

                    if (text.charAt(0) == '!') {
                        setForeground(Color.YELLOW);
                        setText(text.substring(1));
                    } else {
                        setForeground(Color.GREEN);
                        setText(text);
                    }
                }

                return this;
            }
        });
    }

    public JTextPane getMessages() {
        return messages;
    }

    public JList getOnlinePeople() {
        return onlinePeople;
    }

    public JTextPane getCurrentTime() {
        return currentTime;
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 3, new Insets(4, 4, 4, 4), -1, -1));
        mainPanel.setAutoscrolls(false);
        messages = new JTextPane();
        messages.setEditable(false);
        mainPanel.add(messages, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        send = new JButton();
        send.setText("Send");
        mainPanel.add(send, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        input = new JTextField();
        mainPanel.add(input, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        onlinePeople = new JList();
        mainPanel.add(onlinePeople, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        currentTime = new JTextPane();
        currentTime.setEditable(false);
        mainPanel.add(currentTime, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 10), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
