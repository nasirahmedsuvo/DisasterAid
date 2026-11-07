
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
import com.example.disasteraid.models.Resource;

import java.util.List;

public class ResourceAdapter
        extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    public interface ResourceActionListener {
        void onEdit(Resource resource);
        void onDelete(Resource resource);
    }

    private final List<Resource> resourceList;
    private final String currentUserId;
    private final ResourceActionListener listener;

    public ResourceAdapter(
            List<Resource> resourceList,
            String currentUserId,
            ResourceActionListener listener) {

        this.resourceList = resourceList;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resource, parent, false);

        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ResourceViewHolder holder, int position) {

        Resource resource = resourceList.get(position);

        holder.tvName.setText(resource.getResourceName());
        holder.tvCategory.setText(
                "Category: " + resource.getCategory());
        holder.tvQuantity.setText(
                "Quantity: " + resource.getQuantity());
        holder.tvLocation.setText(
                "Location: " + resource.getLocation());

        boolean isOwner = currentUserId != null
                && currentUserId.equals(resource.getUserId());

        holder.layoutActions.setVisibility(
                isOwner ? View.VISIBLE : View.GONE);

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(resource));

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(resource));
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    static class ResourceViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvCategory, tvQuantity, tvLocation;
        LinearLayout layoutActions;
        Button btnEdit, btnDelete;

        ResourceViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvResourceName);
            tvCategory = itemView.findViewById(R.id.tvResourceCategory);
            tvQuantity = itemView.findViewById(R.id.tvResourceQuantity);
            tvLocation = itemView.findViewById(R.id.tvResourceLocation);

            layoutActions = itemView.findViewById(
                    R.id.layoutResourceActions);

            btnEdit = itemView.findViewById(R.id.btnEditResource);
            btnDelete = itemView.findViewById(R.id.btnDeleteResource);
        }
    }
}
