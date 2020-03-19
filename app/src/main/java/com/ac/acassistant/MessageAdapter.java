package com.ac.acassistant;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    List<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            if(message.isTemp()){
                convertView = messageInflater.inflate(R.layout.temp_message, null);
                holder.avatar = convertView.findViewById(R.id.avatar);
                holder.name = convertView.findViewById(R.id.name);
                holder.temp = convertView.findViewById(R.id.message_body);
                holder.messageBody = convertView.findViewById(R.id.desc);
                holder.maxTemp = convertView.findViewById(R.id.max);
                holder.minTemp = convertView.findViewById(R.id.min);
                holder.cloud = convertView.findViewById(R.id.cloud);
                holder.sun = convertView.findViewById(R.id.sun);
                holder.windDirection = convertView.findViewById(R.id.direction);
                holder.windSpeed = convertView.findViewById(R.id.speed);
                holder.speedImage = convertView.findViewById(R.id.speedImage);
                RotateAnimation anim = new RotateAnimation(0f, 350f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setRepeatCount(Animation.INFINITE);
                anim.setDuration(700);

                // Start animating the image
                holder.speedImage.startAnimation(anim);

                convertView.setTag(holder);


                String temp = message.getTemp()[0] + "º";
                String maxTemp = "Máxima: " + message.getTemp()[1] + "º";
                String minTemp = "Mínima: " + message.getTemp()[2] + "º";

                holder.windDirection.setText(message.getTemp()[5]);
                holder.windSpeed.setText(message.getTemp()[6]);
                if(message.getTemp()[3].equals("true")){
                    holder.cloud.setVisibility(View.VISIBLE);
                }else{
                    holder.cloud.setVisibility(View.GONE);
                }
                if(message.getTemp()[4].equals("true")){
                    holder.sun.setImageResource(R.drawable.ic_wb_sunny_black_24dp);
                }else{
                    holder.sun.setImageResource(R.drawable.ic_brightness_3_black_24dp);
                }
                holder.name.setText(message.getData().getName());
                holder.messageBody.setText(message.getText());
                holder.temp.setText(temp);
                holder.maxTemp.setText(maxTemp);
                holder.minTemp.setText(minTemp);
                GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
                //drawable.setColor(Color.parseColor(message.getData().getColor())); Set Drawable Color
            }else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.avatar = convertView.findViewById(R.id.avatar);
                holder.name = convertView.findViewById(R.id.name);
                holder.messageBody = convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);

                holder.name.setText(message.getData().getName());
                holder.messageBody.setText(message.getText());
                GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
                //drawable.setColor(Color.parseColor(message.getData().getColor())); Set Drawable Color
            }
        }

        return convertView;
    }

}

class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
    public TextView temp;
    public TextView maxTemp;
    public TextView minTemp;
    public ImageView sun;
    public ImageView cloud;
    public TextView windDirection;
    public TextView windSpeed;
    public ImageView speedImage;
}