package reader.concurrency

import java.util.concurrent.ThreadPoolExecutor

import play.api._

class ExecutorMonitor(pool: ThreadPoolExecutor) extends Runnable {

  val executor: ThreadPoolExecutor = pool;

  def run() {
    try {
      var running = true
      do {
        Logger.info("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s, queueSize: %d"
          format (executor.getPoolSize,
          executor.getCorePoolSize,
          executor.getActiveCount,
          executor.getCompletedTaskCount,
          executor.getTaskCount,
          executor.isShutdown,
          executor.isTerminated,
          executor.getQueue.size))
        if (executor.getCompletedTaskCount >= executor.getTaskCount) {
          executor.shutdown
          running = false
        }
        Thread.sleep(3000)
      } while (running)
    } catch {
      case e: Exception => println(e.getMessage)
    }
  }
}
