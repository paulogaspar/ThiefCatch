package pt.ua.TextBoard;

import javax.swing.ImageIcon;

public class TextBoardConsoleViewer extends TextBoardViewer
{
  public TextBoardConsoleViewer(String name, int numLines, int numColumns)
  {
    super(name, numLines, numColumns);

    Console.hideCursor();
    matrix = new String[numLines][numColumns];
    for (int l = 0; l < numLines; l++)
      for (int c = 0; c < numColumns; c++)
        matrix[l][c] = " ";
    drawAll();
  }

  synchronized public void drawAll()
  {
    Console.resetColorsToDefault();
    Console.clear();
    printCentered(name,boardLine-1);
    gotoCell(0,0);
    Console.print("/");
    for(int c = 1;c <= numberOfColumns();c++)
      Console.print("-");
    Console.print("\\");
    gotoCell(1,0);
    for(int l = 1;l <= numberOfLines();l++)
    {
      Console.print("|");
      for(int c = 1;c <= numberOfColumns();c++)
        Console.print(matrix[l-1][c-1]);
      Console.print("|");
      gotoCell(l+1,0);
    }
    Console.print("\\");
    for(int c = 1;c <= numberOfColumns();c++)
      Console.print("-");
    Console.print("/");
  }

  synchronized public void setCell(int line, int column, String text)
  {
    gotoCell(line,column);
    matrix[line-1][column-1] = text;
    Console.resetColorsToDefault();
    Console.print(text);
    gotoEnd();
  }

  synchronized public void setCell(int line, int column, String text, Colors foreground)
  {
    gotoCell(line,column);
    matrix[line-1][column-1] = text;
    Console.resetColorsToDefault();
    Console.setForeground(convertColor(foreground));
    Console.print(text);
    gotoEnd();
  }

  synchronized public void setCell(int line, int column, String text, Colors foreground, Colors background)
  {
    gotoCell(line,column);
    matrix[line-1][column-1] = text;
    Console.setForeground(convertColor(foreground));
    Console.setBackground(convertColor(background));
    Console.print(text);
    gotoEnd();
  }

  synchronized public void resetCell(int line,int column)
  {
    gotoCell(line,column);
    Console.resetColorsToDefault();
    Console.print(matrix[line-1][column-1]);
    gotoEnd();
  }

  synchronized public void terminate()
  {
    Console.moveCursor(boardLine+numberOfLines()+2,0);
    Console.print(name + " finished!");
    Console.showCursor();
    gotoEnd();
  }

  protected void gotoCell(int line,int column)
  {
    Console.moveCursor(boardLine+line,boardColumn+column);
  }

  protected void gotoEnd()
  {
    Console.moveCursor(boardLine+numberOfLines()+3,0);
  }

  protected void printCentered(String str,int line)
  {
    int spaces = (numberOfColumns()+2-str.length())/2;
    Console.moveCursor(line,boardColumn);
    for(int c = 0;c < spaces;c++)
      Console.print(" ");
    Console.print(str);
    for(int c = 0;c < spaces;c++)
      Console.print(" ");
  }

  protected Console.Color convertColor(Colors c)
  {
    Console.Color[] transation = {
      Console.Color.BLACK, Console.Color.RED, Console.Color.GREEN, Console.Color.YELLOW,
      Console.Color.BLUE, Console.Color.MAGENTA, Console.Color.CYAN, Console.Color.WHITE
    };

    Console.Color result = null;
    for(int i = 0; result == null && i < allColors.length; i++)
      if (c == allColors[i])
        result = transation[i];

    assert result != null;

    return result;
  }

  protected final int boardLine = 1;
  protected final int boardColumn = 4;
  protected String[][] matrix = null;
  protected Colors[] allColors = Colors.values();


    public void setCell(int line, int column, ImageIcon icon)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

