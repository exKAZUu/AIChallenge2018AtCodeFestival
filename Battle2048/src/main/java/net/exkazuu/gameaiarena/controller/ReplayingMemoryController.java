package net.exkazuu.gameaiarena.controller;

import java.io.Serializable;
import java.util.Iterator;

public class ReplayingMemoryController<Arg, Result extends Serializable>
    extends DefaultController<Arg, Result> {
  private final Iterator<Result> resultIterator;

  public ReplayingMemoryController(Controller<Arg, Result> controller,
                                   Iterable<Result> results) {
    super(controller);
    this.resultIterator = results.iterator();
  }

  @Override
  protected void sendDataToAI(Arg input) {}

  @Override
  protected void receiveDataFromAI(Arg input) {}

  @Override
  protected Result runPostProcessing(Arg input) {
    return resultIterator.next();
  }
}
