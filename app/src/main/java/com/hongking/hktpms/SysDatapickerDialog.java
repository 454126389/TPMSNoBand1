package com.hongking.hktpms;

import com.hongking.oemtpms.R;

import android.app.Dialog;  
import android.content.Context;  
import android.content.DialogInterface;  
//import android.content.res.Resources;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;  
import android.view.Window;
import android.view.ViewGroup.LayoutParams;  
import android.widget.Button;
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
public class SysDatapickerDialog extends Dialog {  
    public SysDatapickerDialog(Context context, int theme) {  
        super(context, theme);  
    }  

    public SysDatapickerDialog(Context context) {  
        super(context);  
    }  

    /**  
     * Helper class for creating a custom dialog  
     */ 

    public static class Builder implements View.OnClickListener,View.OnTouchListener,OnCancelListener{
        private Context context;  
        private String title;  
        private String message;  
        private String positiveButtonText;  
        private String negativeButtonText;  
        private String neutralButtonText;  
        private View contentView;  
        private OnClickListener
                        positiveButtonClickListener,  
                        negativeButtonClickListener,
        				neutralButtonClickListener; 
        OnCancelListener cancelClickListener;
		private boolean AutoDismiss=true;
		
		SysDatapickerDialog dialog;
		
		Button positiveButton;
		Button negativeButton;
		private Button hundredsIncButton;
		private Button hundredsDecButton;
		
		private Button decadeIncButton;
		private Button decadeDecButton;
		
		private Button unitsIncButton;
		private Button unitsDecButton;
		
		
		private SysParam.DataInfo info;
		private TextView hundredsText; 
		private TextView decadeText;
		private TextView unitsText;
		
		private TextView symbolText;
		int TempValue;
		private int theme;
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
    
