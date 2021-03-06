package org.ineto.niosocks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class TrafficLogger {

  private final static Logger log = Logger.getLogger(TrafficLogger.class);

  private boolean enable;
  private ExecutorService logExecutor;
  private String logDir;

  public TrafficLogger(Properties props) {
    this.logDir = props.getProperty("log.dir");
    if(logDir == null) {
    	enable = false;
    	return;
    }

    enable = "yes".equalsIgnoreCase(props.getProperty("log.traffic", "yes"));
    if (enable) {
      int logthreads = Integer.parseInt(props.getProperty("log.threads", "1"));
      logExecutor = Executors.newFixedThreadPool(logthreads);
    }
  }

  public void shutdown() {
    if (enable) {
      logExecutor.shutdownNow();
    }
  }

  public void log(int connectionId, String logfile, int num, byte[] blob) {
    if (enable) {
      logExecutor.execute(new FileLogOperation(connectionId, logfile, num, blob));
    }
  }

  public boolean isEnable() {
    return enable;
  }

  public class FileLogOperation implements Runnable {

    private int connectionId;
    private String logfile;
    private int num;
    private byte[] blob;

    public FileLogOperation(int connectionId, String logfile, int num, byte[] blob) {
      this.connectionId = connectionId;
      this.logfile = logfile;
      this.num = num;
      this.blob = new byte[blob.length];
      System.arraycopy(blob, 0, this.blob, 0, blob.length);
    }

    @Override
    public void run() {
      writeLog(connectionId, logfile, num, blob);
    }

  }

  public void writeLog(int connectionId, String logfile, int num, byte[] blob) {
    try {
    	String dir = logDir + "/log/" + connectionId + "_" + logfile + num;
    	System.out.println(dir);
      FileOutputStream out = new FileOutputStream(dir);
      try {
        out.write(blob);
      }
      finally {
        out.close();
      }
    }
    catch(IOException e) {
      log.error("fail to write to file", e);
    }
  }

}
