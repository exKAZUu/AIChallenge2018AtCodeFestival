package net.exkazuu.battle2048;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.exkazuu.battle2048.controller.*;
import net.exkazuu.battle2048.game.Game;
import net.exkazuu.battle2048.game.GameManager;
import net.exkazuu.battle2048.game.GameResult;
import net.exkazuu.battle2048.util.Logger;
import net.exkazuu.gameaiarena.controller.Controller;
import net.exkazuu.gameaiarena.player.ExternalComputerPlayer;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
  private static final String HELP = "h";
  private static final String LOGGING = "l";
  private static final String LOGGING_DETAILED = "ll";
  private static final String EXTERNAL_AI_PROGRAM = "a";
  private static final String WORKING_DIR = "w";
  private static final String REPLAY_OUTPUT_NAME = "o";
  private static final String TIME_LIMIT = "t";
  private static final String PAUSE_COMMAND = "p";
  private static final String UNPAUSE_COMMAND = "u";
  private static final String ONLINE_SYSTEM = "online-system";
  private static final String DEFAULT_COMMAND = "java SampleAI";
  private static final String DEFAULT_WORK_DIR = "./defaultai";
  private static final String DEFAULT_RECORD_FILENAME = "game.log";

  public static void main(String[] args) {
    Options options = buildOptions();
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cl = parser.parse(options, args);
      if (cl.hasOption(HELP)) {
        printHelp(options);
      } else {
        start(new Game(), cl);
      }
    } catch (ParseException | IOException e) {
      System.err.println("Error: " + e.getMessage());
      printHelp(options);
      System.exit(-1);
    }
  }

  private static void start(Game game, CommandLine cl) throws ParseException, IOException {
    List<String> externalCmds = getOptionsValuesOrEmptyList(cl, EXTERNAL_AI_PROGRAM);
    List<String> workingDirs = getOptionsValuesOrEmptyList(cl, WORKING_DIR);
    List<String> pauseCmds = getOptionsValuesOrEmptyList(cl, PAUSE_COMMAND);
    List<String> unpauseCmds = getOptionsValuesOrEmptyList(cl, UNPAUSE_COMMAND);
    if (workingDirs.isEmpty()) {
      workingDirs = externalCmds.stream().map(s -> ".").collect(Collectors.toList());
    }
    if (workingDirs.size() != externalCmds.size()) {
      throw new ParseException("The number of -w arguments must be 0 or equal to the number of -a arguments.");
    }
    if (pauseCmds.size() != unpauseCmds.size()) {
      throw new ParseException("The numbers of -p and -u arguments must be equal.");
    }
    if (pauseCmds.size() != 0 && pauseCmds.size() != externalCmds.size()) {
      throw new ParseException("The number of -p arguments must be 0 or equal to the number of -a arguments.");
    }
    if (unpauseCmds.size() != 0 && unpauseCmds.size() != externalCmds.size()) {
      throw new ParseException("The number of -u arguments must be 0 or equal to the number of -a arguments.");
    }

    List<Controller<Game, String[]>> initCtrls = new ArrayList<>();
    List<Controller<Game, String[]>> mainCtrls = new ArrayList<>();

    for (int i = 0; i < externalCmds.size(); i++) {
      try {
        ExternalComputerPlayer com = new ExternalComputerPlayer(externalCmds.get(i).split(" "), workingDirs.get(i));
        Controller<Game, String[]> initCtrl = new AIInitialController(com, i);
        Controller<Game, String[]> mainCtrl = new AIMainController(com, i);
        if (pauseCmds.size() != 0) {
          initCtrl = initCtrl.pauseUnpause(pauseCmds.get(i).split(" "), unpauseCmds.get(i).split(" "));
          mainCtrl = mainCtrl.pauseUnpause(pauseCmds.get(i).split(" "), unpauseCmds.get(i).split(" "));
        }
        initCtrls.add(initCtrl);
        mainCtrls.add(mainCtrl);
      } catch (IOException e) {
        System.exit(-1);
      }
    }
    for (int i = externalCmds.size(); i < 2; i++) {
      initCtrls.add(new RandomAIInitialController(i));
      mainCtrls.add(new RandomAIMainController(i));
    }

    int logLevel = Logger.LOG_LEVEL_RESULT;
    if (cl.hasOption(LOGGING)) logLevel = Logger.LOG_LEVEL_STATUS;
    if (cl.hasOption(LOGGING_DETAILED)) logLevel = Logger.LOG_LEVEL_DETAILS;
    if (cl.hasOption(ONLINE_SYSTEM)) logLevel = 0;
    Logger.getInstance().initialize(logLevel, false);

    int initTimeLimit = 1000;
    int mainTurnTimeLimit = 1000;
    int mainSumTimeLimit = 100 * 1000;
    List<String> timeLimitArgs = getOptionsValuesOrEmptyList(cl, TIME_LIMIT);
    if (!timeLimitArgs.isEmpty()) {
      try {
        Integer[] t = Arrays.stream(timeLimitArgs.get(0).split(",")).map(Integer::parseInt).toArray(Integer[]::new);
        initTimeLimit = t[0];
        mainTurnTimeLimit = t[1];
        mainSumTimeLimit = t[2];
      } catch (Exception e) {
        throw new ParseException("The -t argument must be comma-separated 3 integers.");
      }
    }

    GameManager gameManager = new GameManager(initCtrls, mainCtrls, initTimeLimit, mainTurnTimeLimit, mainSumTimeLimit);
    GameResult result = gameManager.start();

    if (cl.hasOption(ONLINE_SYSTEM)) {
      final JudgeOutput judgeOutput = new JudgeOutput(result.winner, gameManager.recorder.getReplayList(), result);
      for (int player = 0; player < 2; player++) {
        final ExternalComputerPlayer computerPlayer = mainCtrls.get(player).getExternalComputerPlayer();
        if (computerPlayer != null) {
          judgeOutput.addLog(player, "STDERR>>\n" + computerPlayer.getErrorLog());
        }
      }

      System.out.println(new ObjectMapper().writeValueAsString(judgeOutput));

    } else {
      System.out.println(new ObjectMapper().writeValueAsString(result));

      gameManager.recorder.writeReplayLog(cl.getOptionValue("o", DEFAULT_RECORD_FILENAME), result);
    }

    Logger.getInstance().finalize();

    for (int i = 0; i < 2; i++) {
      initCtrls.get(i).release();
      mainCtrls.get(i).release();
    }
  }

  private static Options buildOptions() {
    return new Options()
      .addOption(HELP, false, "Print this help.")
      .addOption(LOGGING, false, "Enable writing logs to stdout and file.")
      .addOption(LOGGING_DETAILED, false, "Enable writing detailed logs to stdout and file.")
      .addOption(EXTERNAL_AI_PROGRAM, true, "Set 0-2 external AI programs.")
      .addOption(WORKING_DIR, true, "Set working directories for external AI programs.")
      .addOption(REPLAY_OUTPUT_NAME, true, "Set output file name for a replay.")
      .addOption(TIME_LIMIT, true, "Set comma-separated 3 time limits (ms) \"INIT,MAIN_TURN,MAIN_SUM\".")
      .addOption(PAUSE_COMMAND, true, "Internal use for online judge system.")
      .addOption(UNPAUSE_COMMAND, true, "Internal use for online judge system.")
      .addOption(null, ONLINE_SYSTEM, false, "Internal use for online judge system.");
  }

  private static void printHelp(Options options) {
    HelpFormatter help = new HelpFormatter();
    help.printHelp("java -jar Battle2048.jar [OPTIONS]\n" + "[OPTIONS]: ", "", options, "", true);
  }

  private static List<String> getOptionsValuesOrEmptyList(CommandLine cl, String option) {
    return cl.hasOption(option) ? Lists.newArrayList(cl.getOptionValues(option)) : Lists.newArrayList();
  }
}
