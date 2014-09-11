/**
 * SDemoFragment.java
 * Sep 8, 2014
 * Sarang Joshi
 */

package com.sarangjoshi.rhsmustangs.schedule.demo;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.sarangjoshi.rhsmustangs.R;
import com.sarangjoshi.rhsmustangs.schedule.SStatic;

public class SDemoFragment extends Fragment {
	int mNum = 0;

	/**
	 * Create a new instance of CountingFragment, providing "num" as an
	 * argument.
	 */
	public static SDemoFragment newInstance(int num) {
		SDemoFragment f = new SDemoFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
			mNum = getArguments().getInt("num");
	}

	/**
	 * The Fragment is created here.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.pager_item_fragment,
				container, false);
		final ImageView x = (ImageView) v.findViewById(R.id.pagerItemImage);

		Bitmap b = ((SDemoActivity) getActivity()).getBitmap(mNum);
		x.setImageBitmap(b);
		/*
		 * switch (mNum) { case 0: x.setImageResource(R.drawable.demo0); break;
		 * case 1: x.setImageResource(R.drawable.demo1); break; case 2:
		 * x.setImageResource(R.drawable.demo2); break; case 3:
		 * x.setImageResource(R.drawable.demo3); break; case 4:
		 * x.setImageResource(R.drawable.demo4); break; }
		 */

		return x;

	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewRef;
		private int data = 0;
		
		public BitmapWorkerTask(ImageView imageView) {
			imageViewRef = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(Integer... params) {
			data = params[0];
			return SStatic.decodeBitmapFromRes(getResources(), data, params[1], params[2]);
		}

	}
}