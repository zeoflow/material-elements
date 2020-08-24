

package com.zeoflow.material.elements.snackbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.ref.WeakReference;


class SnackbarManager {

  static final int MSG_TIMEOUT = 0;

  private static final int SHORT_DURATION_MS = 1500;
  private static final int LONG_DURATION_MS = 2750;

  private static SnackbarManager snackbarManager;

  static SnackbarManager getInstance() {
    if (snackbarManager == null) {
      snackbarManager = new SnackbarManager();
    }
    return snackbarManager;
  }

  @NonNull private final Object lock;
  @NonNull private final Handler handler;

  @Nullable private SnackbarRecord currentSnackbar;
  @Nullable private SnackbarRecord nextSnackbar;

  private SnackbarManager() {
    lock = new Object();
    handler =
        new Handler(
            Looper.getMainLooper(),
            new Handler.Callback() {
              @Override
              public boolean handleMessage(@NonNull Message message) {
                switch (message.what) {
                  case MSG_TIMEOUT:
                    handleTimeout((SnackbarRecord) message.obj);
                    return true;
                  default:
                    return false;
                }
              }
            });
  }

  interface Callback {
    void show();

    void dismiss(int event);
  }

  public void show(int duration, Callback callback) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback)) {
        
        currentSnackbar.duration = duration;

        
        
        handler.removeCallbacksAndMessages(currentSnackbar);
        scheduleTimeoutLocked(currentSnackbar);
        return;
      } else if (isNextSnackbarLocked(callback)) {
        
        nextSnackbar.duration = duration;
      } else {
        
        nextSnackbar = new SnackbarRecord(duration, callback);
      }

      if (currentSnackbar != null
          && cancelSnackbarLocked(currentSnackbar, Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE)) {
        
        return;
      } else {
        
        currentSnackbar = null;
        
        showNextSnackbarLocked();
      }
    }
  }

  public void dismiss(Callback callback, int event) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback)) {
        cancelSnackbarLocked(currentSnackbar, event);
      } else if (isNextSnackbarLocked(callback)) {
        cancelSnackbarLocked(nextSnackbar, event);
      }
    }
  }

  
  public void onDismissed(Callback callback) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback)) {
        
        currentSnackbar = null;
        if (nextSnackbar != null) {
          showNextSnackbarLocked();
        }
      }
    }
  }

  
  public void onShown(Callback callback) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback)) {
        scheduleTimeoutLocked(currentSnackbar);
      }
    }
  }

  public void pauseTimeout(Callback callback) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback) && !currentSnackbar.paused) {
        currentSnackbar.paused = true;
        handler.removeCallbacksAndMessages(currentSnackbar);
      }
    }
  }

  public void restoreTimeoutIfPaused(Callback callback) {
    synchronized (lock) {
      if (isCurrentSnackbarLocked(callback) && currentSnackbar.paused) {
        currentSnackbar.paused = false;
        scheduleTimeoutLocked(currentSnackbar);
      }
    }
  }

  public boolean isCurrent(Callback callback) {
    synchronized (lock) {
      return isCurrentSnackbarLocked(callback);
    }
  }

  public boolean isCurrentOrNext(Callback callback) {
    synchronized (lock) {
      return isCurrentSnackbarLocked(callback) || isNextSnackbarLocked(callback);
    }
  }

  private static class SnackbarRecord {
    @NonNull final WeakReference<Callback> callback;
    int duration;
    boolean paused;

    SnackbarRecord(int duration, Callback callback) {
      this.callback = new WeakReference<>(callback);
      this.duration = duration;
    }

    boolean isSnackbar(@Nullable Callback callback) {
      return callback != null && this.callback.get() == callback;
    }
  }

  private void showNextSnackbarLocked() {
    if (nextSnackbar != null) {
      currentSnackbar = nextSnackbar;
      nextSnackbar = null;

      final Callback callback = currentSnackbar.callback.get();
      if (callback != null) {
        callback.show();
      } else {
        
        currentSnackbar = null;
      }
    }
  }

  private boolean cancelSnackbarLocked(@NonNull SnackbarRecord record, int event) {
    final Callback callback = record.callback.get();
    if (callback != null) {
      
      handler.removeCallbacksAndMessages(record);
      callback.dismiss(event);
      return true;
    }
    return false;
  }

  private boolean isCurrentSnackbarLocked(Callback callback) {
    return currentSnackbar != null && currentSnackbar.isSnackbar(callback);
  }

  private boolean isNextSnackbarLocked(Callback callback) {
    return nextSnackbar != null && nextSnackbar.isSnackbar(callback);
  }

  private void scheduleTimeoutLocked(@NonNull SnackbarRecord r) {
    if (r.duration == Snackbar.LENGTH_INDEFINITE) {
      
      return;
    }

    int durationMs = LONG_DURATION_MS;
    if (r.duration > 0) {
      durationMs = r.duration;
    } else if (r.duration == Snackbar.LENGTH_SHORT) {
      durationMs = SHORT_DURATION_MS;
    }
    handler.removeCallbacksAndMessages(r);
    handler.sendMessageDelayed(Message.obtain(handler, MSG_TIMEOUT, r), durationMs);
  }

  void handleTimeout(@NonNull SnackbarRecord record) {
    synchronized (lock) {
      if (currentSnackbar == record || nextSnackbar == record) {
        cancelSnackbarLocked(record, Snackbar.Callback.DISMISS_EVENT_TIMEOUT);
      }
    }
  }
}
