package layout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.belladati.android.views.StackedBarChart;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.example.karimt.belladatidongleapp.ConnectBTActivity;
import com.example.karimt.belladatidongleapp.MainActivity;
import com.example.karimt.belladatidongleapp.PhoneNumber;
import com.example.karimt.belladatidongleapp.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ObjectInputStream;

/**
 * Created by KarimT on 06.02.2017.
 */

public class EmerAndBreakSup extends Fragment {
    Button callService,callEmergency;
    String serviceNumber,emergencyNumber;
    public EmerAndBreakSup() {
    }

    public static EmerAndBreakSup newInstance() {
        EmerAndBreakSup fragment = new EmerAndBreakSup();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.emer_break_sup, container, false);
        callEmergency=(Button) rootView.findViewById(R.id.btnCallEmergency);
        callService=(Button) rootView.findViewById(R.id.btnCallService);
        callEmergency.setOnClickListener(new CallEmergencyListener());
        callService.setOnClickListener(new CallServiceListener());
        super.onCreate(savedInstanceState);


        return rootView;
    }
    private class CallEmergencyListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            PhoneNumber phoneNumber = (PhoneNumber) loadClassFileEmerNumber();
            if (phoneNumber != null) {
                emergencyNumber = phoneNumber.getFirstAidNumber();
            }
            if(!emergencyNumber.equals(""))
            {
                Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + emergencyNumber));
                try {
                    startActivity(in);
                } catch (android.content.ActivityNotFoundException ex) {

                    Toast.makeText(getContext(), "yourActivity is not founded", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private class CallServiceListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            PhoneNumber phoneNumber = (PhoneNumber) loadClassFileEmerNumber();
            if (phoneNumber != null) {
                serviceNumber = phoneNumber.getServiceNumber();
            }
            if(!serviceNumber.equals(""))
            {
                Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + serviceNumber));
                try {
                    startActivity(in);
                } catch (android.content.ActivityNotFoundException ex) {

                    Toast.makeText(getContext(), "yourActivity is not founded", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public Object loadClassFileEmerNumber() {
        try {
            ObjectInputStream ois = new ObjectInputStream(getActivity().openFileInput("phoneNumber.xml"));
            Object o = ois.readObject();
            return o;

        } catch (Exception ex) {
            Log.v("MacAddress", ex.getMessage());

            ex.printStackTrace();
        }
        return null;
    }
}
