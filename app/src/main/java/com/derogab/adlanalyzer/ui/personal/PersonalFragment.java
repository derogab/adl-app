package com.derogab.adlanalyzer.ui.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.databinding.FragmentPersonalBinding;
import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class PersonalFragment extends Fragment {

    private static final String TAG = "PersonalFragment";

    private FragmentPersonalBinding binding;

    private PersonalViewModel personalViewModel;
    private List<FormElement> elements;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create ViewModel
        personalViewModel = new ViewModelProvider(requireActivity()).get(PersonalViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // View Binding
        binding = FragmentPersonalBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PERSONAL_DATA_INFORMATION_FILE_NAME, Context.MODE_PRIVATE);

        // Get elements for form
        personalViewModel.getFormElements().observe(getViewLifecycleOwner(), new Observer<List<FormElement>>() {
            @Override
            public void onChanged(List<FormElement> elements) {

                Log.d(TAG, "Elements: " + elements);

                setElements(elements);
                binding.loader.setVisibility(View.GONE);
                binding.fragmentPersonalSaveButton.show();
                binding.personalFormContent.generate(elements, sharedPreferences);

            }

        });

        // Saving button to save info
        binding.fragmentPersonalSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.personalFormContent.save(elements, sharedPreferences);
                Snackbar.make(v, R.string.fragment_personal_saved, Snackbar.LENGTH_SHORT).show();

            }
        });

    }

    /**
     * setElements()
     *
     * Set the current form elements
     * */
    public void setElements(List<FormElement> elements) {
        this.elements = elements;
    }
}
