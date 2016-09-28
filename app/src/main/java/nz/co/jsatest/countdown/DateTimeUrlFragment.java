package nz.co.jsatest.countdown;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A fragment enabling the user to select a date and time.
 */

public class DateTimeUrlFragment extends Fragment {


	public interface Listener {
		void onDateTimeUrlSelected(String url, int year, int month, int dayOfMonth, int hour, int minute);
	}


	private Listener mFragmentListener;

	private Unbinder mUnbinder;
	@BindView(R.id.datepicker) DatePicker mDatePicker;
	@BindView(R.id.timepicker) TimePicker mTimePicker;
	@BindView(R.id.ok_button) Button mOkButton;
	@BindView(R.id.url_edittext) EditText mUrlEditText;

	public static DateTimeUrlFragment newInstance() {
		return new DateTimeUrlFragment();
	}

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * on create view
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_fragment_datetime, container, false);
		mUnbinder = ButterKnife.bind(this, view);
		return view;
	}

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	 * lifecycle
	 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Listener) mFragmentListener = (Listener) activity;
		else throw new IllegalStateException("Activity must implement DateTimeDialogFragment Listener");
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		mUnbinder.unbind();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.setRetainInstance(true);

		mOkButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				// get values and pass back to the activity
				mFragmentListener.onDateTimeUrlSelected(
						mUrlEditText.getText().toString(),
						mDatePicker.getYear(),
						mDatePicker.getMonth() + 1,
						mDatePicker.getDayOfMonth(),
						mTimePicker.getCurrentHour(),
						mTimePicker.getCurrentMinute());
			}
		});
	}
}