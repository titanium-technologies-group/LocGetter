package codes.titanium.locgetter_sample;


import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LocationsRvAdapter extends RecyclerView.Adapter {

    private List<Location> locations;

    public LocationsRvAdapter() {
        this.locations = new ArrayList<>();
    }

    public void addLocation(Location location) {
        locations.add(location);
        notifyItemInserted(locations.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((TextView) holder.itemView.findViewById(android.R.id.text1)).setText(locations.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }
}
