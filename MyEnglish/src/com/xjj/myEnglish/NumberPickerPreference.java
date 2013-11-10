package com.xjj.myEnglish;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NumberPickerPreference extends DialogPreference {

	//缺省值，最小值，最大值
    private static final int DEFAULT_VALUE = 0;
	private static final int mMinValue=0;
    private static final int mMaxValue=60;
    
    private int mCurrentValue;
    private NumberPicker mNumberPicker;
    
	public NumberPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
        setDialogLayoutResource(R.layout.preference_number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        setDialogIcon(null);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		// TODO Auto-generated method stub
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		
	    if (restorePersistedValue) {
	        // Restore existing state
	        mCurrentValue = this.getPersistedInt(DEFAULT_VALUE);
	    } else {
	        // Set default state from the XML attribute
	        mCurrentValue = (Integer) defaultValue;
	        persistInt(mCurrentValue);
	    }
	}
	
	@Override
	protected void onBindDialogView(View view) {
		// TODO Auto-generated method stub
		super.onBindDialogView(view);
		
        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());
 
        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mCurrentValue);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		
	    if (positiveResult) {
	        persistInt(mNumberPicker.getValue());
	    }
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		// TODO Auto-generated method stub
		return a.getInteger(index, DEFAULT_VALUE);
	}

	
	//处理横屏竖屏切换：
	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    // Change this data type to match the type saved by your Preference
	    int value;

	    public SavedState(Parcelable superState) {
	        super(superState);
	    }

	    public SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readInt();  // Change this to read the appropriate data type
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeInt(value);  // Change this to write the appropriate data type
	    }

	    // Standard creator object using an instance of this class
	    public static final Parcelable.Creator<SavedState> CREATOR =
	            new Parcelable.Creator<SavedState>() {

	        public SavedState createFromParcel(Parcel in) {
	            return new SavedState(in);
	        }

	        public SavedState[] newArray(int size) {
	            return new SavedState[size];
	        }
	    };
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
	    final Parcelable superState = super.onSaveInstanceState();
	    // Check whether this Preference is persistent (continually saved)
	    if (isPersistent()) {
	        // No need to save instance state since it's persistent, use superclass state
	        return superState;
	    }

	    // Create instance of custom BaseSavedState
	    final SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current setting value
	    myState.value = mNumberPicker.getValue();
	    return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    SavedState myState = (SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    
	    // Set this Preference's widget to reflect the restored state
	    mNumberPicker.setValue(myState.value);
	}


	
}
