
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
import com.example.disasteraid.models.AidRequest;

import java.util.List;

public class RequestAdapter
        extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    public interface RequestActionListener {
        void onEdit(AidRequest request);
        void onDelete(AidRequest request);
    }

    private final List<AidRequest> requestList;
    private final String currentUserId;
    private final RequestActionListener listener;

    public RequestAdapter(
            List<AidRequest> requestList,
            String currentUserId,
            RequestActionListener listener) {

        this.requestList = requestList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RequestViewHolder holder, int position) {

        AidRequest request = requestList.get(position);

        holder.tvTitle.setText(request.getTitle());
        holder.tvCategory.setText("Category: " + request.getCategory());
        holder.tvDescription.setText(request.getDescription());
        holder.tvLocation.setText("Location: " + request.getLocation());
        holder.tvQuantity.setText("Quantity: " + request.getQuantity());
        holder.tvStatus.setText(request.getStatus());

        // Show actions only to the request owner
        boolean isOwner = currentUserId != null
                && currentUserId.equals(request.getUserId());

        holder.layoutOwnerActions.setVisibility(
                isOwner ? View.VISIBLE : View.GONE
        );

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(request);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvCategory, tvDescription;
        TextView tvLocation, tvQuantity, tvStatus;

        Button btnEdit, btnDelete;
        LinearLayout layoutOwnerActions;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvRequestTitle);
            tvCategory = itemView.findViewById(R.id.tvRequestCategory);
            tvDescription = itemView.findViewById(R.id.tvRequestDescription);
            tvLocation = itemView.findViewById(R.id.tvRequestLocation);
            tvQuantity = itemView.findViewById(R.id.tvRequestQuantity);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);

            btnEdit = itemView.findViewById(R.id.btnEditRequest);
            btnDelete = itemView.findViewById(R.id.btnDeleteRequest);

            layoutOwnerActions =
                    itemView.findViewById(R.id.layoutOwnerActions);
        }
    }
}
