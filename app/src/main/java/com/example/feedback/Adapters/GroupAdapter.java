package com.example.feedback.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feedback.DataClasses.Group;
import com.example.feedback.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class is an adapter to display the groups in the GroupsActivity as a list
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private ArrayList<Group> groupList;

    public GroupAdapter(ArrayList<Group> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item_layout, parent, false);

        int[] androidColors = view.getResources().getIntArray(R.array.androidcolors);
        int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
        view.setBackgroundColor(randomAndroidColor);

        return new GroupViewHolder(view);
    }

    public Group getGroupAtPosition(int position) {
        return groupList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group currentGroup = groupList.get(position);

        holder.tvGroupName.setText(currentGroup.getGroupName());
        holder.tvLeaderName.setText(currentGroup.getLeader().getUsername());


    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public TextView tvGroupName;
        public TextView tvLeaderName;

        public GroupViewHolder(View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvLeaderName = itemView.findViewById(R.id.tvLeaderName);
        }
    }
}
