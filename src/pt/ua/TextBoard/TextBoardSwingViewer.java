package pt.ua.TextBoard;

import java.awt.*;
import javax.swing.*;

public class TextBoardSwingViewer extends TextBoardViewer
{
  public TextBoardSwingViewer(String name, int numLines, int numColumns)
  {
    super(name, numLines, numColumns);

    
    Font f = new Font("Lucida Console",Font.PLAIN,16);
    matrix = new JLabel[numLines][numColumns];
    for(int l=0;l < numLines;l++)
      for(int c=0;c < numColumns;c++)
      {
        matrix[l][c] = new JLabel(" ",SwingConstants.CENTER);
        matrix[l][c].setFont(f);
        matrix[l][c].setOpaque(true);
        matrix[l][c].setPreferredSize(new Dimension(20,20));
        matrix[l][c].setHorizontalTextPosition(JLabel.CENTER);
        matrix[l][c].setVerticalTextPosition(JLabel.CENTER);
        matrix[l][c].setBackground(new Color(102,102,102));
      }
    defaultForeground = matrix[0][0].getForeground();
    defaultBackground = matrix[0][0].getBackground();

    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        createAndShowGUI();
      }
    });
  }

  public void drawAll()
  {
      for(int l=1;l <= numLines;l++)
          for(int c=1;c <= numColumns;c++)
          {
            resetCell(l, c);
          }
  }

   public void setCell(int line, int column, ImageIcon icon)
  {
    javax.swing.SwingUtilities.invokeLater(new TextColorAction(matrix[line-1][column-1],
                                           icon,defaultForeground,defaultBackground));
  }

  public void setCell(int line, int column, String text)
  {
    javax.swing.SwingUtilities.invokeLater(new TextColorAction(matrix[line-1][column-1],
                                           text,defaultForeground,defaultBackground));
  }

  public void setCell(int line, int column, String text, Colors foreground)
  {
    javax.swing.SwingUtilities.invokeLater(new TextColorAction(matrix[line-1][column-1],
                                           text,convertColor(foreground),defaultBackground));
  }

  public void setCell(int line, int column, String text, Colors foreground, Colors background)
  {
    javax.swing.SwingUtilities.invokeLater(new TextColorAction(matrix[line-1][column-1],
                                           text,convertColor(foreground),convertColor(background)));
  }

  public void resetCell(int line,int column)
  {
    javax.swing.SwingUtilities.invokeLater(new TextColorAction(matrix[line-1][column-1],
                                           defaultForeground,defaultBackground));
  }

  public void terminate()
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        JOptionPane.showMessageDialog(frame.getContentPane(),name + " finished!");
        System.exit(0);
      }
    });
  }

  protected void createAndShowGUI()
  {
    JFrame.setDefaultLookAndFeelDecorated(true);

    frame = new JFrame(name);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(numberOfLines(),numberOfColumns(),0,0));
    for(int l=0;l < numberOfLines();l++)
      for(int c=0;c < numberOfColumns();c++)
        panel.add(matrix[l][c]);

    Container pane = frame.getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
    pane.add(panel,BorderLayout.CENTER);

    JSeparator sep = new JSeparator();
    sep.setAlignmentX(Component.CENTER_ALIGNMENT);
    pane.add(sep,BorderLayout.CENTER);

    frame.pack();
    frame.setVisible(true);
  }

  protected Color convertColor(Colors c)
  {
    Color[] transation = {
      Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW,
      Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE
    };

    Color result = null;
    for(int i = 0; result == null && i < allColors.length; i++)
      if (c == allColors[i])
        result = transation[i];

    assert result != null;

    return result;
  }

   
  protected JFrame frame;
  protected JLabel[][] matrix = null;
  protected Color defaultForeground;
  protected Color defaultBackground;
  protected Colors[] allColors = Colors.values();
}
