package com.example.feedback.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.feedback.DataClasses.Assignment;
import com.example.feedback.R;

import java.util.ArrayList;

/**
 * This class is an adapter to display the assignments in the AssignmentsActivity as a list
 */
public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private ArrayList<Assignment> assignmentList;

    public AssignmentAdapter(ArrayList<Assignment> assignmentList) {
        this.assignmentList = assignmentList;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assignment_item_layout, parent, false);
        return new AssignmentViewHolder(view);
    }

    public Assignment getAssignmentAtPosition(int position) {
        return assignmentList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentAdapter.AssignmentViewHolder holder, int position) {
        Assignment currentAssignment = assignmentList.get(position);

        holder.tvAssignmentName.setText(currentAssignment.getName());
        holder.tvAssignmentDescription.setText(currentAssignment.getDescription().length() > 45 ? currentAssignment.getDescription().substring(0, 45) + "..." : currentAssignment.getDescription());
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAssignmentName;
        public TextView tvAssignmentDescription;

        public AssignmentViewHolder(View itemView) {
            super(itemView);
            tvAssignmentName = itemView.findViewById(R.id.tvAssignmentName);
            tvAssignmentDescription = itemView.findViewById(R.id.tvAssignmentDescription);
        }
    }
}
