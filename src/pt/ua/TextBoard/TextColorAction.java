package pt.ua.TextBoard;

import java.awt.*;
import javax.swing.*;

public class TextColorAction implements Runnable
{
  public TextColorAction(JLabel label, Color foreground)
  {
    this.label = label;
    this.foreground = foreground;
  }

  public TextColorAction(JLabel label, Color foreground, Color background)
  {
    this.label = label;
    this.foreground = foreground;
    this.background = background;
  }

  public TextColorAction(JLabel label, String text, Color foreground)
  {
    this.label = label;
    this.text = text;
    this.foreground = foreground;
  }

  public TextColorAction(JLabel label, String text, Color foreground, Color background)
  {
    this.label = label;
    this.text = text;
    this.foreground = foreground;
    this.background = background;
  }

  public TextColorAction(JLabel label, ImageIcon icon, Color foreground, Color background)
  {
    this.label = label;
    this.icon = icon;
    this.foreground = foreground;
    this.background = background;
  }

  public void run()
  {
    if (text != null)
      label.setText(text);

    label.setForeground(foreground);

    if (background != null)
      label.setBackground(background);

    label.setIcon(icon);
  }

  protected JLabel label;
  protected ImageIcon icon = null;
  protected String text = null;
  protected Color foreground = null;
  protected Color background = null;
}