        /**  
         * Set the positive button resource and it's listener  
         * @param positiveButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setPositiveButton(int positiveButtonText,  
                OnClickListener listener) {
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
                OnClickListener listener) {
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
                OnClickListener listener) {
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
                OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;  
            this.negativeButtonClickListener = listener;  
            return this;  
        } 
        
        
        /**  
         * Set the neutral button resource and it's listener  
         * @param negativeButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setNeutralButton(int neutraleButtonText,  
                OnClickListener listener) {
            this.neutralButtonText = (String) context  
                    .getText(neutraleButtonText);  
            this.neutralButtonClickListener = listener;  
            return this;  
        }  

    

        /**  
         * Set the negative button text and it's listener  
         * @param negativeButtonText  
         * @param listener  
         * @return  
         */ 
        public Builder setNeutralButton(String neutralButtonText,  
                OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;  
            this.neutralButtonClickListener = listener;  
            return this;  
        } 
        /**  
         * Create the custom dialog  
         */ 
		/*
        private Resources getResources() {
	    	// TODO Auto-generated method stub
	    	Resources mResources = null;
	    	mResources = getResources();
	    	return mResources;
    	}   
		*/     
        public SysDatapickerDialog create(View layout,SysParam.DataInfo value,boolean mode) {  
            //LayoutInflater inflater = (LayoutInflater) context  
             //       .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            // instantiate the dialog with the custom Theme  
        	Window  mWindow;
        	if(context == null){
        		Log.i("dialog","context is destory");
        	}
        	AutoDismiss = mode;
        	info = value;
            dialog = new SysDatapickerDialog(context,theme);  
            //View layout = inflater.inflate(R.layout.sys_custom_dialog, null);  
            mWindow = dialog.getWindow();
            
            mWindow.requestFeature(Window.FEATURE_NO_TITLE);
            mWindow.setContentView(layout);  
            
            // set the dialog title  
            ((TextView) layout.findViewById(R.id.title)).setText(title);  
            /*--+号控件监听----*/
            TempValue = info.value;
            
            if(info.baxMax > 100 || info.baxMove >= 100){
            	hundredsIncButton = ((Button) layout.findViewById(R.id.hundredsIncButton));
            	hundredsDecButton = ((Button) layout.findViewById(R.id.hundredsDecButton));
            	hundredsText = ((TextView) layout.findViewById(R.id.hundredsText));
            	
            	//hundredsIncButton.setOnTouchListener(this); 
            	//hundredsDecButton.setOnTouchListener(this); 
            	hundredsIncButton.setOnClickListener(this);
            	hundredsDecButton.setOnClickListener(this);
            	
            }else{
            	(layout.findViewById(R.id.hundreds)).setVisibility(View.GONE);//隐藏视图
            }
            if(info.baxMove >= 100){
            	((TextView) layout.findViewById(R.id.hundredsPoint)).setText(".");
            	
            }else if(info.baxMove >= 10){
            	((TextView) layout.findViewById(R.id.decadePoint)).setText(".");
            }else{}
            decadeText = ((TextView) layout.findViewById(R.id.decadeText));
            unitsText = ((TextView) layout.findViewById(R.id.unitsText));
            symbolText = ((TextView) layout.findViewById(R.id.symbolTitle));
            final TextView UnitTitle = ((TextView) layout.findViewById(R.id.unitTitle)); 
            final TextView scopeText = ((TextView) layout.findViewById(R.id.scopeText));
            //SysApplication app;
            //app = (SysApplication)getApplicationContext();
            if(info.baxMove >= 10){
            	scopeText.setText(context.getResources().getString(R.string.SettingRange)+(float)((float)info.baxMin/info.baxMove) +"~"+(float)((float)info.baxMax/info.baxMove));
            }
        	else{
            	scopeText.setText(context.getResources().getString(R.string.SettingRange)+info.baxMin/info.baxMove +"~"+info.baxMax/info.baxMove);
        	}
        	UnitTitle.setText(info.unitName);
        	displayUpdate();
            
            decadeIncButton = ((Button) layout.findViewById(R.id.decadeIncButton));
        	decadeDecButton = ((Button) layout.findViewById(R.id.decadeDecButton));
        	
        	
        	//decadeIncButton.setOnTouchListener(this); 
        	decadeIncButton.setOnClickListener(this);
        	
        	//decadeDecButton.setOnTouchListener(this); 
        	decadeDecButton.setOnClickListener(this);
        	
        	
        	unitsIncButton = ((Button) layout.findViewById(R.id.unitsIncButton));
        	unitsDecButton = ((Button) layout.findViewById(R.id.unitsDecButton));
        	
        	
        	//unitsIncButton.setOnTouchListener(this); 
        	//unitsDecButton.setOnTouchListener(this); 
        	unitsIncButton.setOnClickListener(this);
        	unitsDecButton.setOnClickListener(this);
        	
                        // set the confirm button  
            if (positiveButtonText != null) {  
            	positiveButton =  ((Button) layout.findViewById(R.id.positiveButton));  
                
            	positiveButton.setText(positiveButtonText); 
            	
            	positiveButton.setOnClickListener(this);
            	//positiveButton.setOnTouchListener(this);  
                if (positiveButtonClickListener != null) {  
                	
                }  
            } 
            // set the cancel button  
            if (negativeButtonText != null) {  
            	negativeButton =  ((Button) layout.findViewById(R.id.negativeButton));
            	negativeButton.setText(negativeButtonText);  
            	
                
                negativeButton.setOnClickListener(this);
                //negativeButton.setOnTouchListener(this); 
                if (negativeButtonClickListener != null) {  
                   
                }  
            }
            dialog.setOnCancelListener(this);
            // set the content message  
            if (message != null) {  
                ((TextView) layout.findViewById(  
                        R.id.message)).setText(message);  
            } else if (contentView != null) {  
                // if no message set  
                // add the contentView to the dialog body  
                ((LinearLayout) layout.findViewById(R.id.content))  
                        .removeAllViews();  
                ((LinearLayout) layout.findViewById(R.id.content))  
                        .addView(contentView,   
                                new LayoutParams(  
                                        LayoutParams.WRAP_CONTENT,   
                                        LayoutParams.WRAP_CONTENT));  
            }
            //dialog.setContentView(R.layout.sys_custom_dialog);
    		//dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); 
    		//dialog.setContentView(layout);  

            return dialog;  
        }
		@Override
    	public void onClick(View v) {
        	if(v == positiveButton){
        		//TPMSParam.tyreData.setPressCapaticy(TempValue);
        		info.value = TempValue;
				if (positiveButtonClickListener != null) {  
					positiveButtonClickListener.onClick(  
		                    dialog,   
		                    DialogInterface.BUTTON_POSITIVE);           	
				 }  
        		
        		if(AutoDismiss)
        			dialog.dismiss();
        		return;
        	}else if(v == negativeButton){
        		if (negativeButtonClickListener != null) {  
        			negativeButtonClickListener.onClick(  
                            dialog,   
                            DialogInterface.BUTTON_NEGATIVE); 
                }  
        		 
        		if(AutoDismiss)
        			dialog.dismiss();
        		return;
        	}
        	else if(v == hundredsIncButton){
        		if(info.baxMax >= (TempValue+100))
        			TempValue += 100;
	
        	}
        	else if(v == hundredsDecButton){
        		if(info.baxMin <= (TempValue-100))
        			TempValue -= 100;
	
        	}
        	else if(v == decadeIncButton){
        		if(info.baxMax >= (TempValue+10))
        			TempValue += 10;
	
        	}
        	else if(v == decadeDecButton){
        		if(info.baxMin <= (TempValue-10))
        			TempValue -= 10;
	
        	}
        	else if(v == unitsIncButton){
        		if(info.baxMax >= (TempValue+1))
        			TempValue++;
	
        	}
        	else if(v == unitsDecButton){
        		if(info.baxMin <= (TempValue-1))
        			TempValue--;
	
        	}
        	displayUpdate();   
        	info.value = TempValue;
			if (neutralButtonClickListener != null) {  
				neutralButtonClickListener.onClick(  
	                    dialog,   
	                    DialogInterface.BUTTON_NEUTRAL);           	
			 } 
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
					
					v.getBackground().setAlpha(255);//还原图片
					v.invalidate();	
					
			}                               
			return false; 	
		}  
        void displayUpdate(){
        	int temp;
        	if(TempValue<0){
        		symbolText.setText("-");
        	}else
        		symbolText.setText("");
        	
        	temp = Math.abs(TempValue);
        	if(info.baxMax > 100 || info.baxMove >= 100){
        		hundredsText.setText(""+temp/100);
        		
        	}
    		decadeText.setText(""+(temp/10)%10);
    		unitsText.setText(""+temp%10);      	
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
