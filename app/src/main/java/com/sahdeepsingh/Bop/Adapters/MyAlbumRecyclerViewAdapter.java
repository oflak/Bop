package com.sahdeepsingh.Bop.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sahdeepsingh.Bop.R;
import com.sahdeepsingh.Bop.SongData.Song;
import com.sahdeepsingh.Bop.fragments.FragmentAlbum.OnListFragmentInteractionListener;
import com.sahdeepsingh.Bop.playerMain.Main;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAlbumRecyclerViewAdapter extends RecyclerView.Adapter<MyAlbumRecyclerViewAdapter.ViewHolder> {

    private final List<String> mValues;
    private OnListFragmentInteractionListener mListener;

    public MyAlbumRecyclerViewAdapter(List<String> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.albumname.setText(mValues.get(position));
        String selectedAlbum = Main.songs.getAlbums().get(position);
        List<Song> songsList = Main.songs.getSongsByAlbum(selectedAlbum);
        for (int i = 0; i < songsList.size(); i++) {
            String path = Main.songs.getAlbumArt(songsList.get(i));
            if (path != null) {
                Picasso.get().load(new File(path)).fit().centerCrop().error(R.mipmap.ic_launcher).into(holder.albumart);
                break;
            }
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = holder.mView.getContext();
                if (context instanceof OnListFragmentInteractionListener) {
                    mListener = (OnListFragmentInteractionListener) context;
                } else {
                    throw new RuntimeException(context.toString()
                            + " must implement OnListFragmentInteractionListener");
                }
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.getAdapterPosition(), "AlbumList");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView albumname;
        public final ImageView albumart;

        ViewHolder(View view) {
            super(view);
            mView = view;
            albumname = view.findViewById(R.id.AlbumName);
            albumart = view.findViewById(R.id.albumArt1);
        }

    }
}
