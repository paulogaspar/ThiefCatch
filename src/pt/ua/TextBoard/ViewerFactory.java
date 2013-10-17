package pt.ua.TextBoard;

public class ViewerFactory
{
  public static TextBoardViewer create(String name, int numLines, int numColumns)
  {
    TextBoardViewer result = null;
    switch(target)
    {
      case CONSOLE:
        result = new TextBoardConsoleViewer(name, numLines, numColumns);
        break;
      case SWING:
        result = new TextBoardSwingViewer(name, numLines, numColumns);
        break;
    }

    assert result != null;

    return result;
  }

  public static void selectConsole()
  {
    target = Target.CONSOLE;
  }

  public static void selectSwing()
  {
    target = Target.SWING;
  }

  protected enum Target {CONSOLE,SWING};
  protected static Target target = Target.SWING;
}

