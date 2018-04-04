package com.hongking.hktpms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hongking.oemtpms.R;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 
 * Create custom Dialog windows for your application Custom dialogs rely on
 * custom layouts wich allow you to create and use your own look & feel.
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey
 * 
 */
public class SysRadioButtonDialog extends Dialog {
	public SysRadioButtonDialog(Context context, int theme) {
		super(context, theme);
	}

	public SysRadioButtonDialog(Context context) {
		super(context);
	}

	public class exitBoardcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// if(D) Log.e(TAG, "** ON RECEIVE **");
			String action = intent.getAction();
			if (action.equals(SysActivity.AUTODESTORY)) {
				SysRadioButtonDialog.this.dismiss();
			} else {
				// Log.e(TAG, "another action: " + action);
			}
		}
	}

	/**
	 * Helper class for creating a custom dialog
	 */

	public static class Builder implements View.OnClickListener, View.OnTouchListener, OnItemClickListener,
			DialogInterface.OnCancelListener {
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;
		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener,
				listViewClickListener;
		DialogInterface.OnCancelListener cancelClickListener;
		private boolean AutoDismiss = true;
		private CharSequence StringArray[];
		private int item;
		private int theme;
		SysRadioButtonDialog dialog;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ListView listView;
		Button positiveButton;
		Button negativeButton;

		public Builder(Context context) {
			this.context = context;
			this.theme = R.style.dialogStyle;
		}

		/**
		 * Set the Dialog message from String
		 * 
		 * @param title
		 * @return
		 */

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource
		 * 
		 * @param title
		 * @return
		 */

		public Builder setMessage(int message) {
			this.message = (String) context.getText(message);
			return this;
		}

		public Builder setSingleChoiceItems(int which) {
			this.item = which;
			return this;
		}

		public Builder setSingleChoiceItems(CharSequence[] items, int which, DialogInterface.OnClickListener listener) {
			this.item = which;
			this.StringArray = items;
			this.listViewClickListener = listener;
			return this;
		}

		/**
		 * Set the Dialog title from resource
		 * 
		 * @param title
		 * @return
		 */

		public Builder setTitle(int title) {
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}

		public Builder setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
			this.cancelClickListener = onCancelListener;
			return this;

		}

		/**
		 * Set the negative button text and it's listener
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		private List<Map<String, Object>> getData() {
			list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < StringArray.length; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("icon", R.drawable.check);
				map.put("soundtype", StringArray[i]);
				list.add(map);

			}

			return list;
		}

		/**
		 * Create the custom dialog
		 */
		public SysRadioButtonDialog create(View layout, boolean mode) {
			// LayoutInflater inflater = (LayoutInflater) context
			// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// instantiate the dialog with the custom Theme
			if (context == null) {
				Log.i("dialog", "context is destory");
			}
			AutoDismiss = mode;
			dialog = new SysRadioButtonDialog(context, theme);
			// View layout = inflater.inflate(R.layout.sys_custom_dialog, null);
			dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			// set the dialog title
			((TextView) layout.findViewById(R.id.title)).setText(title);

			if (StringArray != null) {
				listView = (ListView) layout.findViewById(R.id.ListView01);
				SimpleAdapter notes = new SimpleAdapter(context, getData(), R.layout.list_type5,
						new String[] { "icon", "soundtype" }, new int[] { R.id.icon, R.id.soundtype });
				listView.setAdapter(notes);
				listView.setSelection(item);
				// listView.setEnabled(false);
				updateListView(-1, item);
				listView.setOnItemClickListener(this);

			}
			// set the confirm button
			if (positiveButtonText != null) {
				positiveButton = ((Button) layout.findViewById(R.id.positiveButton));
				positiveButton.setText(positiveButtonText);

				positiveButton.setOnClickListener(this);
				// positiveButton.setOnTouchListener(this);
				if (positiveButtonClickListener != null) {

				}
			}
			// set the cancel button
			if (negativeButtonText != null) {
				negativeButton = ((Button) layout.findViewById(R.id.negativeButton));
				negativeButton.setText(negativeButtonText);

				negativeButton.setOnClickListener(this);
				// negativeButton.setOnTouchListener(this);
				if (negativeButtonClickListener != null) {

				}
			}
			dialog.setOnCancelListener(this);

			// set the content message
			if (message != null) {
				((TextView) layout.findViewById(R.id.message)).setText(message);
			} else if (contentView != null) {
				// if no message set
				// add the contentView to the dialog body
				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
				((LinearLayout) layout.findViewById(R.id.content)).addView(contentView,
						new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			// dialog.setContentView(R.layout.sys_custom_dialog);
			// dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			// dialog.setContentView(layout);

			// Drawable dr =
			// context.getResources().getDrawable(R.drawable.dialog_bg1);

			// dialog.getWindow().setLayout(400, 380);
			return dialog;
		}

		@Override
		public void onClick(View v) {
			if (v == positiveButton) {
				if (positiveButtonClickListener != null)
					positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
			} else if (v == negativeButton) {
				if (negativeButtonClickListener != null)
					negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}

			if (AutoDismiss)
				dialog.dismiss();

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// 更改为按下时的背景图片
				// ((ImageView)v).getDrawable().setAlpha(150);//
				// ((ImageView)v).invalidate();
				// 更改为按下时的背景图片
				v.getBackground().setAlpha(150);//
				v.invalidate();

			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// 改为抬起时的图片
				// ((ImageView)v).getDrawable().setAlpha(255);//还原图片
				// ((ImageView)v).invalidate();
				v.getBackground().setAlpha(255);// 还原图片
				v.invalidate();

			}
			return false;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			updateListView(item, arg2);
			item = arg2;
			if (listViewClickListener != null)
				listViewClickListener.onClick(dialog, arg2);
		}

		void updateListView(int convert, int item) {
			if (convert == item)
				return;
			ListAdapter la = listView.getAdapter();
			HashMap<String, Object> map = (HashMap<String, Object>) list.get(item);
			map.put("icon", R.drawable.check_);
			if (convert >= 0) {
				map = (HashMap<String, Object>) list.get(convert);
				map.put("icon", R.drawable.check);
			}
			((SimpleAdapter) la).notifyDataSetChanged();// 通知系统，实时更新

		}

		@Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			if (cancelClickListener != null) {
				cancelClickListener.onCancel(dialog);
			} else {
				if (negativeButtonClickListener != null)
					negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
			}
		}
	}

}
