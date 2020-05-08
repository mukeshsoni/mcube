package com.brizztv.mcube;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

import com.brizztv.mcube.R;

@ReportsCrashes(formKey = 
//							"dFhBcUxHQlNMMjV0QktpZWVlTTJsTUE6M",
							"dFhBcUxHQlNMMjV0QktpZWVlTTJsTUE6MQ",
							mode = ReportingInteractionMode.TOAST,
							forceCloseDialogAfterToast = false, // optional, default false
							resToastText = R.string.crash_toast_text)

public class CrashReporter extends Application {
	@Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
//        ACRA.init(this);
        super.onCreate();
    }
}
