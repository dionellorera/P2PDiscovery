package com.example.dione.p2pdiscovery;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.util.MutableChar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.uepaa.p2pkit.P2PKitClient;
import ch.uepaa.p2pkit.P2PKitStatusCallback;
import ch.uepaa.p2pkit.StatusResult;
import ch.uepaa.p2pkit.StatusResultHandling;
import ch.uepaa.p2pkit.discovery.GeoListener;
import ch.uepaa.p2pkit.discovery.InfoTooLongException;
import ch.uepaa.p2pkit.discovery.P2PListener;
import ch.uepaa.p2pkit.discovery.entity.Peer;
import ch.uepaa.p2pkit.internal.messaging.MessageTooLargeException;
import ch.uepaa.p2pkit.messaging.MessageListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String APP_KEY = "9a97db9011d04bd89442ee46640a7feb";
    private Context mContext;
    private Button sendMessagetoPeers;
    ArrayList<Peer> nodeList;
    PeerAdapter peerAdapter;
    ListViewCompat peerList;
    AppCompatTextView myNodeId;
    AppCompatTextView noPeersAroundMessage;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initViews();
        enableP2PKit();
        initPeerAdapter();
    }

    private void initViews(){
        progressDialog = new ProgressDialog(mContext);
        sendMessagetoPeers = (Button) findViewById(R.id.sendMessagetoPeers);
        sendMessagetoPeers.setOnClickListener(this);
        myNodeId = (AppCompatTextView) findViewById(R.id.myNodeId);
        noPeersAroundMessage = (AppCompatTextView) findViewById(R.id.noPeersAroundMessage);
        nodeList = new ArrayList<>();
    }

    private void showNoPeerMessage(){
        peerList.setVisibility(View.GONE);
        noPeersAroundMessage.setVisibility(View.VISIBLE);
    }

    private void showPeerList(){
        peerList.setVisibility(View.VISIBLE);
        noPeersAroundMessage.setVisibility(View.GONE);
    }
    private void initPeerAdapter(){
        peerList = (ListViewCompat) findViewById(R.id.peerList);
        peerAdapter = new PeerAdapter(mContext, nodeList);
        peerList.setAdapter(peerAdapter);
        peerAdapter.notifyDataSetChanged();
    }
    private void enableP2PKit(){
         final P2PKitStatusCallback mStatusCallback = new P2PKitStatusCallback() {
            @Override
            public void onEnabled() {
                // ready to start discovery
                addP2PListener();
                addGeoDiscovery();
                addMessaging();
                progressDialog.dismiss();
            }

            @Override
            public void onSuspended() {
                // p2pkit is temporarily suspended
            }

            @Override
            public void onResumed() {
                // coming back from a suspended state
            }

            @Override
            public void onDisabled() {
                // p2pkit has been disabled
            }

            @Override
            public void onError(StatusResult statusResult) {
                // enabling failed, handle statusResult
            }
        };

        final StatusResult result = P2PKitClient.isP2PServicesAvailable(this);
        if (result.getStatusCode() == StatusResult.SUCCESS) {
            P2PKitClient client = P2PKitClient.getInstance(this);
            client.enableP2PKit(mStatusCallback, APP_KEY);
            progressDialog.setMessage("Enabling P2P Kit");
            progressDialog.setCancelable(true);
            progressDialog.show();
        } else {
            StatusResultHandling.showAlertDialogForStatusError(this, result);
        }
    }

    private void addP2PListener(){
        final P2PListener mP2pDiscoveryListener = new P2PListener() {
            @Override
            public void onP2PStateChanged(int state) {
                Log.d("P2PListener", "State changed: " + state);
            }

            @Override
            public void onPeerDiscovered(Peer peer) {
                Log.d("P2PListener", "Peer discovered: " + peer.getNodeId() );
                if (!nodeList.contains(peer)){
                    showPeerList();
                    nodeList.add(peer);
                    peerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onPeerLost(Peer peer) {
                Log.d("P2PListener", "Peer lost: " + peer.getNodeId());
                nodeList.remove(peer);
                peerAdapter.notifyDataSetChanged();
                if (nodeList.isEmpty()){
                    showNoPeerMessage();
                }
            }

            @Override
            public void onPeerUpdatedDiscoveryInfo(Peer peer) {
                Log.d("P2PListener", "Peer updated: " + peer.getNodeId() + " with new info: " + new String(peer.getDiscoveryInfo()));
            }

            @Override
            public void onProximityStrengthChanged(Peer peer) {
                Log.d("P2pListener", "Peer " + peer.getNodeId() + " changed proximity strength: " + peer.getProximityStrength());
            }
        };
        myNodeId.setText("My Id-" +String.valueOf(P2PKitClient.getInstance(this).getNodeId()));
        P2PKitClient.getInstance(mContext).getDiscoveryServices().addP2pListener(mP2pDiscoveryListener);

        try {
            P2PKitClient.getInstance(this).getDiscoveryServices().setP2pDiscoveryInfo("Hello p2pkit".getBytes());
        } catch (InfoTooLongException e) {
            Log.e("P2PListener", "The discovery info is too long");
        }
    }

    private void addGeoDiscovery(){
        final GeoListener mGeoDiscoveryListener = new GeoListener() {
            @Override
            public void onGeoStateChanged(int state) {
                Log.d("GeoListener", "State changed: " + state);
            }

            @Override
            public void onPeerDiscovered(final UUID nodeId) {
                Log.d("GeoListener", "Peer discovered: " + nodeId);
//                if (!nodeList.contains(nodeId)) {
//                    nodeList.add(nodeId);
//                }
            }

            @Override
            public void onPeerLost(final UUID nodeId) {
                Log.d("GeoListener", "Peer lost: " + nodeId);
            }
        };
        P2PKitClient.getInstance(mContext).getDiscoveryServices().addGeoListener(mGeoDiscoveryListener);
    }

    private void addMessaging(){
        final MessageListener mMessageListener = new MessageListener() {
            @Override
            public void onMessageStateChanged(int state) {
                Log.d("MessageListener", "State changed: " + state);
            }

            @Override
            public void onMessageReceived(long timestamp, UUID origin, String type, byte[] message) {
                Toast.makeText(mContext, "Message received: From=" + origin + " message=" + new String(message), Toast.LENGTH_SHORT).show();
//                Log.d("MessageListener", "Message received: From=" + origin + " type=" + type + " message=" + new String(message));
            }
        };
        P2PKitClient.getInstance(mContext).getMessageServices().addMessageListener(mMessageListener);
    }

    private void sendMessage(UUID nodeId){
        //limit is 2mb
        try {
            boolean forwarded = P2PKitClient.getInstance(mContext).getMessageServices().sendMessage(nodeId, "text/plain", "Hello My Peer Mate!".getBytes());
            Toast.makeText(mContext, "Sending message to " + String.valueOf(nodeId), Toast.LENGTH_SHORT).show();
        } catch (MessageTooLargeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sendMessagetoPeers:
                if (nodeList != null){
                    for (Peer nodes: nodeList) {
                        sendMessage(nodes.getNodeId());
                    }
                }
                break;
        }
    }
}
