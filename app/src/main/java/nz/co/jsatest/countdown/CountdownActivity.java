package nz.co.jsatest.countdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * A full-screen activity that shows days and time until an event along with an image that will download and fade in on completion
 * (thanks Glide!).
 *
 * This was created using the Full Screen Activity wizard in Android Studio (~28/09/2016).
 */
public class CountdownActivity extends AppCompatActivity implements DateTimeUrlFragment.Listener {

	private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
	private static final int UI_ANIMATION_DELAY = 1000;
	private static final long UPDATE_DELAY = 61;
	private static final String TAG_DATETIME_SELECTION = "tag_datetime_selection";

	// timezone and event date/time
	private int mEventYear = 2016;
	private int mEventMonth = 10;
	private int mEventDayOfMonth = 5;
	private int mEventHour = 5;
	private int mEventMinute = 0;
	private static final int EVENT_SECOND = 0;
	// url for image
	private String mUrlToEventImage = "https://www.google.co.nz/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";

	// views
	@BindView(R.id.fullscreen_content) View mContentView;
	@BindView(R.id.days_textview) TextView mDaysTextView;
	@BindView(R.id.hours_textview) TextView mHoursTextView;
	@BindView(R.id.minutes_textview) TextView mMinutesTextView;
	@BindView(R.id.seconds_textview) TextView mSecondsTextView;
	@BindView(R.id.milliseconds_textview) TextView mMillisecondsTextView;
	@BindView(R.id.event_imageview) ImageView mEventImageView;

	// state
	private DateTime mEventDateTime;
	private MediaPlayer mPlayer;
	private boolean mVisible;
	private Unbinder mUnbinder;
	private DateTimeUrlFragment mDateTimePickerFragment;

