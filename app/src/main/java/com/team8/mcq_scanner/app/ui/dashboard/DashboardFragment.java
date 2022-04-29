package com.team8.mcq_scanner.app.ui.dashboard;

import static com.team8.mcq_scanner.app.managers.Constants.REF_IMAGES;
import static com.team8.mcq_scanner.app.managers.Constants.REF_QNA;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TASKS;
import static com.team8.mcq_scanner.app.managers.Constants.REF_TESTS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team8.mcq_scanner.app.adapters.HomeAdapters;
import com.team8.mcq_scanner.app.adapters.TasksAdapter;
import com.team8.mcq_scanner.app.adapters.TestAdapter;
import com.team8.mcq_scanner.app.databinding.FragmentDashboardBinding;
import com.team8.mcq_scanner.app.models.Image;
import com.team8.mcq_scanner.app.models.Tasks;
import com.team8.mcq_scanner.app.models.Tests;

import java.util.ArrayList;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DatabaseReference reference;
    private ArrayList<Tests> testsArrayList;
    private TestAdapter homeAdapters;
    private FirebaseAuth auth;
    private String currentUser;
    private LinearLayoutManager layoutManager;
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        currentUser = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        final RecyclerView completedRV = binding.completedRV;
        final FloatingActionButton fab = binding.fab;
        reference = FirebaseDatabase.getInstance().getReference(REF_QNA).child(currentUser);
        reference.keepSynced(true);
        testsArrayList = new ArrayList<>();
        completedRV.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        completedRV.setLayoutManager(layoutManager);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren()){
                    testsArrayList = new ArrayList<>();
                    for (DataSnapshot datasnapshot :
                            snapshot.getChildren()) {
                        Tests user = datasnapshot.getValue(Tests.class);
                        testsArrayList.add(user);
                    }
                }
                if (!testsArrayList.isEmpty()){
                    homeAdapters = new TestAdapter(getContext(),testsArrayList,true);
                    completedRV.setAdapter(homeAdapters);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}