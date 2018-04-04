package com.hongking.hktpms;

import com.hongking.oemtpms.R;
import android.app.Dialog;  
import android.content.BroadcastReceiver;
import android.content.Context;  
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;  
import android.view.ViewGroup.LayoutParams;  
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;  
import android.widget.TextView;  
/**  
 *   
 * Create custom Dialog windows for your application  
 * Custom dialogs rely on custom layouts wich allow you to   
 * create and use your own look & feel.  
 *   
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html  
 *   
 * @author antoine vianey  
 *  
 */ 
public class SysCustomDialog extends Dialog {  
	
	
    public SysCustomDialog(Context context, int theme) {  
        super(context, theme);  
    }  

    public SysCustomDialog(Context context) {  
        super(context);  
    }  
   
    public class exitBoardcast extends BroadcastReceiver {
		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//if(D) Log.e(TAG, "** ON RECEIVE **");
			String action  = intent.getAction();
			if (action.equals(SysActivity.AUTODESTORY)){
				SysCustomDialog.this.dismiss();
			}
			else{
				//Log.e(TAG, "another action: " + action);
			}
		}
	} 
    /**  
     * Helper class for creating a custom dialog  
     */ 

    public static class Builder implements View.OnClickListener,View.OnTouchListener,DialogInterface.OnCancelListener{  
        private Context context;  
        private String title;  
        private String message;  
        private String positiveButtonText;  
        private String negativeButtonText;  
        private View contentView;  
        private int titleIcon;
        private int theme;
        private DialogInterface.OnClickListener   
                        positiveButtonClickListener,  
                        negativeButtonClickListener;  
        DialogInterface.OnCancelListener cancelClickListener;  
		private boolean AutoDismiss=true;
		SysCustomDialog dialog;
		
		Button positiveButton;
		Button negativeButton;
        public Builder(Context context) {  
            this.context = context; 
            this.theme = R.style.dialogStyle; 
        } 
         
        
        /**  
         * Set the Dialog message from String  
         * @param title  
         * @return  
         */ 

        public Builder setMessage(String message) {  
            this.message = message;  
            return this;  
        }  
    
        /**  
         * Set the Dialog message from resource  
         * @param title  
         * @return  
         */ 

        public Builder setMessage(int message) {  
            this.message = (String) context.getText(message);  
            return this;  
        }  

    

        /**  
         * Set the Dialog title from resource  
         * @param title  
         * @return  
         */ 

        public Builder setTitle(int title) {  
            this.title = (String) context.getText(title);  
            return this;  
        }  
    

        /**  
         * Set the Dialog title from String  
         * @param title  
         * @return  
         */ 
        public Builder setTitle(String title) {  
            this.title = title;  
            return this;  
        }  
        public Builder setIcon(int titleIcon) {  
            this.titleIcon = titleIcon;  
            return this;  
        }  

        /**  
         * Set a custom content view for the Dialog.  
         * If a message is set, the contentView is not  
         * added to the Dialog...  
         * @param v  
         * @return  
         */ 
        public Builder setContentView(View v) {  
            this.contentView = v;  
            return this;  
        }

//        public Builder setSingleChoiceItems(String[] date,checkedItem) {
//            this.contentView = v;
//            return this;
//        }


        /**  
         * Set the positive button resource and it's listener  
         * @param positiveButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setPositiveButton(int positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = (String) context  
                    .getText(positiveButtonText);  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
    

        /**  
         * Set the positive button text and it's listener  
         * @param positiveButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setPositiveButton(String positiveButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.positiveButtonText = positiveButtonText;  
            this.positiveButtonClickListener = listener;  
            return this;  
        }  
    

        /**  
         * Set the negative button resource and it's listener  
         * @param negativeButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setNegativeButton(int negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = (String) context  
                    .getText(negativeButtonText);  
            this.negativeButtonClickListener = listener;  
            return this;  
        }  

    

        /**  
         * Set the negative button text and it's listener  
         * @param negativeButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setNegativeButton(String negativeButtonText,  
                DialogInterface.OnClickListener listener) {  
            this.negativeButtonText = negativeButtonText;  
            this.negativeButtonClickListener = listener;  
            return this;  
        }  
    
        /**  
         * Create the custom dialog  
         */ 
        public SysCustomDialog create(View layout,boolean mode) {  
            //LayoutInflater inflater = (LayoutInflater) context  
             //       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
        	Window  mWindow;
        	AutoDismiss = mode;
        	
        	
        	if(context == null){
        		Log.i("dialog","context is destory");
        	}
            dialog = new SysCustomDialog(context,theme);  
            //View layout = inflater.inflate(R.layout.sys_custom_dialog, null);  
            mWindow = dialog.getWindow();
            
            mWindow.requestFeature(Window.FEATURE_NO_TITLE);
            mWindow.setContentView(layout);  
            //dialog.addContentView(layout, new LayoutParams(  
            //        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
            // set the dialog title 
            if(title != null || titleIcon != 0){
            	mWindow.findViewById(R.id.titleView).setVisibility(View.VISIBLE);   
            if(title != null )
               ((TextView) mWindow.findViewById(R.id.title)).setText(title);  
            if(titleIcon != 0){
            	((ImageView)(mWindow.findViewById(R.id.titleIcon))).setImageResource(titleIcon); 
            }}
            // set the confirm button  
            if (positiveButtonText != null) {  
            	positiveButton =  ((Button) mWindow.findViewById(R.id.positiveButton)); 
            	positiveButton.setText(positiveButtonText);
                if (positiveButtonClickListener != null) {  
                	 
                          
                	positiveButton.setOnClickListener(this);
                	//positiveButton.setOnTouchListener(this);  
                }  
            }else{
            	positiveButton =  ((Button) mWindow.findViewById(R.id.positiveButton)); 
            	if(positiveButton != null)
            		positiveButton.setVisibility(View.GONE);
            	
            }
            // set the cancel button  
            if (negativeButtonText != null) {  
            	negativeButton =  ((Button) mWindow.findViewById(R.id.negativeButton));
            	negativeButton.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {  
                  
                  
                  negativeButton.setOnClickListener(this);
                  //negativeButton.setOnTouchListener(this);  
                }  
            }
            else{
            	negativeButton =  ((Button) mWindow.findViewById(R.id.negativeButton)); 
            	if(negativeButton != null)
            	negativeButton.setVisibility(View.GONE);
            	
            } 
            dialog.setOnCancelListener(this);
            // set the content message  
            if (message != null) {  
                ((TextView) mWindow.findViewById(  
                        R.id.message)).setText(message);  
            } else if (contentView != null) {  
                // if no message set  
                // add the contentView to the dialog body  
                ((LinearLayout) mWindow.findViewById(R.id.content))  
                        .removeAllViews();  
                ((LinearLayout) mWindow.findViewById(R.id.content))  
                        .addView(contentView,   
                                new LayoutParams(  
                                        LayoutParams.WRAP_CONTENT,   
                                        LayoutParams.WRAP_CONTENT));  
            }
            //dialog.setContentView(R.layout.sys_custom_dialog);
    		//dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background); 
    		
    		
    		
    		
    		
    		//Drawable dr = context.getResources().getDrawable(R.drawable.dialog_bg1);
    		
    		//dialog.getWindow().setLayout(400, 380); 
            //dialog.AutoDestroryListen();
            return dialog;  
        }
        @Override
    	public void onClick(View v) {
        	if(v == positiveButton){
        	positiveButtonClickListener.onClick(  
                    dialog,   
                    DialogInterface.BUTTON_POSITIVE);  
        	}else if(v == negativeButton){
        		negativeButtonClickListener.onClick(  
                        dialog,   
                        DialogInterface.BUTTON_NEGATIVE);  
        	}
        	
        	if(AutoDismiss)
    			dialog.dismiss();
        	
        }
        @Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				//更改为按下时的背景图片 
				//((ImageView)v).getDrawable().setAlpha(150);//
				//((ImageView)v).invalidate();
				//更改为按下时的背景图片 
				v.getBackground().setAlpha(150);//
				v.invalidate();

			}else if(event.getAction() == MotionEvent.ACTION_UP) 
			{ 
					//改为抬起时的图片 
				//((ImageView)v).getDrawable().setAlpha(255);//还原图片
				//((ImageView)v).invalidate();	
				v.getBackground().setAlpha(255);//还原图片
				v.invalidate();	

			}                               
			return false; 	
		}
        @Override
		public void onCancel(DialogInterface dialog) {
			// TODO Auto-generated method stub
			if(cancelClickListener != null){
				cancelClickListener.onCancel(dialog);
			}else{
				if(negativeButtonClickListener != null)
	        		negativeButtonClickListener.onClick(  
	                 dialog,DialogInterface.BUTTON_NEGATIVE); 
			}
		}


       
    
    }    
   
} 
