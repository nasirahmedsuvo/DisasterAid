package com.example.disasteraid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.disasteraid.R;
import com.example.disasteraid.models.Volunteer;

import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    public interface VolunteerActionListener {
        void onEdit(Volunteer volunteer);
        void onDelete(Volunteer volunteer);
    }

    private final List<Volunteer> volunteerList;
    private final String currentUserId;
    private final VolunteerActionListener listener;

    public VolunteerAdapter(
            List<Volunteer> volunteerList,
            String currentUserId,
            VolunteerActionListener listener) {

        this.volunteerList = volunteerList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_volunteer, parent, false);

        return new VolunteerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull VolunteerViewHolder holder, int position) {

        Volunteer volunteer = volunteerList.get(position);

        holder.tvName.setText(volunteer.getName());
        holder.tvPhone.setText("Phone: " + volunteer.getPhone());
        holder.tvSkills.setText("Skills: " + volunteer.getSkills());
        holder.tvAvailability.setText(
                "Availability: " + volunteer.getAvailability());
        holder.tvLocation.setText(
                "Location: " + volunteer.getLocation());

        boolean isOwner = currentUserId != null
                && currentUserId.equals(volunteer.getUserId());

        holder.layoutActions.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(volunteer));

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(volunteer));
    }

    @Override
    public int getItemCount() {
        return volunteerList.size();
    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPhone, tvSkills;
        TextView tvAvailability, tvLocation;

        LinearLayout layoutActions;

        Button btnEdit, btnDelete;

        VolunteerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvVolunteerName);
            tvPhone = itemView.findViewById(R.id.tvVolunteerPhone);
            tvSkills = itemView.findViewById(R.id.tvVolunteerSkills);
            tvAvailability = itemView.findViewById(
                    R.id.tvVolunteerAvailability);
            tvLocation = itemView.findViewById(
                    R.id.tvVolunteerLocation);

            layoutActions = itemView.findViewById(
                    R.id.layoutVolunteerActions);

            btnEdit = itemView.findViewById(R.id.btnEditVolunteer);
            btnDelete = itemView.findViewById(R.id.btnDeleteVolunteer);
        }
    }
}
