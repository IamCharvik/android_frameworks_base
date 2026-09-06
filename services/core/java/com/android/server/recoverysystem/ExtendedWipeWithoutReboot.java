package com.android.server.recoverysystem;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;

import com.android.server.LocalServices;
import com.android.server.StorageManagerInternal;
import com.android.server.pm.UserManagerInternal;

class ExtendedWipeWithoutReboot {
    static final String TAG = "ExtWipeWithoutReboot";

    static void run() {
        eraseSecureElement();

        StorageManagerInternal storageManager =
                LocalServices.getService(StorageManagerInternal.class);
        UserManagerInternal userManager = LocalServices.getService(UserManagerInternal.class);
        if (storageManager == null || userManager == null) {
            Slog.e(TAG, "Required storage or user manager service is unavailable");
            return;
        }

        for (int userId : userManager.getUserIds()) {
            Slog.d(TAG, "destroying storage keys for user " + userId);
            try {
                // Use the API available on the Halogen base. This destroys both CE and DE
                // keys for internal and adoptable storage.
                storageManager.destroyUserStorageKeys(userId);
            } catch (Throwable e) {
                Slog.e(TAG, "failed to destroy storage keys for user " + userId, e);
            }
        }
    }

    private static void eraseSecureElement() {
        Slog.d(TAG, "eraseSecureElement start");
        String prop = "sys.erase_secure_element";
        try {
            SystemProperties.set(prop, "start");
            for (int i = 0; i < 500; ++i) {
                // wait at most ~2.5 seconds
                SystemClock.sleep(5);
                if ("finished".equals(SystemProperties.get(prop))) {
                    Slog.d(TAG, "eraseSecureElement end");
                    return;
                }
            }
            Slog.e(TAG, "eraseSecureElement timeout");
        } catch (Throwable e) {
            Slog.e(TAG, "", e);
        }
    }

}
