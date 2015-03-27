package com.davisosa.structura.fragments;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.davisosa.structura.R;
import com.davisosa.structura.view.EdgeView;
import com.davisosa.structura.view.NodeView;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LLPlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LLPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LLPlayFragment extends Fragment {
    private LinearLayout mNodeLayout;

    private Button mInsBtn;
    private Button mDelBtn;
    private Button mSearchBtn;

    private LinkedList<Pair<NodeView, EdgeView>> mNodes = new LinkedList<>();
    private Handler mHandler = new Handler();
    private Sequencer mSequencer = new Sequencer();

    private OnFragmentInteractionListener mListener;

    public LLPlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LLPlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LLPlayFragment newInstance() {
        LLPlayFragment fragment = new LLPlayFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout root = (FrameLayout) inflater.inflate(R.layout.fragment_ll_play,
                container, false);
        mNodeLayout = (LinearLayout) root.findViewById(R.id.node_layout);
        final Resources res = getResources();

        mInsBtn = (Button) root.findViewById(R.id.btn_insert);
        mInsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EdgeView edgeView = null;
                if (mNodeLayout.getChildCount() > 0) {
                    resetNodeColors();
                    edgeView = new EdgeView(getActivity());
                    mNodeLayout.addView(edgeView, res.getDimensionPixelSize(R.dimen.edge_width),
                            res.getDimensionPixelSize(R.dimen.edge_height));
                } else {
                    mDelBtn.setEnabled(true);
                    mSearchBtn.setEnabled(true);
                }

                NodeView nodeView = new NodeView(getActivity());
                nodeView.setId(mSequencer.next());
                mNodeLayout.addView(nodeView, res.getDimensionPixelSize(R.dimen.node_width),
                        res.getDimensionPixelSize(R.dimen.node_height));

                mNodes.add(Pair.create(nodeView, edgeView));
            }
        });

        mDelBtn = (Button) root.findViewById(R.id.btn_delete);
        mDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetNodeColors();
                showDeleteDialog();
            }
        });

        mSearchBtn = (Button) root.findViewById(R.id.btn_search);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetNodeColors();
                showSearchDialog();
            }

        });

        return root;
    }

    private void showDeleteDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_delete_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.dialog_num_input, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        FrameLayout root = (FrameLayout) dialog.getCustomView();
                        EditText input = (EditText) root.findViewById(R.id.input);
                        int id = 0;
                        try {
                            id = Integer.valueOf(input.getText().toString());
                        } catch (NumberFormatException nfe) {
                            Timber.w(nfe, "User didn't input number.");
                        }

                        if (!removeNodePair(id)) {
                            //TODO alert user when node not in list
                        }
                    }
                })
                .show();
    }

    private void showSearchDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.dialog_search_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.dialog_num_input, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        FrameLayout root = (FrameLayout) dialog.getCustomView();
                        EditText input = (EditText) root.findViewById(R.id.input);
                        int id = 0;
                        try {
                            id = Integer.valueOf(input.getText().toString());
                        } catch (NumberFormatException nfe) {
                            Timber.w(nfe, "User didn't input number.");
                        }

                        if (!findNodePair(id)) {
                            //TODO alert user when node not in list
                        }
                    }
                })
                .show();
    }

    /**
     * Removes the {@link android.util.Pair} that contains the node with the specified ID,
     * and colors each visited node including the desired node.
     *
     * @param id node ID
     * @return {@code true} if node was found, {@code false} otherwise.
     */
    private boolean removeNodePair(int id) {
        final Resources res = getResources();
        int count = 0;
        for (final Pair<NodeView, EdgeView> pair : mNodes) {
            if (pair.first.getId() == id) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pair.first.setColor(res.getColor(R.color.red_400));
                    }
                }, 500 * ++count);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNodeLayout.removeView(pair.second);
                        mNodeLayout.removeView(pair.first);

                        if (pair.equals(mNodes.element()) && mNodes.size() > 1) {
                            Pair<NodeView, EdgeView> nextPair = mNodes.get(1);
                            mNodeLayout.removeView(nextPair.second);
                            mNodes.set(1, new Pair<>(nextPair.first, (EdgeView) null));
                        }
                        mNodes.remove(pair);

                        mDelBtn.setEnabled(mNodeLayout.getChildCount() > 0);
                        mSearchBtn.setEnabled(mNodeLayout.getChildCount() > 0);
                    }
                }, 500 * ++count);

                return true;
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pair.first.setColor(res.getColor(R.color.grey_500));
                    }
                }, 500 * ++count);
            }
        }
        return false;
    }

    /**
     * Finds the {@link android.util.Pair} that contains the node with the specified ID,
     * and colors each visited node including the desired node.
     *
     * @param id node ID
     * @return {@code true} if node was found, {@code false} otherwise.
     */
    private boolean findNodePair(int id) {
        final Resources res = getResources();
        int count = 0;
        for (final Pair<NodeView, EdgeView> pair : mNodes) {
            if (pair.first.getId() == id) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pair.first.setColor(res.getColor(R.color.blue_400));
                    }
                }, 500 * ++count);
                return true;
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pair.first.setColor(res.getColor(R.color.grey_500));
                    }
                }, 500 * ++count);
            }
        }
        return false;
    }

    private void resetNodeColors() {
        for (Pair<NodeView, EdgeView> pair : mNodes) {
            pair.first.resetColor();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private class Sequencer {
        private final AtomicInteger mSequenceNumber = new AtomicInteger(0);

        public int next() {
            return mSequenceNumber.incrementAndGet();
        }
    }
}