package nz.co.jsatest.countdown;

/**
 * A class to handle parsing time to be read.
 */

public class HoursMinutesSecondsMilliseconds {
	int mHours;
	int mMinutes;
	int mSeconds;
	int mMilliseconds;

	public HoursMinutesSecondsMilliseconds(int hours, int minutes, int seconds, int milliseconds) {
		mHours = hours;
		mMinutes = minutes;
		mSeconds = seconds;
		mMilliseconds = milliseconds;
	}

	public String getHours() {
		StringBuffer sb = new StringBuffer();
		if (mHours < 10) sb.append(0);
		sb.append(mHours);
		sb.append(":");
		return sb.toString();
	}

	public String getMinutes() {
		StringBuffer sb = new StringBuffer();
		if (mMinutes < 10) sb.append("0");
		sb.append(mMinutes);
		sb.append(":");
		return sb.toString();
	}

	public String getSeconds() {
		StringBuffer sb = new StringBuffer();
		if (mSeconds < 10) sb.append("0");
		sb.append(mSeconds);
		sb.append(":");
		return sb.toString();

	}

	public String getMilliseconds() {
		StringBuffer sb = new StringBuffer();
		if (mMilliseconds < 100) sb.append("0");
		if (mMilliseconds < 10) sb.append("0");
		sb.append(mMilliseconds);
		return sb.toString();
	}

}