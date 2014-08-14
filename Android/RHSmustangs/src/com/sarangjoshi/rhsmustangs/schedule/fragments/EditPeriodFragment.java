/**
 * EditPeriodFragment.java
 * May 25, 2014
 * Sarang Joshi
 */

package com.sarangjoshi.rhsmustangs.schedule.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.sarangjoshi.rhsmustangs.R;

public class EditPeriodFragment extends DialogFragment {

	public interface EditPeriodDialogListener {
		/**
		 * Callback method when the right-most button is clicked.
		 * 
		 * @param dialog this dialog
		 * @param savedName the Period name
		 */
		public void onDialogPositiveClick(EditPeriodFragment dialog,
				String savedName);

		/**
		 * Callback method when the middle button is clicked.
		 * 
		 * @param dialog this dialog
		 */
		public void onDialogNeutralClick(EditPeriodFragment dialog);

		/**
		 * Callback method when the left-most button is clicked.
		 * 
		 * @param dialog this dialog
		 */
		public void onDialogNegativeClick(EditPeriodFragment dialog);
	}

	EditPeriodDialogListener mListener;
	public String hintText;
	public EditText edit;
	public boolean isKeyboardOpen = true;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (EditPeriodDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EditPeriodDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedStateInstance) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		edit = (EditText) inflater.inflate(
				R.layout.dialog_edittext_class, null);
		edit.setText(hintText);

		edit.requestFocus();

		builder.setTitle("Change class name")
				.setView(edit)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Editable temp = edit.getText();
								String savedName = temp.toString();
								mListener.onDialogPositiveClick(
										EditPeriodFragment.this, savedName);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int which) {
								mListener
										.onDialogNegativeClick(EditPeriodFragment.this);
							}
						})
				.setNeutralButton("Default",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener
										.onDialogNeutralClick(EditPeriodFragment.this);
							}
						});

		return builder.create();
	}


}