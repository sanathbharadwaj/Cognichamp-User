package com.anekvurna.cognichamp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Sanath on 1/5/2018.
 */

public class MyDriversAdapter extends RecyclerView.Adapter<MyDriversAdapter.MyHolder> {

    private List<LocalDriver> localDrivers;
    private Context context;
    private DriverListActivity driverListActivity;


    MyDriversAdapter(List<LocalDriver> users, Context context) {
        this.localDrivers = users;
        this.context = context;
        driverListActivity = (DriverListActivity)context;
    }



    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.driver_element, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, int position) {
        final LocalDriver myLocalDriver = localDrivers.get(position);
        String name = myLocalDriver.getName();
        holder.driverName.setText(name);
        holder.driverMobile.setText(myLocalDriver.getMobile());

        holder.callButton.setTag(position);
        holder.deleteButton.setTag(position);
        holder.editButton.setTag(position);

        if(!myLocalDriver.isCurrent())
        holder.trackDriverButton.setEnabled(false);
        else
            holder.trackDriverButton.setEnabled(true);

        holder.trackDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("trackDriver", true);
                intent.putExtra("driverId", myLocalDriver.getUserId());
                driverListActivity.startActivity(intent);
            }
        });
        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                String mobile = localDrivers.get(tag).getMobile();
                intent.setData(Uri.parse("tel:" + mobile));
                context.startActivity(intent);
            }
        });


        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                alertDelete(tag);
            }
        });

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (int) view.getTag();
                driverListActivity.showEditingPopup(localDrivers.get(tag), tag);
            }
        });

    }

    private void alertDelete(final int tag)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle("Delete Passenger")
                .setMessage("Are you sure you want to delete this passenger?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        driverListActivity.deleteUser(tag);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



    @Override
    public int getItemCount() {
        return localDrivers.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView driverName, driverMobile;
        ImageButton callButton, deleteButton, editButton;
        Button trackDriverButton;
       // private String receiverName;

        MyHolder(View itemView) {
            super(itemView);
            //profilePic = (ImageView) itemView.findViewById(R.id.profile_pic);
            driverName = (TextView) itemView.findViewById(R.id.driver_name);
            driverMobile = itemView.findViewById(R.id.driver_mobile_element);
            deleteButton = itemView.findViewById(R.id.delete_button);
            editButton = itemView.findViewById(R.id.edit_button);
            trackDriverButton = itemView.findViewById(R.id.track_driver_button);
            callButton = (ImageButton) itemView.findViewById(R.id.call_button);

        }
    }
    void showToast(String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }



}
