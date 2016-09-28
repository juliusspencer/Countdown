package nz.co.jsatest.countdown;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by julius on 27/09/16.
 */

public class CDApplication extends Application {

	@Override public void onCreate() {
		super.onCreate();
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/DS-DIGIT.TTF")
				.setFontAttrId(R.attr.fontPath)
				.build()
		);
	}

}
