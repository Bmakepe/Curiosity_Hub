package com.makepe.curiosityhubls.Fragments.MaterialFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.makepe.curiosityhubls.R;

public class GroupDiscussionFragment extends Fragment {

    EditText messageET;
    ImageButton voiceBTN;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.group_discussions_fragment, container, false);

        messageET = view.findViewById(R.id.groupMessageET);
        voiceBTN = view.findViewById(R.id.groupSendBTN);

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() != 0){

                    voiceBTN.setImageResource(R.drawable.ic_baseline_send_24);
                }else{

                    voiceBTN.setImageResource(R.drawable.ic_baseline_mic_24);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }
}