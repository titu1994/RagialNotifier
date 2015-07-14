package com.somshubra.ragialnotifier.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;

/**
 * Created by Yue on 5/31/2015.
 */
public class RagialErrorDialog extends DialogFragment {

    private TextView tv;
    private String errorMessage;

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;

        if(tv != null)
            tv.setText(errorMessage);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle("Enter name to search");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_notify_error, null, false);
        builder.setView(v);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final TextView ed = (TextView) v.findViewById(R.id.dialog_error_text);
        tv = ed;
        ed.setText(errorMessage);

        return builder.create();
    }
}
