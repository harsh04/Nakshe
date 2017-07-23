package harshmathur.nic.soi.com.nakshe;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyView> {

    private List<String> name;
    private List<String> state;
    private List<String> OSM;

    public class MyView extends RecyclerView.ViewHolder {

        public TextView name, state, osm_N;

        public MyView(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.cityName);
            state = (TextView) view.findViewById(R.id.state);
            osm_N = (TextView) view.findViewById(R.id.osm_sheet);
        }
    }

    public RecyclerViewAdapter(List<String> name, List<String> state, List<String> OSM) {
        this.name = name;
        this.state = state;
        this.OSM = OSM;
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_item, parent, false);

        return new MyView(itemView);
    }

    @Override
    public void onBindViewHolder(final MyView holder, final int position) {

        holder.name.setText(name.get(position));
        holder.state.setText(state.get(position));
        holder.osm_N.setText("OSM Number : "+OSM.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

}
