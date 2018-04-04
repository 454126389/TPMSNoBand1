package com.hongking.hktpms;

import java.util.List;
import java.util.Map;

import com.hongking.oemtpms.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class SysListviewbase extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Map<String, Object>> mData;
	private float density=1;

	// private List<String> listTag;
	public SysListviewbase(Context context, List<Map<String, Object>> mData,float density) {
		this.mInflater = LayoutInflater.from(context);
		this.mData = mData;
		this.density = density;
		// listTag = new ArrayList<String>();
		// listTag.add(" ");
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// 如果-开头，则该项不可选
		if (mData.get(position).get("flag") != null) {
			return (!mData.get(position).get("flag").equals("tag"));
		} else
			return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		// 根据标签类型加载不同的布局模板
		if (mData.get(position).get("flag") == null || mData.get(position).get("flag").equals("tag")) {
			// 如果是标签项

			// 显示名称
			if (mData.get(position).get("title").equals(" ")) {
				view = mInflater.inflate(R.layout.group_list_item_tag, null);
				TextView textViewinfo = (TextView) view.findViewById(R.id.group_list_item_text);
				textViewinfo.setText((String) mData.get(position).get("title"));
			} else {
				view = mInflater.inflate(R.layout.group_list_item_title, null);
				TextView textView = (TextView) view.findViewById(R.id.group_list_item_text);
				textView.setText((String) mData.get(position).get("title"));
			}
		} else {
			// 否则就是数据项
			if(density==1.5){
				view = mInflater.inflate(R.layout.list_type1den15, null);
			}else {
				view = mInflater.inflate(R.layout.list_type1, null);
			}
			View bgview = (View) view.findViewById(R.id.bgview);
			// bgview.setBackgroundResource(getstrokebg((String)mData.get(position).get("flag")));

			View listview = (View) view.findViewById(R.id.listview);
			// listview.setBackgroundResource(getbg((String)mData.get(position).get("flag")));
			TextView textViewTitle = (TextView) view.findViewById(R.id.title);
			textViewTitle.setText((String) mData.get(position).get("title"));

			if ((Integer) mData.get(position).get("imgTitle") != null) {
				ImageView ImageViewInfo = (ImageView) view.findViewById(R.id.ImageTitle);
				ImageViewInfo.setImageResource((Integer) mData.get(position).get("imgTitle"));
			}
			if ((String) mData.get(position).get("info") != null) {
				TextView textViewinfo = (TextView) view.findViewById(R.id.info);
				textViewinfo.setText((String) mData.get(position).get("info"));
			}
			if ((Integer) mData.get(position).get("img") != null) {
				ImageView ImageViewInfo = (ImageView) view.findViewById(R.id.ImageInfo);
				ImageViewInfo.setImageResource((Integer) mData.get(position).get("img"));
			}
			// ImageView imageView = (ImageView)view.findViewById(R.id.img);
			// imageView.setBackgroundResource((Integer)mData.get(position).get("img"));
		}

		// 返回重写的view
		return view;
	}

	private int getbg(String flag) {
		// TODO Auto-generated method stub
		if (flag.equals("single")) { // 仅一项
			return R.drawable.list_corner_round;
		} else if (flag.equals("top")) { // 第一项
			return R.drawable.list_corner_round_top;
		} else if (flag.equals("bottom")) {// 最后一项
			return R.drawable.list_corner_round_bottom;
		} else
			return R.drawable.list_corner_shape; // 中间项
	}

	private int getstrokebg(String flag) {
		// TODO Auto-generated method stub
		if (flag.equals("single")) { // 仅一项
			return R.drawable.background_corner_round;
		} else if (flag.equals("top")) { // 第一项
			return R.drawable.background_corner_round_top;
		} else if (flag.equals("bottom")) {// 最后一项
			return R.drawable.background_corner_round_bottom;
		} else
			return R.drawable.background_corner_shape; // 中间项
	}
	/*
	 * private final class viewholder{ private TextView title; private TextView
	 * info; private ImageView img; }
	 */

}
