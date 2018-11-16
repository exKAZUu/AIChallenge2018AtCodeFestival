package net.exkazuu.battle2048.game;

import com.google.common.collect.ImmutableMap;
import net.exkazuu.gameaiarena.api.Direction4;
import net.exkazuu.gameaiarena.api.Point2;
import net.exkazuu.gameaiarena.controller.LimitingSumTimeController;
import net.exkazuu.gameaiarena.controller.LimitingTimeController;
import net.exkazuu.gameaiarena.controller.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameManager {
  private static final Map<Character, Direction4> charToDirection = ImmutableMap.of(
    'L', Direction4.LEFT, 'U', Direction4.UP, 'R', Direction4.RIGHT, 'D', Direction4.DOWN
  );

  private final Game _game;
  private final List<Controller<Game, String[]>> _initCtrls;
  private final List<Controller<Game, String[]>> _mainCtrls;
  private final int _initTimeLimit;
  private final int _mainTurnTimeLimit;
  private final int _mainSumTimeLimit;

  public final GameRecorder recorder;

  public GameManager(List<Controller<Game, String[]>> initCtrls, List<Controller<Game, String[]>> mainCtrls, int initTimeLimit, int mainTurnTimeLimit, int mainSumTimeLimit) {
    _game = new Game();
    _initCtrls = initCtrls;
    _mainCtrls = mainCtrls;
    _initTimeLimit = initTimeLimit;
    _mainTurnTimeLimit = mainTurnTimeLimit;
    _mainSumTimeLimit = mainSumTimeLimit;
    recorder = new GameRecorder();
  }

  public GameResult start() {
    for (int playerIndex = 0; playerIndex < _initCtrls.size(); playerIndex++) {
      LimitingTimeController<Game, String[]> controller = new LimitingTimeController<>(_initCtrls.get(playerIndex), _initTimeLimit);
      String[] result = controller.run(_game);

      if (controller.timeExceeded()) {
        return killController(DefeatReason.TIME_LIMIT_EXCEEDED_AT_INIT, playerIndex);
      }
      this.recorder.add(new TurnRecord(playerIndex)
        .setAIInput(Integer.toString(playerIndex))
        .setAIOutput(result)
        .setRuntime(controller.getLastConsumedMillisecond())
      );

      if (result.length != 2) {
        return killController(DefeatReason.PRESENTATION_ERROR_AT_INIT, playerIndex);
      }
      int x, y;
      try {
        y = Integer.parseInt(result[0]) - 1;
        x = Integer.parseInt(result[1]) - 1;
      } catch (Exception e) {
        return killController(DefeatReason.PRESENTATION_ERROR_AT_INIT, playerIndex);
      }
      DefeatReason reason = _game.advanceInitialPhase(playerIndex, new Point2(x, y));
      if (reason != null) {
        return killController(reason, playerIndex);
      }
    }

    List<LimitingSumTimeController<Game, String[]>> limitingSumTimeControllers = new ArrayList<>();
    for (Controller<Game, String[]> controller : _mainCtrls) {
      limitingSumTimeControllers.add(new LimitingSumTimeController<>(controller, 0, _mainSumTimeLimit));
    }
    while (!_game.finished()) {
      for (int playerIndex = 0; playerIndex < _mainCtrls.size(); playerIndex++) {
        if (!_game.getMyBoard(playerIndex).canMove()) {
          return killController(DefeatReason.CANNOT_MOVE_AT_MAIN, playerIndex);
        }

        LimitingSumTimeController<Game, String[]> limitingSumTimeController = limitingSumTimeControllers.get(playerIndex);
        LimitingTimeController<Game, String[]> limitingTimeController = new LimitingTimeController<>(limitingSumTimeController, _mainTurnTimeLimit);

        _game.setMyTimeLeft(playerIndex, limitingSumTimeController.getRestExceededMillisecond());
        String[] result = limitingTimeController.run(_game);

        if (limitingSumTimeController.timeExceeded() || limitingTimeController.timeExceeded()) {
          return killController(DefeatReason.TIME_LIMIT_EXCEEDED_AT_MAIN_OR_ATTACK, playerIndex);
        }

        this.recorder.add(new TurnRecord(playerIndex)
          .setAIInput(_game, playerIndex)
          .setAIOutput(result)
          .setRuntime(limitingTimeController.getLastConsumedMillisecond())
        );

        if (result.length < 5) {
          return killController(DefeatReason.PRESENTATION_ERROR_AT_MAIN_OR_ATTACK, playerIndex);
        }
        if (result[0].length() != 1 || !charToDirection.containsKey(result[0].charAt(0))) {
          return killController(DefeatReason.PRESENTATION_ERROR_AT_MAIN_OR_ATTACK, playerIndex);
        }

        Direction4 d = charToDirection.get(result[0].charAt(0));

        int M, V;
        try {
          M = Integer.parseInt(result[1]);
          V = Integer.parseInt(result[2]);
        } catch (Exception e) {
          return killController(DefeatReason.PRESENTATION_ERROR_AT_MAIN_OR_ATTACK, playerIndex);
        }
        if (result.length != 3 + M * 2 || V < 1) {
          return killController(DefeatReason.PRESENTATION_ERROR_AT_MAIN_OR_ATTACK, playerIndex);
        }

        List<Point2> ps = new ArrayList<>();
        for (int i = 0; i < M * 2; i += 2) {
          try {
            int y = Integer.parseInt(result[i + 3]) - 1;
            int x = Integer.parseInt(result[i + 4]) - 1;
            ps.add(new Point2(x, y));
          } catch (Exception e) {
            return killController(DefeatReason.PRESENTATION_ERROR_AT_MAIN_OR_ATTACK, playerIndex);
          }
        }

        DefeatReason reason = _game.advanceMainAndAttackPhase(playerIndex, d, M, V, ps);
        if (reason != null) {
          return killController(reason, playerIndex);
        }
      }
    }
    return _game.judge();
  }


  private GameResult killController(DefeatReason reason, int playerIndex) {
    _initCtrls.get(playerIndex).release();
    _mainCtrls.get(playerIndex).release();
    return _game.lose(playerIndex, reason);
  }
}