	@Override protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_countdown);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mUnbinder = ButterKnife.bind(this, this);

		// set up mPlayer
		mPlayer = new MediaPlayer();

		// set up date
		updateDateTime();

		// set the state of the system UI visibility
		mVisible = true;

		// Set up the user interaction to manually show or hide the system UI.
		mContentView.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				toggle();
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		mContentView.setOnTouchListener(mDelayHideTouchListener);
		mContentView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				// show the dialog
				mDateTimePickerFragment = DateTimeUrlFragment.newInstance();
				getSupportFragmentManager().beginTransaction().add(android.R.id.content, mDateTimePickerFragment, TAG_DATETIME_SELECTION).commit();
				return false;
			}
		});
		mDaysTextView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override public boolean onLongClick(View v) {
				if(mPlayer.isPlaying()) stopPlayer();
				else playBeep();
				return false;
			}
		});
	}

	/**
	 * Start updating the display on start.
	 */
	@Override protected void onStart() {
		super.onStart();
		mContentView.postDelayed(mUpdateRunnable, UPDATE_DELAY);
	}

	/**
	 * Stop updating when stopping this.
	 */
	@Override protected void onStop() {
		super.onStop();
		mContentView.removeCallbacks(mUpdateRunnable);
		stopPlayer();
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		mUnbinder.unbind();
	}

	/**
	 * Update the event time.
	 */
	private void updateDateTime() {
		mEventDateTime = new DateTime(mEventYear, mEventMonth, mEventDayOfMonth, mEventHour, mEventMinute, EVENT_SECOND);
	}

	/**
	 * A runnable to set update the view and set up another runnable after a delay
	 */
	private Runnable mUpdateRunnable = new Runnable() {
		@Override public void run() {
			mContentView.postDelayed(mUpdateRunnable, UPDATE_DELAY);
			updateView();
		}
	};

	/**
	 * Handle setting the event url and date time from a dialog
	 */
	@Override public void onDateTimeUrlSelected(String url, int year, int month, int dayOfMonth, int hour, int minute) {
		if(!TextUtils.isEmpty(url)) mUrlToEventImage = url;

		mEventYear = year;
		mEventMonth = month;
		mEventDayOfMonth = dayOfMonth;
		mEventHour = hour;
		mEventMinute = minute;
		updateDateTime();

		getSupportFragmentManager().beginTransaction().remove(mDateTimePickerFragment).commit();

		// clear and restart updating
		mContentView.removeCallbacks(mUpdateRunnable);
		mContentView.postDelayed(mUpdateRunnable, UPDATE_DELAY);
	}

	/**
	 * Update the views
	 */
	private void updateView() {
		DateTime now = new DateTime();
		// update the time if we're still before the event time
		boolean afterNow = mEventDateTime.isAfterNow();
		String daysUntilEvent = getDaysUntilEvent(now);

		// update view visibility
		mDaysTextView.setVisibility(daysUntilEvent == null ? GONE : VISIBLE);
		mHoursTextView.setVisibility(afterNow ? VISIBLE : GONE);
		mMinutesTextView.setVisibility(afterNow ? VISIBLE : GONE);
		mSecondsTextView.setVisibility(afterNow ? VISIBLE : GONE);
		mMillisecondsTextView.setVisibility(afterNow ? VISIBLE : GONE);
		mEventImageView.setVisibility(afterNow ? GONE : VISIBLE);

		if(afterNow) {
			// update the text
			mDaysTextView.setText(daysUntilEvent);
			HoursMinutesSecondsMilliseconds timeUntilEvent = getTimeUntilEvent(now);
			mHoursTextView.setText(timeUntilEvent.getHours());
			mMinutesTextView.setText(timeUntilEvent.getMinutes());
			mSecondsTextView.setText(timeUntilEvent.getSeconds());
			mMillisecondsTextView.setText(timeUntilEvent.getMilliseconds());
		} else {
			// load the image
			loadImage();
		}
	}

	/**
	 * Load an image into the ImageView
	 */
	private void loadImage() {
		// stop updating the views
		mContentView.removeCallbacks(mUpdateRunnable);
		// load the image
		Glide.with(this).load(mUrlToEventImage)
				.fitCenter()
				.crossFade(UI_ANIMATION_DELAY)
				.into(mEventImageView);
	}

	/**
	 * Get the text to display "n days" (until the event)
	 *
	 * @param now the current time
	 * @return the number of days until the event "n days"
	 */
	private String getDaysUntilEvent(DateTime now) {
		if (Days.daysBetween(now, mEventDateTime).getDays() == 0) return null;
		return Days.daysBetween(now, mEventDateTime).getDays() + " days";
	}

	/**
	 * Get the text to display "HH:mm:ss:SSS" (excluding days until the event)
	 *
	 * @param now the current time
	 * @return the time until "HH:mm:ss:SSS"
	 */
	private HoursMinutesSecondsMilliseconds getTimeUntilEvent(DateTime now) {
		Calendar nowCalendar = now.toCalendar(Locale.getDefault());
		Calendar eventCalendar = mEventDateTime.toCalendar(Locale.getDefault());
		return formatHoursMinutesSeconds((int) (eventCalendar.getTimeInMillis() - nowCalendar.getTimeInMillis()));
	}

	/**
	 * Takes time to the event in millis and creates a HoursMinutesSecondsMilliseconds
	 * @param millis the time to the event
	 * @return a HoursMinutesSecondsMilliseconds
	 */
	public static HoursMinutesSecondsMilliseconds formatHoursMinutesSeconds(int millis) {
		int hours = (millis / 3600000) % 24;
		int remainder = millis % 3600000;
		int minutes = remainder / 60000;
		int seconds = (millis % 60000) / 1000;
		int milliseconds = millis % 1000;
		return new HoursMinutesSecondsMilliseconds(hours, minutes, seconds, milliseconds);
	}

	/**
	 * Plays a looping sound.
	 */
	public void playBeep() {
		try {
			if (mPlayer.isPlaying()) {
				stopPlayer();
			}

			AssetFileDescriptor descriptor = getAssets().openFd("sounds/Beep.mp3");
			mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();

			mPlayer.prepare();
			mPlayer.setVolume(1f, 1f);
			mPlayer.setLooping(true);
			mPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stops playing the looping sound.
	 */
	private void stopPlayer() {
		mPlayer.stop();
		mPlayer.release();
		mPlayer = new MediaPlayer();
	}


	/* - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * Below this is slightly modified stuff from the fullscreen activity
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - */
	@Override protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the system UI. This is to prevent the jarring behavior of controls
	 * going away while interacting with activity UI.
	 */
	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override public boolean onTouch(View view, MotionEvent motionEvent) {
			if (mVisible)
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			else
				show();
			return false;
		}
	};

	private void toggle() {
		if (mVisible) {
			hide();
		} else {
			show();
		}
	}

	private void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) actionBar.hide();

		mVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi") @Override public void run() {
			// Delayed removal of status and navigation bar

			// Note that some of these constants are new as of API 16 (Jelly Bean)
			// and API 19 (KitKat). It is safe to use them, as they are inlined
			// at compile-time and do nothing on earlier devices.
			if(mContentView != null) mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};

	@SuppressLint("InlinedApi") private void show() {
		// Show the system bar
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;
	}

	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) actionBar.show();
		}
	};

	private final Handler mHideHandler = new Handler();
	private final Runnable mHideRunnable = new Runnable() {
		@Override public void run() {
			hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] mMilliseconds, canceling any previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}