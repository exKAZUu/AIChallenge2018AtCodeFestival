package net.exkazuu.battle2048.controller;

import com.google.common.base.Strings;
import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.util.Logger;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;

import java.io.*;

public class AIMainController extends AIController {
  private String _line;
  private String _input;
  private BufferedReader errorReader;

  public AIMainController(ExternalComputerPlayer com, int index) {
    super(com, index);

    try {
      final PipedOutputStream pipedOutputStream = new PipedOutputStream();
      com.addErrorLogStream(new PrintStream(pipedOutputStream));

      final PipedInputStream pipedInputStream = new PipedInputStream();
      pipedOutputStream.connect(pipedInputStream);

      errorReader = new BufferedReader(new InputStreamReader(pipedInputStream));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void runPreProcessing(Game game) {
    _input = game.generateMainInput(_index);
    Logger.getInstance().outputLog("AI" + _index + ">>Writing to stdin, waiting for stdout", Logger.LOG_LEVEL_DETAILS);
    Logger.getInstance().outputLog(_input, Logger.LOG_LEVEL_DETAILS);

    _line = "";
  }

  @Override
  protected void sendDataToAI(Game game) {
    _com.writeLine(_input);
  }

  @Override
  protected void receiveDataFromAI(Game game) {
    _line = _com.readLine();
  }

  @Override
  protected String[] runPostProcessing(Game game) {
    try {
      String line;
      while (errorReader.ready() && (line = errorReader.readLine()) != null) {
        Logger.getInstance().outputLog("AI" + _index + ">>STDERR: " + line, Logger.LOG_LEVEL_STATUS);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Logger.getInstance().outputLog("AI" + _index + ">>STDOUT: " + _line, Logger.LOG_LEVEL_DETAILS);
    return !Strings.isNullOrEmpty(_line) ? _line.trim().split(" ") : new String[0];
  }
}