package net.exkazuu.gameaiarena.player;

import com.google.common.base.Joiner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExternalComputerPlayer {

  private Process _process;
  private StringBuilder _errorLog;

  private final BufferedReader _reader;
  private final BufferedReader _errorReader;

  private final PrintStream _writer;
  private final List<PrintStream> _inputLogStreams;
  private final List<PrintStream> _outputLogStreams;
  private final List<PrintStream> _errorLogStreams;

  public ExternalComputerPlayer(String[] command) throws IOException {
    this(command, null);
  }

  public ExternalComputerPlayer(String[] command, String workDir) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(command);
    if (workDir != null) {
      pb.directory(new File(workDir));
    }
    _inputLogStreams = new ArrayList<>();
    _outputLogStreams = new ArrayList<>();
    _errorLogStreams = new ArrayList<>();
    _errorLog = new StringBuilder();
    try {
      _process = pb.start();
      _reader = new BufferedReader(new InputStreamReader(_process.getInputStream()));
      _writer = new PrintStream(_process.getOutputStream());
      _errorReader = new BufferedReader(new InputStreamReader(_process.getErrorStream()));
    } catch (IOException e) {
      System.err.println("Fail to launch the specified command for running an AI program.");
      System.err.println("    Command with args: " + Joiner.on(" ").join(command));
      if (_process != null) {
        _process.destroy();
      }
      throw e;
    }
  }

  public void addInputLogStream(PrintStream outStream) {
    _inputLogStreams.add(outStream);
  }

  public void addOuputLogStream(PrintStream outStream) {
    _outputLogStreams.add(outStream);
  }

  public void addErrorLogStream(PrintStream outStream) {
    _errorLogStreams.add(outStream);
  }

  public boolean available() {
    return _process != null;
  }

  public void release() {
    if (_process == null) {
      return;
    }
    writeStderr();
    _process.destroy();
    try {
      if (_reader != null) {
        _reader.close();
      }
      if (_writer != null) {
        _writer.close();
      }
      if (_errorReader != null) {
        _errorReader.close();
      }
      for (PrintStream stream : _inputLogStreams) {
        stream.close();
      }
      for (PrintStream stream : _outputLogStreams) {
        stream.close();
      }
      for (PrintStream stream : _errorLogStreams) {
        stream.close();
      }
    } catch (IOException e) {
      System.err.println("Fail to close streams.");
    }
    _process = null;
  }

  public void writeLine(String str) {
    str = str.trim();
    for (PrintStream stream : _inputLogStreams) {
      stream.println(str);
      stream.flush();
    }
    _writer.println(str);
    _writer.flush();
  }

  public String readLine() {
    String line = null;
    try {
      line = _reader.readLine();
    } catch (IOException e) {
      System.err.println("Fail to read a line from the standard output.");
    }
    writeStderr();
    for (PrintStream stream : _outputLogStreams) {
      stream.println(line);
      stream.flush();
    }
    return line;
  }

  public String getErrorLog() {
    return _errorLog.toString();
  }

  private void writeStderr() {
    try {
      if (!_errorReader.ready()) {
        return;
      }
      String line;
      while (_errorReader.ready() && (line = _errorReader.readLine()) != null) {
        for (PrintStream stream : _errorLogStreams) {
          stream.println(line);
          stream.flush();
        }
        _errorLog.append(line);
        _errorLog.append('\n');
      }
    } catch (IOException e) {
      System.err.println("Fail to read the error stream.");
    }
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      release();
    } finally {
      super.finalize();
    }
  }
}
