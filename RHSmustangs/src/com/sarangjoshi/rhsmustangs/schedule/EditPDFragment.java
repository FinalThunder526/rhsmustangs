/**
 * EditPeriodFragment.java
 * May 25, 2014
 * Sarang Joshi
 */

package com.sarangjoshi.rhsmustangs.schedule;

import com.sarangjoshi.rhsmustangs.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class EditPDFragment extends DialogFragment {

	public interface EditPeriodDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog,
				String savedName);

		public void onDialogNeutralClick(DialogFragment dialog);

	}

	EditPeriodDialogListener mListener;
	public String hintText;

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
		final EditText edit = (EditText) inflater.inflate(
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
										EditPDFragment.this, savedName);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							}
						})
				.setNeutralButton("Default",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mListener
										.onDialogNeutralClick(EditPDFragment.this);
							}
						});

		return builder.create();
	}
}
