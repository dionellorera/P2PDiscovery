package com.example.dione.p2pdiscovery;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import ch.uepaa.p2pkit.discovery.entity.Peer;

/**
 * Created by dione on 6 Sep 2016.
 */
public class PeerAdapter extends ArrayAdapter<Peer> {
    ViewHolderItem viewHolderItem;
    private Context mContext;
    ArrayList<Peer> nodeArrayList;
    public PeerAdapter(Context context , ArrayList<Peer> nodeArrayList) {
        super(context, R.layout.peers_item_row, nodeArrayList);
        this.mContext = context;
        this.nodeArrayList = nodeArrayList;
    }

    static class ViewHolderItem{
        AppCompatTextView peerNodeId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.peers_item_row, parent, false);
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.peerNodeId = (AppCompatTextView) convertView.findViewById(R.id.peerNodeId);
            convertView.setTag(viewHolderItem);
        }else{
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }
        if (nodeArrayList!=null){
            viewHolderItem.peerNodeId.setText(String.valueOf(nodeArrayList.get(position).getNodeId()));
        }
        return convertView;
    }
}
