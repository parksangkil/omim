package com.mapswithme.maps.background;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mapswithme.maps.Framework;
import com.mapswithme.util.log.Logger;
import com.mapswithme.util.log.StubLogger;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class WorkerService extends IntentService
{
  private static final String ACTION_PUSH_STATISTICS = "com.mapswithme.maps.action.stat";
  private static final String ACTION_CHECK_UPDATE = "com.mapswithme.maps.action.update";

  private Logger mLogger = StubLogger.get();//SimpleLogger.get("MWMWorkerService");
  private Notifier mNotifier;


  /**
   * Starts this service to perform action  with the given parameters. If the
   * service is already performing a task this action will be queued.
   *
   * @see IntentService
   */
  public static void startActionPushStat(Context context)
  {
    Intent intent = new Intent(context, WorkerService.class);
    intent.setAction(ACTION_PUSH_STATISTICS);
    context.startService(intent);
  }

  /**
   * Starts this service to perform check update action with the given parameters. If the
   * service is already performing a task this action will be queued.
   *
   * @see IntentService
   */
  public static void startActionCheckUpdate(Context context)
  {
    Intent intent = new Intent(context, WorkerService.class);
    intent.setAction(ACTION_CHECK_UPDATE);
    context.startService(intent);
  }

  public WorkerService()
  {
    super("WorkerService");
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    mNotifier = new Notifier(this);
  }

  @Override
  protected void onHandleIntent(Intent intent)
  {
    if (intent != null)
    {
      final String action = intent.getAction();

      if (ACTION_CHECK_UPDATE.equals(action))
        handleActionCheckUpdate();
      else if (ACTION_PUSH_STATISTICS.equals(action))
        handleActionPushStat();
    }
  }

  private void handleActionCheckUpdate()
  {
    mLogger.d("Trying to update");
    if (!Framework.nativeIsDataVersionChanged()) return;

    final String countriesToUpdate = Framework.nativeGetOutdatedCountriesString();
    if (!TextUtils.isEmpty(countriesToUpdate))
    {
      mLogger.d("Update available! " + countriesToUpdate);
      mNotifier.placeUpdateAvailable(countriesToUpdate);
    }
    // We are done with current version
    Framework.nativeUpdateSavedDataVersion();
    mLogger.d("Version updated");
  }

  private void handleActionPushStat()
  {
    // TODO: add server call here
  }
}
