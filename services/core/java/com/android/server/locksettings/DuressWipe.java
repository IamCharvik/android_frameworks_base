package com.android.server.locksettings;

import android.content.Context;
import android.os.SystemClock;
import android.util.Slog;

import com.android.server.power.PowerManagerService;
import com.android.server.recoverysystem.RecoverySystemService;

public class DuressWipe {
    static final String TAG = DuressWipe.class.getSimpleName();

    // used only for testing, guarded by owner credential
    public static boolean sleep5sBeforePoweroff;

    static void run(Context context) {
        Slog.d(TAG, "start");

        Slog.d(TAG, "calling deleteSecrets");
        // deleteSecrets() calls AndroidKeyStoreMaintenance.deleteAllKeys(), which deletes all
        // KeyMint keys, including the storage encryption keys
        RecoverySystemService.deleteSecrets();
        Slog.d(TAG, "deleteSecrets returned");

        if (sleep5sBeforePoweroff) {
            SystemClock.sleep(5000);
        }

        PowerManagerService.lowLevelShutdown(null);
    }
}
